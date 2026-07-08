package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.entity.boss.BossRuneModifiers;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.Phase;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

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
            new ResourceLocation("the_vault", "shiver"),
            new ResourceLocation("the_vault", "deathcap"),
            new ResourceLocation("the_vault", "blood_tank"),
            new ResourceLocation("the_vault", "overgrown_tank"),
            new ResourceLocation("the_vault", "pirate_guardian_tank"),
            new ResourceLocation("the_vault", "craftenstein"),
            new ResourceLocation("the_vault", "yeti"),
            new ResourceLocation("the_vault", "deep_dark_horror"),
            new ResourceLocation("minecraft", "piglin_brute"),
            new ResourceLocation("woldsvaults", "haturkin"),
    };
    private static final ResourceLocation[] ASSASSIN_ADDS = {
            new ResourceLocation("the_vault", "vault_spider"),
            new ResourceLocation("the_vault", "t3_skeleton"),
            new ResourceLocation("the_vault", "t3_stray"),
            new ResourceLocation("the_vault", "t3_wither_skeleton"),
            new ResourceLocation("the_vault", "t3_creeper"),
            new ResourceLocation("the_vault", "t3_enderman"),
            new ResourceLocation("the_vault", "blood_slime"),
            new ResourceLocation("the_vault", "vault_wraith_white"),
            new ResourceLocation("the_vault", "vault_wraith_yellow"),
            new ResourceLocation("the_vault", "grimwick"),
            new ResourceLocation("the_vault", "winter_wolf"),
            new ResourceLocation("woldsvaults", "black_ghost"),
            new ResourceLocation("woldsvaults", "blue_ghost"),
    };

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
        objective.set(HyperVaultObjective.PILLAR_POS, pillarPos);

        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double escalationFactor = Math.pow(HyperVaultObjective.HYPER_STAT_FACTOR, cycle);
        // Units are fractions of base (0.5 = +50%); trait stacks SUM, so the per-cycle ×1.75
        // compounding is folded in here rather than by re-stacking modifiers.
        BossRuneModifiers armed = new BossRuneModifiers(
                HyperVaultObjective.BOSS_HEALTH_PERCENT * escalationFactor,
                HyperVaultObjective.BOSS_DAMAGE_PERCENT * escalationFactor,
                HyperVaultObjective.BOSS_ABILITY_HASTE);
        pillar.getModifiers().copyFrom(armed);
        // Rune count keys the shield/waveblast/revive settings table; capped so an absurd cycle
        // count cannot index past the config.
        pillar.setRuneCount(Math.min(HyperVaultObjective.BASE_RUNE_TIER + cycle, HyperVaultObjective.RUNE_TIER_CAP));
        pillar.getModifiers().setReviveAbility(null); // the hyperboss never heals or revives

        fights.add(pillar.createFight());
        objective.set(HyperVaultObjective.PHASE, Phase.FIGHT);
        objective.set(HyperVaultObjective.WAVE_TICK, HyperVaultObjective.WAVE_PERIOD_TICKS);
        this.addTimer = HyperVaultObjective.FIGHT_ADD_PERIOD_TICKS;
        // BOSS_ID intentionally keeps its previous value until the new boss spawns: gate checks
        // resolve a dead entity to null and skip, and ENTITY_SPAWN overwrites it on summon.
        objective.set(HyperVaultObjective.GATE_MASK, 0);
        HyperVaultObjective.broadcast(vault, "The Hyperboss awakens!", ChatFormatting.DARK_RED);
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
            if (BrutalBossesObjective.spawnMob(world, vault, center.getX() + x, center.getY() + y, center.getZ() + z, random) != null) {
                return true;
            }
        }
        return false;
    }
}
