package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.VaultMod;
import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.EntityAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import iskallia.vault.core.world.data.entity.PartialEntity;
import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.storage.WorldZones;
import iskallia.vault.entity.boss.BossRuneModifiers;
import iskallia.vault.world.data.WorldZonesData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BossRunePillarAccessor;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.MobAttributeModifierSettable;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.lib.EntityAttributeModifierSettable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.Phase;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Drives the hyperboss cycle: arms the pillar with escalated stats, runs the periodic and
 * health-gated brutal waves, and hands a finished fight over to the escalation manager.
 */
public class HyperBossManager extends ObjectiveManager<HyperVaultObjective> {
    // Arena adds: hostile picks from the config's the_vault:tank / the_vault:assassin entity
    // groups (the groups config only exposes match-predicates, so the spawnable subset is
    // curated here, same as BrutalBossesRegistry does for its bosses).
    private static final ResourceLocation[] TANK_ADDS = {
            VaultMod.id("shiver"),
            VaultMod.id("deathcap"),
            VaultMod.id("blood_tank"),
            VaultMod.id("overgrown_tank"),
            VaultMod.id("pirate_guardian_tank"),
            VaultMod.id("craftenstein"),
            VaultMod.id("yeti"),
            VaultMod.id("deep_dark_horror"),
            ResourceLocation.parse("minecraft:piglin_brute"),
            WoldsVaults.id("haturkin"),
    };
    private static final ResourceLocation[] ASSASSIN_ADDS = {
            VaultMod.id("vault_spider"),
            VaultMod.id("t3_skeleton"),
            VaultMod.id("t3_stray"),
            VaultMod.id("t3_wither_skeleton"),
            VaultMod.id("t3_creeper"),
            VaultMod.id("t3_enderman"),
            VaultMod.id("blood_slime"),
            VaultMod.id("vault_wraith_white"),
            VaultMod.id("vault_wraith_yellow"),
            VaultMod.id("grimwick"),
            VaultMod.id("winter_wolf"),
            WoldsVaults.id("black_ghost"),
            WoldsVaults.id("blue_ghost"),
    };

    // Stable id for the boss's percent damage-escalation modifier (idempotent across reloads).
    private static final UUID HYPER_DAMAGE_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:hyper_damage_escalation".getBytes(StandardCharsets.UTF_8));
    // The health_attribute trait's baseValue in vault_boss.json (the boss's innate +50%);
    // part of the reference total the vault health factor multiplies.
    private static final double INNATE_HEALTH_BONUS = 0.5;
    private static final ResourceLocation MAX_HEALTH_ID = ResourceLocation.parse("generic.max_health");
    // The four arena gates of the BOSS_1 room style, relative to the pillar (RuneBossAnimation).
    private static final BlockPos[] GATE_OFFSETS = {
            new BlockPos(23, 4, 0), new BlockPos(-23, 4, 0),
            new BlockPos(0, 4, 23), new BlockPos(0, 4, -23)};

    private final HyperEscalationManager escalation;
    // Transient on purpose: a reload mid-fight just restarts the short add countdown.
    private int addTimer = HyperVaultObjective.FIGHT_ADD_PERIOD_TICKS;

    public HyperBossManager(Vault vault, VirtualWorld world, HyperVaultObjective objective, HyperEscalationManager escalation) {
        super(vault, world, objective);
        this.escalation = escalation;
    }

