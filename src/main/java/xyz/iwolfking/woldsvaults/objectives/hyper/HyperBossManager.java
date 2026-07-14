package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.VaultMod;
import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.EntityAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.rune.RuneBossFight;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.data.entity.PartialEntity;
import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.storage.WorldZones;
import iskallia.vault.entity.boss.BossRuneModifiers;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.world.data.WorldZonesData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BossRunePillarAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BossRunePillarConfigAccessor;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.MobAttributeModifierSettable;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.lib.EntityAttributeModifierSettable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.Phase;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Drives the hyperboss cycle: arms the pillar with escalated stats, runs the periodic and
 * health-gated brutal waves, and hands a finished fight over to the escalation manager.
 *
 * <p>FRAGILITY NOTE: arming reuses RuneBossFight/BossRunePillarTileEntity in a loop those
 * classes were never designed for (pillar NBT snapshot/restore, per-fight zone recreation,
 * gate-surround repair). This works against the current base-mod internals and degrades
 * with a logged warning where it can, but it is the first place to re-verify after any
 * Vault Hunters update that touches the rune-boss machinery.
 */
public class HyperBossManager extends ObjectiveManager<HyperVaultObjective> {
    /**
     * Arena adds: hostile picks from the config's the_vault:tank / the_vault:assassin entity
     * groups (the groups config only exposes match-predicates, so the spawnable subset is
     * curated here, same as BrutalBossesRegistry does for its bosses).
     */
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

    /** Stable id for the boss's percent damage-escalation modifier (idempotent across reloads). */
    private static final UUID HYPER_DAMAGE_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:hyper_damage_escalation".getBytes(StandardCharsets.UTF_8));
    /** Stable id for the multiplayer health bonus (idempotent across reloads). */
    private static final UUID MULTIPLAYER_HEALTH_UUID =
            UUID.nameUUIDFromBytes("woldsvaults:hyper_multiplayer_health".getBytes(StandardCharsets.UTF_8));
    /**
     * The health_attribute trait's baseValue in vault_boss.json (the boss's innate +50%);
     * part of the reference total the vault health factor multiplies.
     */
    private static final double INNATE_HEALTH_BONUS = 0.5;
    private static final ResourceLocation MAX_HEALTH_ID = ResourceLocation.parse("generic.max_health");
    /** The four arena gates of the BOSS_1 room style, relative to the pillar (RuneBossAnimation). */
    private static final BlockPos[] GATE_OFFSETS = {
            new BlockPos(23, 4, 0), new BlockPos(-23, 4, 0),
            new BlockPos(0, 4, 23), new BlockPos(0, 4, -23)};

    /**
     * How long the arena must stay empty of living fighters before the fight counts as wiped
     * (debounces death screens, brief dimension-change resolution gaps, etc.).
     */
    private static final int WIPE_GRACE_TICKS = 60;

    private final HyperEscalationManager escalation;
    /** Transient on purpose: a reload mid-fight just restarts the short add countdown. */
    private int addTimer = HyperVaultObjective.cfg().getFightAddPeriodTicks();
    /** Transient on purpose: a reload mid-countdown just restarts the wipe grace window. */
    private int wipeGraceTicks = WIPE_GRACE_TICKS;
    private String lastRoster = "";

    public HyperBossManager(Vault vault, VirtualWorld world, HyperVaultObjective objective, HyperEscalationManager escalation) {
        super(vault, world, objective);
        this.escalation = escalation;
    }

