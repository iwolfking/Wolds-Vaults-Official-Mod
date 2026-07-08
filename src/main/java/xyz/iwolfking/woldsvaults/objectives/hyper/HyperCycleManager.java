package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.elixir.ElixirTask;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.task.BingoTask;
import iskallia.vault.task.source.EntityTaskSource;
import net.minecraft.ChatFormatting;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ScavengerBingoObjectiveAccessor;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.HyperMini;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.Phase;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Rolls each cycle's mini-objective batch and flips the phase to ARMED once every batch entry
 * reports complete. A batch is always the shared elixir bar, one card (bingo or collector,
 * coin-flipped), and the forced brutal pillars.
 */
public class HyperCycleManager extends ObjectiveManager<HyperVaultObjective> {
    public HyperCycleManager(Vault vault, VirtualWorld world, HyperVaultObjective objective) {
        super(vault, world, objective);
    }

    @Override
    public void tick() {
        Phase phase = objective.getOr(HyperVaultObjective.PHASE, Phase.ROLLING);
        if (phase == Phase.ROLLING) {
            rollBatch();
        } else if (phase == Phase.MINIS && isBatchComplete()) {
            objective.set(HyperVaultObjective.PHASE, Phase.ARMED);
            HyperVaultObjective.broadcast(vault, "All objectives complete — the boss podium is armed!", ChatFormatting.RED);
        }
    }

    public void rollBatch() {
        // Old minis keep their event hooks through the boss/reward phases (so obelisk
        // placeholders keep converting); they are only torn down here, at the next roll.
        objective.get(HyperVaultObjective.MINIS).forEach(Objective::releaseServer);
        objective.get(HyperVaultObjective.MINIS).clear();
        objective.set(HyperVaultObjective.ELIXIR_PROGRESS, 0);

        JavaRandom random = JavaRandom.ofNanoTime();
        HyperMini card = random.nextBoolean() ? HyperMini.BINGO : HyperMini.SCAVENGER;
        int mask = (1 << HyperMini.ELIXIR.ordinal()) | (1 << card.ordinal()) | (1 << HyperMini.BRUTAL.ordinal());
        objective.set(HyperVaultObjective.BATCH_MASK, mask);

        initMini(HyperMini.ELIXIR, random);
        initMini(card, random);
        initMini(HyperMini.BRUTAL, random);
        objective.set(HyperVaultObjective.PHASE, Phase.MINIS);
        HyperVaultObjective.broadcast(vault, "New objectives: Elixir, " + displayName(card) + ", Brutal Pillars", ChatFormatting.GOLD);
    }

    private void initMini(HyperMini mini, JavaRandom random) {
        switch (mini) {
            case BINGO -> {
                Optional<BingoTask> task = ModConfigs.BINGO.generate(HyperVaultObjective.BINGO_POOL, level);
                if (task.isEmpty()) {
                    WoldsVaults.LOGGER.warn("Bingo pool {} missing/empty, falling back to the default pool for the Hyper batch.", HyperVaultObjective.BINGO_POOL);
                    task = ModConfigs.BINGO.generate(VaultMod.id("default"), level);
                }
                if (task.isPresent()) {
                    addMini(BingoObjective.of(task.get(), 4, 4, false));
                } else {
                    WoldsVaults.LOGGER.error("Default bingo pool is empty too — the bingo mini will report complete immediately.");
                }
            }
            case SCAVENGER -> addMini(ScavengerBingoObjective.of(4, 4, 0.0F, VaultMod.id("default"), false));
            case ELIXIR -> ensureElixirGoal();
            case BRUTAL -> {
                int obelisks = HyperVaultObjective.OBELISK_MIN
                        + random.nextInt(HyperVaultObjective.OBELISK_MAX - HyperVaultObjective.OBELISK_MIN + 1);
                addMini(BrutalBossesObjective.of(obelisks, () -> random.nextInt(3) + 1,
                        HyperVaultObjective.BRUTAL_OBELISK_PROBABILITY, true));
            }
        }
    }