    /** Podium shift-click during ARMED: escalate the pillar's stats and start a fresh fight. */
    public void armAndStartFight(BlockPos pillarPos) {
        RuneBossFights fights = objective.get(HyperVaultObjective.FIGHTS);
        if (fights.hasFightAt(pillarPos)) {
            WoldsVaults.LOGGER.warn("Podium at {} already has a scheduled/active fight; not re-arming.", pillarPos);
            return;
        }
        BlockEntity be = world.getBlockEntity(pillarPos);
        if (!(be instanceof BossRunePillarTileEntity pillar)) {
            WoldsVaults.LOGGER.error("Hyper podium at {} has no BossRunePillarTileEntity — cannot start the fight (staying armed).", pillarPos);
            return;
        }
        rerollBoss(pillar);
        objective.set(HyperVaultObjective.PILLAR_POS, pillarPos);
        // The fight removes the pillar block when the boss summons; snapshot the tile so the
        // escalation manager can re-place it (config, boss list and zone id intact) next cycle.
        objective.set(HyperVaultObjective.PILLAR_NBT, pillar.saveWithoutMetadata());
        // Before the ability/stat writes: the tile's onLoad also refreshes ability modifiers,
        // which would resurrect the revive ability if it ran after setReviveAbility(null).
        ensureProtectionZone(pillar, pillarPos);
        snapshotOrRepairGates(pillarPos);

        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double escalation = HyperVaultObjective.BOSS_HEALTH_PERCENT
                * Math.pow(HyperVaultObjective.HYPER_STAT_FACTOR, cycle)
                + HyperVaultObjective.BOSS_STAT_INCREMENT * cycle;
        // The boss's health is one giant MULTIPLY_BASE trait term, so applying the vault's mob
        // health modifiers as attribute modifiers dilutes them to noise (+100% of base next to
        // the +5000%·1.75^cycle escalation is +2%). Instead the vault's health factor —
        // computed exactly as it stacks on a normal mob — multiplies the whole escalated total
        // here, before the traits (and the boss's frozen raw health) are built at summon.
        // Damage stays out of the trait (its operator is flat "add"): the percent escalation
        // and the vault's non-health mob modifiers are applied in applyBossStats instead.
        double healthFactor = vaultHealthFactor();
        double healthPercent = (1.0 + INNATE_HEALTH_BONUS + escalation) * healthFactor
                - 1.0 - INNATE_HEALTH_BONUS;
        if (healthFactor > 1.0) {
            WoldsVaults.LOGGER.info("Hyperboss health inherits the vault's mob modifiers: x{}.",
                    Math.round(healthFactor * 100.0) / 100.0);
        }
        BossRuneModifiers armed = new BossRuneModifiers(healthPercent, 0.0,
                HyperVaultObjective.BOSS_ABILITY_HASTE);
        pillar.getModifiers().copyFrom(armed);
        // Rune count keys the shield/waveblast/revive settings table; capped so an absurd cycle
        // count cannot index past the config.
        pillar.setRuneCount(Math.min(HyperVaultObjective.BASE_RUNE_TIER + cycle, HyperVaultObjective.RUNE_TIER_CAP));
        pillar.getModifiers().setReviveAbility(null); // the hyperboss never heals or revives

        objective.set(HyperVaultObjective.SCORE, 0);
        fights.add(pillar.createFight());
        objective.set(HyperVaultObjective.PHASE, Phase.FIGHT);
        objective.set(HyperVaultObjective.WAVE_TICK, HyperVaultObjective.WAVE_PERIOD_TICKS);
        this.addTimer = HyperVaultObjective.FIGHT_ADD_PERIOD_TICKS;
        // BOSS_ID intentionally keeps its previous value until the new boss spawns: gate checks
        // resolve a dead entity to null and skip, and ENTITY_SPAWN overwrites it on summon.
        objective.set(HyperVaultObjective.GATE_MASK, 0);
        HyperVaultObjective.broadcast(vault, "The Hyperboss awakens!", ChatFormatting.DARK_RED);
    }

    /**
     * The gate-frame animation re-places its own blocks every open/close, but explosion damage
     * just OUTSIDE the frame templates — the floor row bordering each gate — persists forever
     * (the room is unprotected between fights). The pristine surroundings are captured once at
     * the first arm (the zone has protected the room until then) and any hole is patched from
     * that snapshot at every later arm, before the doors close.
     */
    private void snapshotOrRepairGates(BlockPos pillarPos) {
        if (!objective.has(HyperVaultObjective.GATE_NBT)) {
            snapshotGateSurrounds(pillarPos);
        } else {
            repairGateSurrounds(pillarPos);
        }
    }

