package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.VaultMod;
import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import iskallia.vault.core.world.data.entity.PartialEntity;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.storage.WorldZones;
import iskallia.vault.entity.boss.BossRuneModifiers;
import iskallia.vault.world.data.WorldZonesData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BossRunePillarAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BossRunePillarConfigAccessor;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.MobAttributeModifierSettable;
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

        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double escalationFactor = Math.pow(HyperVaultObjective.HYPER_STAT_FACTOR, cycle);
        // Health units are fractions of base (0.5 = +50%) landing in the boss's MULTIPLY_BASE
        // health trait; trait stacks SUM, so the per-cycle ×1.75 compounding is folded in here.
        // Damage escalation deliberately stays out: the boss's damage trait applies amounts as
        // FLAT damage (operator "add" in vault_boss.json), so the percent escalation is added
        // as a real attribute modifier in applyBossStats — together with the vault's mob
        // modifiers, which the modifier-immune boss never received on spawn.
        BossRuneModifiers armed = new BossRuneModifiers(
                HyperVaultObjective.BOSS_HEALTH_PERCENT * escalationFactor,
                0.0,
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
        BossRunePillarTileEntity.Config config = ((BossRunePillarAccessor) pillar).getConfig();
        WeightedList<PartialEntity> pool = ((BossRunePillarConfigAccessor) (Object) config).getBossPool();
        if (pool == null || pool.isEmpty()) {
            WoldsVaults.LOGGER.warn("The boss pillar has no roster to reroll from; keeping {}.",
                    pillar.getBoss() == null ? "nothing" : pillar.getBoss().getId());
            return;
        }
        pool.getRandom(JavaRandom.ofNanoTime()).ifPresent(rolled -> {
            ((BossRunePillarAccessor) pillar).setBoss(rolled.copy());
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
        double traitHealth = boss.getMaxHealth();
        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double damageEscalation = HyperVaultObjective.BOSS_DAMAGE_PERCENT
                * Math.pow(HyperVaultObjective.HYPER_STAT_FACTOR, cycle);
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
            if (modifier instanceof MobAttributeModifier mob) {
                mob.applyToEntity(boss, context.getUUID(), context);
                applied++;
            } else if (modifier instanceof MobAttributeModifierSettable settable) {
                settable.applyToEntity(boss, context.getUUID(), context);
                applied++;
            }
        }
        boss.setHealth(boss.getMaxHealth());
        WoldsVaults.LOGGER.info(
                "Hyperboss stats: {} HP ({} from traits before vault modifiers), {} damage — {} vault mob modifiers applied like a normal mob.",
                Math.round(boss.getMaxHealth()), Math.round(traitHealth),
                damage == null ? "?" : Math.round(damage.getValue()), applied);
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