    /**
     * Empty-hand podium click during ARMED: escalate the pillar's stats and start a fresh
     * fight. Deliberately reachable without sneaking — vanilla skips BlockBehaviour.use
     * entirely for a sneaking player holding an item in either hand, so a sneak requirement
     * would eat clicks made with a totem in the off-hand.
     *
     * <p>The pillar tile is snapshotted before the fight consumes it (the escalation manager
     * re-places it next cycle), and the protection zone is ensured BEFORE the ability/stat
     * writes — the tile's onLoad refreshes ability modifiers and would resurrect the revive
     * ability after setReviveAbility(null). The boss's health is one giant MULTIPLY_BASE trait
     * term, so vault mob-health modifiers applied as attribute modifiers would dilute to noise
     * next to the escalation; instead the vault's health factor multiplies the whole escalated
     * total here, before the traits are built at summon. Damage stays out of the trait (flat
     * "add" operator) and is applied with the non-health modifiers in applyBossStats.
     */
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
        objective.set(HyperVaultObjective.PILLAR_NBT, pillar.saveWithoutMetadata());
        ensureProtectionZone(pillar, pillarPos);
        snapshotOrRepairGates(pillarPos);

        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double escalation = HyperVaultObjective.cfg().getBossHealthPercent()
                * Math.pow(HyperVaultObjective.cfg().getHyperStatFactor(), cycle)
                + HyperVaultObjective.cfg().getBossStatIncrement() * cycle;
        double healthFactor = vaultHealthFactor();
        double healthPercent = (1.0 + INNATE_HEALTH_BONUS + escalation) * healthFactor
                - 1.0 - INNATE_HEALTH_BONUS;
        if (healthFactor > 1.0) {
            WoldsVaults.LOGGER.info("Hyperboss health inherits the vault's mob modifiers: x{}.",
                    Math.round(healthFactor * 100.0) / 100.0);
        }
        BossRuneModifiers armed = new BossRuneModifiers(healthPercent, 0.0,
                HyperVaultObjective.cfg().getBossAbilityHaste());
        pillar.getModifiers().copyFrom(armed);
        pillar.setRuneCount(Math.min(HyperVaultObjective.cfg().getBaseRuneTier() + cycle, HyperVaultObjective.cfg().getRuneTierCap()));
        pillar.getModifiers().setReviveAbility(null);

        objective.set(HyperVaultObjective.SCORE, 0);
        fights.add(pillar.createFight());
        objective.set(HyperVaultObjective.PHASE, Phase.FIGHT);
        objective.set(HyperVaultObjective.WAVE_TICK, HyperVaultObjective.cfg().getWavePeriodTicks());
        this.addTimer = HyperVaultObjective.cfg().getFightAddPeriodTicks();
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
    private void forEachGatePos(BlockPos pillarPos, Consumer<BlockPos> action) {
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
     * reused so the zone box and flags stay exactly vanilla; a stale id from a previous cycle
     * (that zone was removed on kill) is reset so the tile registers a fresh one.
     */
    private void ensureProtectionZone(BossRunePillarTileEntity pillar, BlockPos pillarPos) {
        BossRunePillarAccessor access = (BossRunePillarAccessor) pillar;
        WorldZones zones = WorldZonesData.get(world.getServer()).getOrCreate(world.dimension());
        int zoneId = access.getZoneId();
        if (zoneId <= 0 || zones.get(zoneId).isEmpty()) {
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

    /**
     * Drives the FIGHT phase. No pending fight counts as a kill — the machinery also completes
     * that way if the boss entity vanishes on a reload edge, indistinguishable from a kill here.
     */
    @Override
    public void tick() {
        if (objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING) != Phase.FIGHT) {
            return;
        }
        RuneBossFights fights = objective.get(HyperVaultObjective.FIGHTS);
        if (!fights.hasPendingFight()) {
            escalation.onBossKilled();
            return;
        }

        if (checkFightWipe(fights)) {
            return;
        }

        tickWaveTimer();
        tickHealthGates();
        tickFightAdds();
        tickBossResistance();
    }

    /**
     * While any spawned reinforcement (wave brutal or arena add) lives, the boss holds
     * Resistance III. Checked once a second with a 25-tick effect so the buff bridges to the
     * next check without per-tick effect spam.
     */
    private void tickBossResistance() {
        if (world.getTickCount() % 20 != 0) {
            return;
        }
        UUID bossId = objective.getOr(HyperVaultObjective.BOSS_ID, null);
        if (bossId == null || !(world.getEntity(bossId) instanceof LivingEntity boss) || !boss.isAlive()) {
            return;
        }
        for (Entity entity : world.getAllEntities()) {
            if (entity instanceof LivingEntity living && living.isAlive()
                    && entity.getTags().contains(HyperVaultObjective.FIGHT_SPAWN_TAG)) {
                boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 25, 2, true, false));
                return;
            }
        }
    }