    /** A box per gate: ±5 along the wall, ±2 across it, pillarY−3..+3 (the frames heal higher). */
    private void forEachGatePos(BlockPos pillarPos, java.util.function.Consumer<BlockPos> action) {
        for (BlockPos offset : GATE_OFFSETS) {
            boolean xWall = offset.getX() != 0;
            for (int along = -5; along <= 5; along++) {
                for (int across = -2; across <= 2; across++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        int dx = offset.getX() + (xWall ? across : along);
                        int dz = offset.getZ() + (xWall ? along : across);
                        action.accept(pillarPos.offset(dx, dy, dz));
                    }
                }
            }
        }
    }

    private void snapshotGateSurrounds(BlockPos pillarPos) {
        ListTag blocks = new ListTag();
        forEachGatePos(pillarPos, pos -> {
            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                return;
            }
            CompoundTag entry = new CompoundTag();
            entry.putIntArray("p", new int[]{
                    pos.getX() - pillarPos.getX(), pos.getY() - pillarPos.getY(), pos.getZ() - pillarPos.getZ()});
            entry.put("s", NbtUtils.writeBlockState(state));
            blocks.add(entry);
        });
        CompoundTag tag = new CompoundTag();
        tag.put("blocks", blocks);
        objective.set(HyperVaultObjective.GATE_NBT, tag);
        WoldsVaults.LOGGER.info("Snapshotted {} pristine blocks around the arena gates for per-cycle repair.", blocks.size());
    }

    private void repairGateSurrounds(BlockPos pillarPos) {
        ListTag blocks = objective.get(HyperVaultObjective.GATE_NBT).getList("blocks", Tag.TAG_COMPOUND);
        int repaired = 0;
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag entry = blocks.getCompound(i);
            int[] p = entry.getIntArray("p");
            if (p.length != 3) {
                continue;
            }
            BlockPos pos = pillarPos.offset(p[0], p[1], p[2]);
            if (!world.getBlockState(pos).isAir()) {
                continue;
            }
            BlockState state = NbtUtils.readBlockState(entry.getCompound("s"));
            if (state.isAir()) {
                continue;
            }
            IZonedWorld.runWithBypass(world, true, () -> world.setBlock(pos, state, 3));
            repaired++;
        }
        if (repaired > 0) {
            WoldsVaults.LOGGER.info("Repaired {} destroyed blocks around the arena gates.", repaired);
        }
    }

    /**
     * The room's no-modify zone normally lives from room load until an open-room animation
     * step that never runs; Hyper scopes it to the fight instead: (re)created on every arm
     * here, removed by the escalation manager when the boss dies. The tile's own onLoad is
     * reused so the zone box and flags stay exactly vanilla.
     */
    private void ensureProtectionZone(BossRunePillarTileEntity pillar, BlockPos pillarPos) {
        BossRunePillarAccessor access = (BossRunePillarAccessor) pillar;
        WorldZones zones = WorldZonesData.get(world.getServer()).getOrCreate(world.dimension());
        int zoneId = access.getZoneId();
        if (zoneId <= 0 || zones.get(zoneId).isEmpty()) {
            // Stale id from a previous cycle (that zone was removed on kill): reset so the
            // tile registers a fresh zone.
            access.setZoneId(0);
            pillar.onLoad();
            zoneId = access.getZoneId();
            WoldsVaults.LOGGER.info("Recreated the boss room protection zone ({}) for this fight.", zoneId);
        }
        if (zoneId <= 0) {
            WoldsVaults.LOGGER.warn("Hyper fight at {} has no protection zone: the pillar config defines no zone box.", pillarPos);
        }
        objective.set(HyperVaultObjective.ZONE_ID, zoneId);
    }

    @Override
    public void tick() {
        if (objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING) != Phase.FIGHT) {
            return;
        }
        RuneBossFights fights = objective.get(HyperVaultObjective.FIGHTS);
        if (!fights.hasPendingFight()) {
            // The fight machinery also completes this way if the boss entity vanishes (e.g. a
            // reload edge); indistinguishable from a kill here, so the cycle is awarded either way.
            escalation.onBossKilled();
            return;
        }

        tickWaveTimer();
        tickHealthGates();
        tickFightAdds();
    }

    /** A lone tank or assassin joins the arena every few seconds while the boss lives. */
    private void tickFightAdds() {
        if (--this.addTimer > 0) {
            return;
        }
        this.addTimer = HyperVaultObjective.FIGHT_ADD_PERIOD_TICKS;
        BlockPos center = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (center == null) {
            return;
        }
        RandomSource random = JavaRandom.ofNanoTime();
        ResourceLocation[] pool = random.nextBoolean() ? TANK_ADDS : ASSASSIN_ADDS;
        ResourceLocation id = pool[random.nextInt(pool.length)];
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(id);
        Entity created = type == null ? null : type.create(world);
        if (!(created instanceof Mob mob)) {
            WoldsVaults.LOGGER.error("Fight-add entity {} is missing or not a mob — skipping this add.", id);
            return;
        }
        double min = 6.0;
        double max = 12.0;
        for (int attempt = 0; attempt < 50; attempt++) {
            double angle = Math.PI * 2 * random.nextDouble();
            double distance = Math.sqrt(random.nextDouble() * (max * max - min * min) + min * min);
            int x = center.getX() + (int) Math.ceil(distance * Math.cos(angle));
            int z = center.getZ() + (int) Math.ceil(distance * Math.sin(angle));
            int y = center.getY() + random.nextInt(5) - 2;
            BlockPos ground = new BlockPos(x, y - 1, z);
            if (!world.getBlockState(ground).isValidSpawn(world, ground, mob.getType())) {
                continue;
            }
            AABB box = mob.getType().getAABB(x + 0.5, y, z + 0.5);
            if (!world.noCollision(box)) {
                continue;
            }
            mob.moveTo(x + 0.5, y + 0.2, z + 0.5, (float) (random.nextDouble() * 2.0 * Math.PI), 0.0F);
            mob.finalizeSpawn(world, new DifficultyInstance(Difficulty.PEACEFUL, 13000L, 0L, 0.0F), MobSpawnType.STRUCTURE, null, null);
            mob.setPersistenceRequired();
            mob.addTag(HyperVaultObjective.FIGHT_SPAWN_TAG);
            world.addWithUUID(mob);
            return;
        }
        mob.discard();
    }

    private void tickWaveTimer() {
        int remaining = objective.getOr(HyperVaultObjective.WAVE_TICK, HyperVaultObjective.WAVE_PERIOD_TICKS) - 1;
        if (remaining <= 0) {
            spawnBrutalWave("timed");
            remaining = HyperVaultObjective.WAVE_PERIOD_TICKS;
        }
        objective.set(HyperVaultObjective.WAVE_TICK, remaining);
    }

    private void tickHealthGates() {
        UUID bossId = objective.getOr(HyperVaultObjective.BOSS_ID, null);
        if (bossId == null) {
            return;
        }
        Entity entity = world.getEntity(bossId);
        if (!(entity instanceof LivingEntity boss) || !boss.isAlive() || boss.getMaxHealth() <= 0.0F) {
            return;
        }
        if (objective.getOr(HyperVaultObjective.SCORE, 0) == 0) {
            // First live sighting (traits are applied by now): give the boss its damage
            // escalation plus the vault's mob modifiers, then score the finished stats.
            applyBossStats(boss);
            double damage = boss.getAttribute(Attributes.ATTACK_DAMAGE) == null
                    ? 0.0 : boss.getAttributeValue(Attributes.ATTACK_DAMAGE);
            objective.set(HyperVaultObjective.SCORE,
                    Math.max(1, (int) Math.round((boss.getMaxHealth() + damage * 100.0) / 1000.0)));
        }
        float fraction = boss.getHealth() / boss.getMaxHealth();
        int mask = objective.getOr(HyperVaultObjective.GATE_MASK, 0);
        for (int i = 0; i < HyperVaultObjective.HEALTH_GATES.length; i++) {
            if (fraction <= HyperVaultObjective.HEALTH_GATES[i] && (mask & (1 << i)) == 0) {
                mask |= 1 << i;
                spawnBrutalWave((int) (HyperVaultObjective.HEALTH_GATES[i] * 100) + "% gate");
            }
        }
        objective.set(HyperVaultObjective.GATE_MASK, mask);
    }

    /**
     * Spawns 2-4 brutal bosses around the pillar. These are plain infernal brutal mobs — they are
     * never registered to any wave, so their deaths add no vault modifiers.
     */
    private void spawnBrutalWave(String reason) {
        BlockPos center = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (center == null) {
            WoldsVaults.LOGGER.warn("Hyper brutal wave ({}) skipped: no pillar position recorded.", reason);
            return;
        }
        RandomSource random = JavaRandom.ofNanoTime();
        int count = HyperVaultObjective.WAVE_MOB_MIN
                + random.nextInt(HyperVaultObjective.WAVE_MOB_MAX - HyperVaultObjective.WAVE_MOB_MIN + 1);
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            if (spawnAround(center, random)) {
                spawned++;
            }
        }
        if (spawned < count) {
            WoldsVaults.LOGGER.warn("Hyper brutal wave ({}): only {}/{} mobs found a valid spawn spot around {}.", reason, spawned, count, center);
        }
        if (spawned > 0) {
            HyperVaultObjective.broadcast(vault, "Brutal reinforcements have arrived!", ChatFormatting.RED);
        }
    }

    /** A fresh boss from the pillar's palette roster every cycle (repeats allowed). */
    private void rerollBoss(BossRunePillarTileEntity pillar) {
        // The roster is read from the tile's own saved NBT (the Config.writeNbt shape: a
        // "boss" list of PartialEntity tags with a "weight") instead of via an accessor on
        // the nested private-field Config — one less mixin that can fail to attach.
        ListTag entries = pillar.saveWithoutMetadata().getCompound("config").getList("boss", Tag.TAG_COMPOUND);
        WeightedList<PartialEntity> pool = new WeightedList<>();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            double weight = Adapters.DOUBLE.readNbt(entry.get("weight")).orElse(1.0);
            Adapters.PARTIAL_ENTITY.readNbt(entry).ifPresent(boss -> pool.add(boss, weight));
        }
        if (pool.isEmpty()) {
            WoldsVaults.LOGGER.warn("The boss pillar has no roster to reroll from; keeping {}.",
                    pillar.getBoss() == null ? "nothing" : pillar.getBoss().getId());
            return;
        }
        pool.getRandom(JavaRandom.ofNanoTime()).ifPresent(rolled -> {
            // No copy(): PartialEntity.copy NPEs on position-less templates (blockPos null),
            // and this pool was freshly parsed from NBT — the instance is exclusively ours.
            // (Vanilla onPopulate also assigns the rolled template directly, without copying.)
            ((BossRunePillarAccessor) pillar).setBoss(rolled);
            WoldsVaults.LOGGER.info("Hyperboss for this cycle: {}.", rolled.getId());
        });
    }

    /**
     * The percent damage escalation plus every mob attribute modifier on the vault, applied to
     * the live boss exactly as ENTITY_SPAWN applies them to normal mobs (the boss is
     * IModifierImmunity, so it never received them). Multiplicative health stacks therefore
     * compound on the boss the same way they compound on the brutals.
     */
    private void applyBossStats(LivingEntity boss) {
        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double damageEscalation = HyperVaultObjective.BOSS_DAMAGE_PERCENT
                * Math.pow(HyperVaultObjective.HYPER_STAT_FACTOR, cycle)
                + HyperVaultObjective.BOSS_STAT_INCREMENT * cycle;
        AttributeInstance damage = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null && damage.getModifier(HYPER_DAMAGE_UUID) == null) {
            damage.addPermanentModifier(new AttributeModifier(HYPER_DAMAGE_UUID,
                    "hyper_damage_escalation", damageEscalation, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        Modifiers vaultModifiers = vault.get(Vault.MODIFIERS);
        int applied = 0;
        for (Modifiers.Entry entry : vaultModifiers.getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            ModifierContext context = vaultModifiers.getContext(entry);
            // Max-health modifiers are folded into the health trait at arm time; the rest
            // (damage, speed, ...) act through real attribute modifiers here.
            if (modifier instanceof MobAttributeModifier mob
                    && !targetsMaxHealth(mob.properties().getType())) {
                mob.applyToEntity(boss, context.getUUID(), context);
                applied++;
            } else if (modifier instanceof MobAttributeModifierSettable settable
                    && !targetsMaxHealth(settable.properties().getType())) {
                settable.applyToEntity(boss, context.getUUID(), context);
                applied++;
            }
        }
        // The vault's speed modifiers were just applied for real; same +200% cap as every mob.
        if (HyperVaultObjective.clampMovementSpeed(boss)) {
            WoldsVaults.LOGGER.info("Hyperboss movement speed capped at +{}%.",
                    Math.round((HyperVaultObjective.SPEED_CAP_FACTOR - 1.0) * 100.0));
        }
        // Reaving's bonus damage (target max health × gear %) fires once per mob, gated by the
        // REAVING effect as its "already reaved" latch — pre-latching makes the hyperboss immune
        // to a proc that scales with its hyper-inflated health pool. Bleed (the other
        // %-max-health source) is denied outright in HyperBossEffectImmunity.
        boss.addEffect(new MobEffectInstance(xyz.iwolfking.woldsvaults.init.ModEffects.REAVING, Integer.MAX_VALUE, 0, true, false));
        boss.setHealth(boss.getMaxHealth());
        WoldsVaults.LOGGER.info(
                "Hyperboss stats: {} HP (vault health factor folded at arm), {} damage — {} non-health vault mob modifiers applied.",
                Math.round(boss.getMaxHealth()),
                damage == null ? "?" : Math.round(damage.getValue()), applied);
    }

    // The addon's settable modifiers carry their own ModifierType enum with the same shape as
    // VH's, hence the overload pair.
    private static boolean targetsMaxHealth(EntityAttributeModifier.ModifierType type) {
        return type != null && type.getAttributeResourceLocations().contains(MAX_HEALTH_ID);
    }

    private static boolean targetsMaxHealth(EntityAttributeModifierSettable.ModifierType type) {
        return type != null && type.getAttributeResourceLocations().contains(MAX_HEALTH_ID);
    }

    /**
     * How much bigger a normal mob's max health gets from the vault's modifiers:
     * (1 + sum of additive percents) x (product of 1 + each multiplicative percent).
     */
    private double vaultHealthFactor() {
        double additive = 0.0;
        double multiplicative = 1.0;
        for (Modifiers.Entry entry : vault.get(Vault.MODIFIERS).getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            AttributeModifier.Operation operation;
            double amount;
            if (modifier instanceof MobAttributeModifier mob
                    && targetsMaxHealth(mob.properties().getType())) {
                operation = mob.properties().getType().getAttributeModifierOperation();
                amount = mob.properties().getAmount();
            } else if (modifier instanceof MobAttributeModifierSettable settable
                    && targetsMaxHealth(settable.properties().getType())) {
                operation = settable.properties().getType().getAttributeModifierOperation();
                amount = settable.properties().getValue();
            } else {
                continue;
            }
            if (operation == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                multiplicative *= 1.0 + amount;
            } else if (operation == AttributeModifier.Operation.MULTIPLY_BASE) {
                additive += amount;
            }
            // plain ADDITION (flat half-hearts) is meaningless at boss scale; ignored
        }
        return (1.0 + additive) * multiplicative;
    }

    /** Mirrors ObeliskObjective.doSpawn's annulus placement, but with a bounded attempt count. */
    private boolean spawnAround(BlockPos center, RandomSource random) {
        double min = 6.0;
        double max = 12.0;
        for (int attempt = 0; attempt < 50; attempt++) {
            double angle = Math.PI * 2 * random.nextDouble();
            double distance = Math.sqrt(random.nextDouble() * (max * max - min * min) + min * min);
            int x = (int) Math.ceil(distance * Math.cos(angle));
            int z = (int) Math.ceil(distance * Math.sin(angle));
            int y = random.nextInt(5) - 2;
            LivingEntity spawned = BrutalBossesObjective.spawnMob(world, vault, center.getX() + x, center.getY() + y, center.getZ() + z, random);
            if (spawned != null) {
                spawned.addTag(HyperVaultObjective.FIGHT_SPAWN_TAG);
                return true;
            }
        }
        return false;
    }
}
