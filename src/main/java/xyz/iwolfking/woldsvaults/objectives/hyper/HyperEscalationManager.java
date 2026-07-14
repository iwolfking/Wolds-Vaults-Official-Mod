package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.VaultMod;
import iskallia.vault.block.ObeliskBlock;
import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.rune.RuneBossAnimation;
import iskallia.vault.core.vault.objective.rune.RuneBossFight;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.WorldZonesData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.Phase;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything that happens because a hyperboss died: the HYPER stack, crate tiers, the chaos
 * modifier dump, and the timed exit pillar. Also runs the periodic negative ambient events.
 *
 * <p>FRAGILITY NOTE: the pillar respawn, per-fight zone lifecycle and door-animation replay
 * in this manager (and in HyperBossManager) deliberately invert RuneBossFight's own
 * single-fight, vault-ends-on-kill assumptions. They are the most base-mod-shape-coupled
 * part of hyper — re-verify them first after any Vault Hunters update that touches
 * BossRunePillarTileEntity, RuneBossFight or RuneBossAnimation.
 */
public class HyperEscalationManager extends ObjectiveManager<HyperVaultObjective> {
    private final HyperCycleManager cycleManager;
    /**
     * The arena gates. A rune fight freezes its own OPEN_ROOM animation on the first frame once it
     * completes (invisible in normal rune vaults — the run ends right after), so the reopening is
     * driven from here instead. Transient: a reload mid-animation just replays the 5s opening.
     */
    private RuneBossAnimation doorAnimation;

    public HyperEscalationManager(Vault vault, VirtualWorld world, HyperVaultObjective objective, HyperCycleManager cycleManager) {
        super(vault, world, objective);
        this.cycleManager = cycleManager;
    }

    public void restartDoorAnimation() {
        this.doorAnimation = new RuneBossAnimation();
        this.doorAnimation.onStart(RuneBossAnimation.State.OPEN_ROOM);
    }

    /**
     * Everything a hyperboss kill grants. One HYPER stack (the icon count reads as the kill
     * tally plus the crystal's entry marker); greedy crate tiers 1/2/3 by score, paid out as
     * extra greed coins per stack by MixinRunner's crate injection; crate scaling on a
     * deliberately quadratic ramp (kill k grants 5k tiers below 250k score, then 7k/9k/15k
     * from 250k/1.5M/25M) using ONLY plain crate_tier stacks — never the settable
     * map_crate_quantity, whose onVaultAdd consumes percentage-points as a raw fraction (the
     * +10,000% unit bug this schedule replaced); k Refined Experience at kill k; and one Inert
     * every second kill (banned from every pool, so this deterministic drip is its only
     * source).
     */
    public void onBossKilled() {
        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0) + 1;
        objective.set(HyperVaultObjective.CYCLE, cycle);

        VaultModifierUtils.addModifier(vault, WoldsVaults.id("hyper"), 1);
        int score = objective.getOr(HyperVaultObjective.SCORE, 0);
        int greedyTiers = score >= HyperVaultObjective.cfg().getScoreTripleGreedy() ? 3
                : score >= HyperVaultObjective.cfg().getScoreDoubleGreedy() ? 2 : 1;
        VaultModifierUtils.addModifier(vault, WoldsVaults.id("greedy_crate_tier"), greedyTiers);

        int crateTiers = (score >= HyperVaultObjective.cfg().getCrateTierScore15() ? 15
                : score >= HyperVaultObjective.cfg().getCrateTierScore9() ? 9
                : score >= HyperVaultObjective.cfg().getCrateTierScore7() ? 7 : 5) * cycle;
        VaultModifierUtils.addModifier(vault, VaultMod.id("crate_tier"), crateTiers);
        VaultModifierUtils.addModifier(vault, VaultMod.id("refined_experience"), cycle);
        if (cycle % 2 == 0) {
            VaultModifierUtils.addModifier(vault, VaultMod.id("inert"), 1);
            HyperVaultObjective.broadcast(vault, "Inert seeps into the Vault (-10% cooldown reduction).", ChatFormatting.DARK_PURPLE);
        }

        awardScoreTiers();

        dumpChaosModifiers();
        restartDoorAnimation();
        spawnExitPillar();
        respawnBossPillar();
        removeBossRoomZone();
        discardFightSpawns();