    /**
     * Death removes a runner from the vault, so a fight whose whole arena roster is gone can
     * never end on its own: the boss would idle in the sealed room while RuneBossFights keeps
     * the vault clock paused, soft-locking every survivor outside. Instead the fight winds
     * back to the armed pillar — same cycle, same stats, no rewards — so the rest of the
     * party can attempt it again. The roster is the fight's own zone-tracked player set;
     * offline participants hold the fight open (they log back in inside the arena), dead and
     * spectating ones do not. While the boss is not yet summoned or already dying, the kill
     * path owns the state — a simultaneous full-party death stays a win.
     */
    private boolean checkFightWipe(RuneBossFights fights) {
        RuneBossFight fight = activeFight(fights);
        UUID bossId = objective.getOr(HyperVaultObjective.BOSS_ID, null);
        Entity boss = bossId == null ? null : world.getEntity(bossId);
        if (fight == null || !(boss instanceof LivingEntity living) || !living.isAlive()) {
            this.wipeGraceTicks = WIPE_GRACE_TICKS;
            return false;
        }
        logRosterChanges(fight);
        if (hasLivingFighter(fight)) {
            this.wipeGraceTicks = WIPE_GRACE_TICKS;
            return false;
        }
        if (--this.wipeGraceTicks > 0) {
            return false;
        }
        this.wipeGraceTicks = WIPE_GRACE_TICKS;
        WoldsVaults.LOGGER.info(
                "Hyperboss fight wiped: no living fighter left in the arena for {} ticks. Discarding the boss and re-arming the pillar (cycle unchanged).",
                WIPE_GRACE_TICKS);
        boss.discard();
        escalation.onFightWiped();
        return true;
    }

    /** The non-completed fight at the recorded pillar, if the machinery has attached it yet. */
    private RuneBossFight activeFight(RuneBossFights fights) {
        BlockPos pillarPos = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (pillarPos == null) {
            return null;
        }
        for (RuneBossFight fight : fights.getFights()) {
            if (!fight.isCompleted() && pillarPos.equals(fight.getOrigin())) {
                return fight;
            }
        }
        return null;
    }