    private void addMini(Objective mini) {
        seedCurrentRunners(mini);
        objective.get(HyperVaultObjective.MINIS).add(mini);
        mini.initServer(world, vault);
        if (mini instanceof ScavengerBingoObjective scav) {
            // Tile generation is seeded purely by the vault seed, so a repeat Collector cycle
            // would get the identical card; this regenerates with a time-mixed seed.
            ((ScavengerBingoObjectiveAccessor) (Object) scav)
                    .callRegenerateIncompleteTiles(vault, scav.get(ScavengerBingoObjective.SETTLED_TILES));
        }
    }

    /**
     * Card objectives only learn about players from LISTENER_JOIN, which fired long before a
     * mid-vault batch roll. Without pre-seeding, bingo's EntityTaskSource stays empty and every
     * player action is filtered out (the card never progresses); the JOINED counters drive
     * per-player target scaling on both cards.
     */
    private void seedCurrentRunners(Objective mini) {
        Collection<Runner> runners = vault.get(Vault.LISTENERS).getAll(Runner.class);
        if (mini instanceof BingoObjective bingo) {
            UUID[] ids = runners.stream().map(Listener::getId).toArray(UUID[]::new);
            // The task source's random populates the card, so mixing the cycle in keeps repeat
            // Bingo cycles distinct while staying deterministic across a reload.
            long seed = vault.get(Vault.SEED) ^ (0x9E3779B97F4A7C15L * (objective.getOr(HyperVaultObjective.CYCLE, 0) + 1));
            bingo.set(BingoObjective.TASK_SOURCE, EntityTaskSource.ofUuids(JavaRandom.ofInternal(seed), ids));
            bingo.set(BingoObjective.JOINED, runners.size());
        } else if (mini instanceof ScavengerBingoObjective scav) {
            scav.set(ScavengerBingoObjective.JOINED, runners.size());
        }
    }

    /**
     * The shared elixir bar. The task set and base target derive from the vault seed (identical
     * after a restart, identical for every player); the target then grows +50% of the base per
     * completed cycle.
     */
    private void ensureElixirGoal() {
        JavaRandom seeded = JavaRandom.ofInternal(vault.get(Vault.SEED));
        int baseTarget = ModConfigs.ELIXIR.generateTarget(level, seeded);
        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        float scale = HyperVaultObjective.ELIXIR_TARGET_MULTIPLIER * (1.0F + 0.5F * cycle);
        objective.set(HyperVaultObjective.ELIXIR_TARGET, Math.max(1, Math.round(baseTarget * scale)));
        ElixirTask.List tasks = objective.get(HyperVaultObjective.ELIXIR_TASKS);
        if (!tasks.isEmpty()) {
            return;
        }
        for (ElixirTask task : ModConfigs.ELIXIR.generateGoals(level, seeded)) {
            tasks.add(task);
        }
        if (tasks.isEmpty()) {
            WoldsVaults.LOGGER.error("ModConfigs.ELIXIR produced no elixir tasks — the shared elixir bar cannot progress.");
        }
    }

    private boolean isBatchComplete() {
        for (HyperMini mini : HyperMini.values()) {
            if (objective.isMiniInBatch(mini) && !isMiniComplete(mini)) {
                return false;
            }
        }
        return true;
    }

    private boolean isMiniComplete(HyperMini mini) {
        return switch (mini) {
            case BINGO -> completeOrMissing(BingoObjective.class, bingo -> bingo.getBingos() > 0);
            case SCAVENGER -> completeOrMissing(ScavengerBingoObjective.class, scav -> scav.getCompletedBingos() > 0);
            case BRUTAL -> completeOrMissing(BrutalBossesObjective.class, BrutalBossesObjective::isCompleted);
            case ELIXIR -> objective.getOr(HyperVaultObjective.ELIXIR_PROGRESS, 0) >= objective.getOr(HyperVaultObjective.ELIXIR_TARGET, Integer.MAX_VALUE);
        };
    }

    private <T extends Objective> boolean completeOrMissing(Class<T> type, java.util.function.Predicate<T> done) {
        Optional<T> mini = objective.findMini(type);
        if (mini.isEmpty()) {
            // Roll-time generation already logged why; don't deadlock the cycle on a missing mini.
            return true;
        }
        return done.test(mini.get());
    }

    private static String displayName(HyperMini mini) {
        return switch (mini) {
            case BINGO -> "Bingo";
            case SCAVENGER -> "Collector";
            case ELIXIR -> "Elixir";
            case BRUTAL -> "Brutal Pillars";
        };
    }
}