        objective.set(HyperVaultObjective.EXIT_TICKS, HyperVaultObjective.cfg().getExitPillarTicks());
        objective.set(HyperVaultObjective.PHASE, Phase.REWARD);
        String tierSummary = "+" + crateTiers + " crate tiers"
                + ", +" + greedyTiers + " greedy crate tier" + (greedyTiers > 1 ? "s" : "")
                + ", +" + cycle + " refined experience";
        HyperVaultObjective.broadcast(vault, "HYPER ×" + cycle + " — everything in the Vault grows stronger! (" + tierSummary + ")", ChatFormatting.GOLD);
    }

    /**
     * Every arena participant died (death removes them from the vault), so the fight could
     * never end on its own. Wind back to the armed pillar for the surviving party members:
     * no cycle advance, no rewards, no chaos dump — re-arming the podium rerolls a boss at
     * the identical cycle stats. The boss entity itself was already discarded by the caller
     * (a discard is not a death: no loot fires, and the fight machinery completes itself
     * once it notices the entity is gone, which reopens the podium for arming).
     */
    public void onFightWiped() {
        restartDoorAnimation();
        respawnBossPillar();
        removeBossRoomZone();
        discardFightSpawns();
        objective.set(HyperVaultObjective.PHASE, Phase.ARMED);
        HyperVaultObjective.broadcast(vault,
                "Everyone challenging the Hyperboss has fallen! The arena reopens — the pillar stands armed for another attempt.",
                ChatFormatting.RED);
    }

    /**
     * The just-killed boss's score gates crate reward injections: every met threshold adds one
     * tier marker (non-exclusive), and each marker stack rolls its pool into the completion
     * crate at award time (see HyperCrateRewards / MixinRunner).
     */
    private void awardScoreTiers() {
        int score = objective.getOr(HyperVaultObjective.SCORE, 0);
        boolean extraDraw = score >= HyperVaultObjective.cfg().getScoreExtraDraw();
        StringBuilder earned = new StringBuilder();
        if (score >= HyperVaultObjective.cfg().getScoreRare()) {
            VaultModifierUtils.addModifier(vault, HyperCrateRewards.RARE_MODIFIER, 1);
            earned.append("Rare");
        }
        if (score >= HyperVaultObjective.cfg().getScoreEpic()) {
            VaultModifierUtils.addModifier(vault, HyperCrateRewards.EPIC_MODIFIER, 1);
            earned.append(earned.isEmpty() ? "" : " + ").append("Epic");
            if (extraDraw) {
                objective.set(HyperVaultObjective.EPIC_PLUS, objective.getOr(HyperVaultObjective.EPIC_PLUS, 0) + 1);
            }
        }
        if (score >= HyperVaultObjective.cfg().getScoreOmega()) {
            VaultModifierUtils.addModifier(vault, HyperCrateRewards.OMEGA_MODIFIER, 1);
            earned.append(earned.isEmpty() ? "" : " + ").append("Omega");
            if (extraDraw) {
                objective.set(HyperVaultObjective.OMEGA_PLUS, objective.getOr(HyperVaultObjective.OMEGA_PLUS, 0) + 1);
            }
        }
        if (earned.isEmpty()) {
            return;
        }
        HyperVaultObjective.broadcast(vault,
                "Score " + score + " — " + earned + " rewards added to the completion crate!"
                        + (extraDraw ? " (empowered: +1 draw)" : ""), ChatFormatting.AQUA);
    }

    /** Fills the granted chaos budget from hyper_mixed; one pull normally rolls the pool's full 25. */
    private void dumpChaosModifiers() {
        int granted = HyperVaultObjective.consumeChaosBudget(vault, HyperVaultObjective.cfg().getChaosPerKill());
        if (granted <= 0) {
            HyperVaultObjective.broadcast(vault, "The Vault can hold no more chaos (" + HyperVaultObjective.cfg().getChaosCap() + " cap reached).", ChatFormatting.DARK_PURPLE);
            return;
        }
        HyperVaultObjective.broadcast(vault, "Chaotic modifiers surge into the Vault!", ChatFormatting.DARK_PURPLE);
        JavaRandom random = JavaRandom.ofNanoTime();
        int added = 0;
        int emptyPulls = 0;
        while (added < granted && emptyPulls < 2) {
            List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(HyperVaultObjective.CHAOS_POOL_MIXED, level, random);
            if (modifiers.isEmpty()) {
                WoldsVaults.LOGGER.error("Chaos modifier pool {} is missing/empty!", HyperVaultObjective.CHAOS_POOL_MIXED);
                emptyPulls++;
                continue;
            }
            emptyPulls = 0;
            for (VaultModifier<?> modifier : modifiers) {
                if (added >= granted) {
                    break;
                }
                if (HyperModifierPolicy.isStackCapped(vault, modifier)) {
                    continue;
                }
                vault.get(Vault.MODIFIERS).addModifier(modifier, 1, true, ChunkRandom.ofNanoTime());
                announceModifier(modifier);
                added++;
            }
        }
        if (added < granted) {
            WoldsVaults.LOGGER.error("Chaos dump only added {}/{} modifiers — pool {} came up empty.", added, granted, HyperVaultObjective.CHAOS_POOL_MIXED);
        }
    }

    private void announceModifier(VaultModifier<?> modifier) {
        for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
            listener.getPlayer().ifPresent(player ->
                    player.displayClientMessage(modifier.getChatDisplayNameComponent(1).copy().append(" was added to The Vault!"), false));
        }
    }

    /**
     * The opening chaos dump fires once, on the first tick a player is actually inside —
     * initServer runs before anyone joins, and the chat lines would be lost.
     */
    @Override
    public void tick() {
        tickDoorAnimation();
        if (objective.getOr(HyperVaultObjective.CHAOS_COUNT, 0) == 0 && !vault.get(Vault.LISTENERS).getAll().isEmpty()) {
            dumpChaosModifiers();
        }
        tickAmbientEvents();
        if (objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING) != Phase.REWARD) {
            return;
        }
        int remaining = objective.getOr(HyperVaultObjective.EXIT_TICKS, 0) - 1;
        objective.set(HyperVaultObjective.EXIT_TICKS, Math.max(0, remaining));
        if (remaining <= 0) {
            removeExitPillar();
            cycleManager.rollBatch();
        }
    }

    private void tickDoorAnimation() {
        if (this.doorAnimation == null) {
            return;
        }
        BlockPos pillar = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (pillar == null) {
            this.doorAnimation = null;
            return;
        }
        this.doorAnimation.onTick(world, pillar, RuneBossFight.RoomStyle.BOSS_1, vault);
        if (this.doorAnimation.isCompleted()) {
            this.doorAnimation = null;
        }
    }

    /** Every 2 minutes, one negative modifier per runner from the curated timer pool (vs the cap). */
    private void tickAmbientEvents() {
        int remaining = objective.getOr(HyperVaultObjective.AMBIENT_TICK, HyperVaultObjective.cfg().getAmbientPeriodTicks()) - 1;
        if (remaining > 0) {
            objective.set(HyperVaultObjective.AMBIENT_TICK, remaining);
            return;
        }
        objective.set(HyperVaultObjective.AMBIENT_TICK, HyperVaultObjective.cfg().getAmbientPeriodTicks());
        for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
            if (!(listener instanceof Runner) || listener.getPlayer().isEmpty()) {
                continue;
            }
            if (HyperVaultObjective.consumeChaosBudget(vault, 1) <= 0) {
                return;
            }
            List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(HyperVaultObjective.CHAOS_POOL_TIMER_EVENTS, level, JavaRandom.ofNanoTime());
            if (modifiers.isEmpty()) {
                WoldsVaults.LOGGER.error("Ambient pool {} is missing/empty — ambient Hyper modifiers cannot fire.", HyperVaultObjective.CHAOS_POOL_TIMER_EVENTS);
                return;
            }
            for (VaultModifier<?> modifier : modifiers) {
                if (HyperModifierPolicy.isStackCapped(vault, modifier)) {
                    continue;
                }
                vault.get(Vault.MODIFIERS).addModifier(modifier, 1, true, ChunkRandom.ofNanoTime());
                announceModifier(modifier);
            }
        }
    }

    /**
     * The fight consumed the pillar block when the boss summoned; put it back from the snapshot
     * taken at arm time so the next cycle has a podium again.
     */
    private void respawnBossPillar() {
        BlockPos pos = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        CompoundTag saved = objective.getOr(HyperVaultObjective.PILLAR_NBT, null);
        if (pos == null || saved == null) {
            WoldsVaults.LOGGER.error("No pillar snapshot recorded — the boss podium cannot respawn this cycle!");
            return;
        }
        if (world.getBlockState(pos).getBlock() == ModBlocks.RUNE_PILLAR) {
            return;
        }
        IZonedWorld.runWithBypass(world, true, () ->
                world.setBlock(pos, ModBlocks.RUNE_PILLAR.defaultBlockState(), 3));
        if (world.getBlockEntity(pos) instanceof BossRunePillarTileEntity pillar) {
            pillar.load(saved);
            pillar.sendUpdates();
        } else {
            WoldsVaults.LOGGER.error("Re-placed boss pillar at {} has no tile entity — arming next cycle will fail.", pos);
        }
    }

    /** Places a temporary obelisk near the boss pillar; using it exits the vault with a completion. */
    private void spawnExitPillar() {
        BlockPos center = objective.getOr(HyperVaultObjective.PILLAR_POS, null);
        if (center == null) {
            WoldsVaults.LOGGER.error("No pillar position recorded — cannot place the Hyper exit pillar this cycle!");
            return;
        }
        BlockPos spot = findExitSpot(center);
        if (spot == null) {
            spot = center.relative(Direction.NORTH, 3);
            WoldsVaults.LOGGER.warn("No clear spot for the Hyper exit pillar around {}; force-placing at {}.", center, spot);
        }
        placeObelisk(spot);
        objective.set(HyperVaultObjective.EXIT_POS, spot);
        HyperVaultObjective.broadcast(vault, "An exit pillar appeared for " + (HyperVaultObjective.cfg().getExitPillarTicks() / 20) + " seconds!", ChatFormatting.AQUA);
    }

    private BlockPos findExitSpot(BlockPos center) {
        for (int distance = 3; distance <= 5; distance++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos pos = center.relative(direction, distance);
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos candidate = pos.above(dy);
                    if (world.getBlockState(candidate).isAir()
                            && world.getBlockState(candidate.above()).isAir()
                            && world.getBlockState(candidate.below()).isFaceSturdy(world, candidate.below(), Direction.UP)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    /**
     * The boss room sits in a no-modify WorldZone kept for the whole vault; without the bypass
     * these setBlock calls are silently rejected and the pillar never appears.
     */
    private void placeObelisk(BlockPos pos) {
        BlockState lower = ModBlocks.OBELISK.defaultBlockState()
                .setValue(ObeliskBlock.HALF, DoubleBlockHalf.LOWER).setValue(ObeliskBlock.FILLED, false);
        BlockState upper = ModBlocks.OBELISK.defaultBlockState()
                .setValue(ObeliskBlock.HALF, DoubleBlockHalf.UPPER).setValue(ObeliskBlock.FILLED, false);
        IZonedWorld.runWithBypass(world, true, () -> {
            world.setBlock(pos, lower, 3);
            world.setBlock(pos.above(), upper, 3);
        });
    }

    /**
     * The boss room's no-modify zone is wider than the arena and makes nearby POI chests
     * unbreakable. Vanilla removes it at an animation step that never runs; do it here once
     * the first fight is over (the physical gates still seal later fights).
     */
    private void removeBossRoomZone() {
        int zoneId = objective.getOr(HyperVaultObjective.ZONE_ID, 0);
        if (zoneId <= 0) {
            return;
        }
        WorldZonesData.get(world.getServer()).getOrCreate(world.dimension()).remove(zoneId);
        objective.set(HyperVaultObjective.ZONE_ID, 0);
        WoldsVaults.LOGGER.info("Removed the boss room's no-modify zone ({}).", zoneId);
    }

    /**
     * The boss's escort has no purpose once it falls: the persistent adds otherwise pile up
     * across cycles (server overload), and any straggler projectile can deadlock vault
     * teardown (VaultFireball.explode sync-loads chunks from the vault's tick thread).
     */
    private void discardFightSpawns() {
        List<Entity> spawns = new ArrayList<>();
        for (Entity entity : world.getAllEntities()) {
            if (entity instanceof LivingEntity living && living.isAlive()
                    && entity.getTags().contains(HyperVaultObjective.FIGHT_SPAWN_TAG)) {
                spawns.add(entity);
            }
        }
        spawns.forEach(Entity::discard);
        if (!spawns.isEmpty()) {
            WoldsVaults.LOGGER.info("Discarded {} leftover hyperboss fight spawns.", spawns.size());
        }
    }

    /**
     * EXIT_POS deliberately keeps its stale value afterwards: the use-handler is gated on
     * PHASE == REWARD, and the next reward phase overwrites it before it can be clicked again.
     */
    private void removeExitPillar() {
        BlockPos pos = objective.getOr(HyperVaultObjective.EXIT_POS, null);
        if (pos == null) {
            return;
        }
        removeIfObelisk(pos);
        removeIfObelisk(pos.above());
    }

    private void removeIfObelisk(BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == ModBlocks.OBELISK) {
            IZonedWorld.runWithBypass(world, true, () -> world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3));
        } else {
            WoldsVaults.LOGGER.warn("Expected the Hyper exit pillar at {} but found {}; leaving it alone.", pos, world.getBlockState(pos));
        }
    }
}