    /**
     * True while any fight participant is alive in the arena; offline participants count as
     * living (they rejoin mid-fight).
     */
    private boolean hasLivingFighter(RuneBossFight fight) {
        for (UUID uuid : fight.getPlayers()) {
            ServerPlayer player = world.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) {
                return true;
            }
            if (player.level == world && player.isAlive() && !player.isSpectator()) {
                return true;
            }
        }
        return false;
    }

    /** One log line whenever the arena roster changes, so multiplayer reports are auditable. */
    private void logRosterChanges(RuneBossFight fight) {
        List<String> names = new ArrayList<>();
        for (UUID uuid : fight.getPlayers()) {
            ServerPlayer player = world.getServer().getPlayerList().getPlayer(uuid);
            names.add(player == null ? uuid + " (offline)" : player.getGameProfile().getName());
        }
        Collections.sort(names);
        String roster = String.join(", ", names);
        if (!roster.equals(this.lastRoster)) {
            WoldsVaults.LOGGER.info("Hyperboss arena roster: [{}]", roster);
            this.lastRoster = roster;
        }
    }

    /** A lone tank or assassin joins the arena every few seconds while the boss lives. */
    private void tickFightAdds() {
        if (--this.addTimer > 0) {
            return;
        }
        this.addTimer = HyperVaultObjective.cfg().getFightAddPeriodTicks();
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
        int remaining = objective.getOr(HyperVaultObjective.WAVE_TICK, HyperVaultObjective.cfg().getWavePeriodTicks()) - 1;
        if (remaining <= 0) {
            spawnBrutalWave("timed");
            remaining = HyperVaultObjective.cfg().getWavePeriodTicks();
        }
        objective.set(HyperVaultObjective.WAVE_TICK, remaining);
    }

    /**
     * Fires a brutal wave at each configured health fraction. On the boss's first live
     * sighting (traits are applied by then) it also applies the damage escalation plus the
     * vault's mob modifiers, then scores the finished stats normalized to the boogeyman
     * reference — clamped, because deep-cycle scores overflow int — and only AFTER the score
     * capture applies the multiplayer health scale, so extra players never inflate the
     * loot-escalation score.
     */
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
            applyBossStats(boss);
            AttributeInstance health = boss.getAttribute(Attributes.MAX_HEALTH);
            AttributeInstance damage = boss.getAttribute(Attributes.ATTACK_DAMAGE);
            double healthMultiplier = health == null || health.getBaseValue() <= 0.0
                    ? 1.0 : boss.getMaxHealth() / health.getBaseValue();
            double damageMultiplier = damage == null || damage.getBaseValue() <= 0.0
                    ? 0.0 : damage.getValue() / damage.getBaseValue();
            long score = Math.round((healthMultiplier * HyperVaultObjective.cfg().getReferenceBossHealth()
                    + damageMultiplier * 100.0 * HyperVaultObjective.cfg().getReferenceBossDamage()) / 1000.0);
            objective.set(HyperVaultObjective.SCORE,
                    (int) Math.max(1L, Math.min(Integer.MAX_VALUE, score)));
            applyMultiplayerHealthScale(boss);
        }
        float fraction = boss.getHealth() / boss.getMaxHealth();
        float[] gates = HyperVaultObjective.cfg().getHealthGates();
        int mask = objective.getOr(HyperVaultObjective.GATE_MASK, 0);
        for (int i = 0; i < gates.length && i < 31; i++) {
            if (fraction <= gates[i] && (mask & (1 << i)) == 0) {
                mask |= 1 << i;
                spawnBrutalWave((int) (gates[i] * 100) + "% gate");
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
        int count = HyperVaultObjective.cfg().getWaveMobMin()
                + random.nextInt(HyperVaultObjective.cfg().getWaveMobMax() - HyperVaultObjective.cfg().getWaveMobMin() + 1);
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

    /**
     * A fresh boss from the pillar's palette roster every cycle (repeats allowed). The roster
     * is read through the typed config accessor — no NBT-shape parsing, so a base-mod save
     * format change breaks loudly at mixin apply instead of silently at runtime. The rolled
     * template is assigned WITHOUT copy(): PartialEntity.copy NPEs on position-less templates,
     * and vanilla onPopulate assigns the rolled template directly the same way.
     */
    private void rerollBoss(BossRunePillarTileEntity pillar) {
        WeightedList<PartialEntity> pool =
                ((BossRunePillarConfigAccessor) (Object) ((BossRunePillarAccessor) pillar).getConfig()).getBossPool();
        if (pool == null || pool.isEmpty()) {
            WoldsVaults.LOGGER.warn("The boss pillar has no roster to reroll from; keeping {}.",
                    pillar.getBoss() == null ? "nothing" : pillar.getBoss().getId());
            return;
        }
        pool.getRandom(JavaRandom.ofNanoTime()).ifPresent(rolled -> {
            ((BossRunePillarAccessor) pillar).setBoss(rolled);
            WoldsVaults.LOGGER.info("Hyperboss for this cycle: {}.", rolled.getId());
        });
    }

    /**
     * The percent damage escalation plus every mob attribute modifier on the vault, applied to
     * the live boss exactly as ENTITY_SPAWN applies them to normal mobs (the boss is
     * IModifierImmunity, so it never received them). Multiplicative health stacks therefore
     * compound on the boss the same way they compound on the brutals. Max-health modifiers are
     * excluded — they were folded into the health trait at arm time. The REAVING effect is
     * pre-applied as its own once-per-mob latch, so Reaving's %-max-health proc can never fire
     * on the hyper-inflated pool (Bleed, the other source, is denied in HyperVaultEvents), and
     * the hyperboss must NEVER be given InfernalMobs modifiers — InfernalMobs rewrites max
     * health generically and produced a 100B-HP cycle-1 boss when tried.
     */
    private void applyBossStats(LivingEntity boss) {
        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double damageEscalation = HyperVaultObjective.cfg().getBossDamagePercent()
                * Math.pow(HyperVaultObjective.cfg().getHyperStatFactor(), cycle)
                + HyperVaultObjective.cfg().getBossStatIncrement() * cycle;
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
        if (HyperVaultObjective.clampMovementSpeed(boss)) {
            WoldsVaults.LOGGER.info("Hyperboss movement speed capped at +{}%.",
                    Math.round((HyperVaultObjective.cfg().getSpeedCapFactor() - 1.0) * 100.0));
        }
        boss.addEffect(new MobEffectInstance(ModEffects.REAVING, Integer.MAX_VALUE, 0, true, false));
        boss.setHealth(boss.getMaxHealth());
        WoldsVaults.LOGGER.info(
                "Hyperboss stats: {} HP (vault health factor folded at arm), {} damage — {} non-health vault mob modifiers applied.",
                Math.round(boss.getMaxHealth()),
                damage == null ? "?" : Math.round(damage.getValue()), applied);
        logDamageAmplifierAudit();
    }

    /**
     * +50% of the boss's finished max health per EXTRA runner, counted once when the boss
     * first ticks. MULTIPLY_TOTAL, so it multiplies the final value after the arm-time health
     * trait and every vault modifier — solo stays untouched, a duo fights x1.5, a trio x2.0.
     */
    private void applyMultiplayerHealthScale(LivingEntity boss) {
        int runners = vault.get(Vault.LISTENERS).getAll(Runner.class).size();
        int extra = Math.max(0, runners - 1);
        if (extra == 0) {
            return;
        }
        AttributeInstance health = boss.getAttribute(Attributes.MAX_HEALTH);
        if (health == null || health.getModifier(MULTIPLAYER_HEALTH_UUID) != null) {
            return;
        }
        double bonus = HyperVaultObjective.cfg().getPlayerScaleBossHealth() * extra;
        health.addPermanentModifier(new AttributeModifier(MULTIPLAYER_HEALTH_UUID,
                "hyper_multiplayer_health", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        boss.setHealth(boss.getMaxHealth());
        WoldsVaults.LOGGER.info("Hyperboss max health x{} for {} runners (+{}% per extra player): {} HP.",
                1.0 + bonus, runners,
                Math.round(HyperVaultObjective.cfg().getPlayerScaleBossHealth() * 100.0),
                Math.round(boss.getMaxHealth()));
    }

    /**
     * One audit line per fight: the vault-wide player-damage multipliers (the Frenzy-family
     * rework in MixinMobFrenzyModifier multiplies ALL player-dealt damage by 3x/2x PER STACK)
     * and each runner's %-scaling damage gear, so the hurt-chain log lines can be attributed.
     */
    private void logDamageAmplifierAudit() {
        if (!WoldsVaultsConfig.COMMON.enableDebugMode.get()) {
            return;
        }
        long frenzy = VaultModifierUtils.getCountOfModifiers(vault, ResourceLocation.parse("the_vault:frenzy"));
        long brew = VaultModifierUtils.getCountOfModifiers(vault, ResourceLocation.parse("the_vault:catastrophic_brew"));
        WoldsVaults.LOGGER.info(
                "Damage-amplifier audit: {} Frenzy (+200% each) + {} Catastrophic Brew (+100% each) stacks -> all player damage x{} (additive per modifier in hyper).",
                frenzy, brew, String.format("%.0f", (1.0 + 2.0 * frenzy) * (1.0 + 1.0 * brew)));
        for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
            listener.getPlayer().ifPresent(player -> {
                var snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
                var merger = VaultGearAttributeTypeMerger.floatSum();
                WoldsVaults.LOGGER.info(
                        "  {} gear: reaving={} execution={} apScaling={} thornsScaling={}",
                        player.getGameProfile().getName(),
                        snapshot.getAttributeValue(ModGearAttributes.REAVING_DAMAGE, merger),
                        snapshot.getAttributeValue(ModGearAttributes.EXECUTION_DAMAGE, merger),
                        snapshot.getAttributeValue(ModGearAttributes.AP_SCALING_DAMAGE, merger),
                        snapshot.getAttributeValue(ModGearAttributes.THORNS_SCALING_DAMAGE, merger));
            });
        }
    }

    /**
     * The addon's settable modifiers carry their own ModifierType enum with the same shape as
     * VH's, hence the overload pair.
     */
    private static boolean targetsMaxHealth(EntityAttributeModifier.ModifierType type) {
        return type != null && type.getAttributeResourceLocations().contains(MAX_HEALTH_ID);
    }

    private static boolean targetsMaxHealth(EntityAttributeModifierSettable.ModifierType type) {
        return type != null && type.getAttributeResourceLocations().contains(MAX_HEALTH_ID);
    }

    /**
     * How much bigger a normal mob's max health gets from the vault's modifiers:
     * (1 + sum of additive percents) x (product of 1 + each multiplicative percent).
     * Plain ADDITION (flat half-hearts) is ignored — meaningless at boss scale.
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
