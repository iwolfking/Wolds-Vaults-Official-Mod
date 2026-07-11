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
import iskallia.vault.task.TaskContext;
import iskallia.vault.task.counter.TargetTaskCounter;
import iskallia.vault.task.source.EntityTaskSource;
import net.minecraft.ChatFormatting;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.BingoObjectiveAccessor;
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
        } else if (phase == Phase.MINIS) {
            rescaleMinis();
            if (isBatchComplete()) {
                objective.set(HyperVaultObjective.PHASE, Phase.ARMED);
                HyperVaultObjective.broadcast(vault, "All objectives complete — the boss podium is armed!", ChatFormatting.RED);
            }
        }
    }

    /**
     * Elastic difficulty: elixir/bingo requirements track the live cycle count and runner
     * count every second (the collector's tiles do the same through the OBJECTIVE_TARGET
     * handler in {@link HyperVaultObjective}), so a death or join rescales mid-card.
     */
    private void rescaleMinis() {
        rescaleElixirTarget();
        pinCardJoinCounters();
        if (world.getTickCount() % 20 == 0) {
            rescaleBingoTargets();
        }
    }

    /**
     * Bingo's JOINED counter normally drives VH's per-task-config player scaling; hyper
     * supplies its own +25%/extra-player rate instead, so it stays pinned at 1 (VH increments
     * it when a new runner joins mid-vault, undone here with a log). The collector keeps VH's
     * native JOINED scaling (+50%/extra player), refreshed per batch by seedCurrentRunners.
     */
    private void pinCardJoinCounters() {
        objective.findMini(BingoObjective.class).ifPresent(bingo -> {
            if (bingo.getOr(BingoObjective.JOINED, 0) != 1) {
                WoldsVaults.LOGGER.info("Re-pinned the bingo card's JOINED counter to 1 (hyper does its own player scaling).");
                bingo.set(BingoObjective.JOINED, 1);
            }
        });
    }

    /** Every task target on incomplete bingo tiles: base x cycle scale x bingo player scale. */
    private void rescaleBingoTargets() {
        objective.findMini(BingoObjective.class).ifPresent(bingo -> {
            if (!(bingo.get(BingoObjective.TASK) instanceof BingoTask root)) {
                return;
            }
            double scale = HyperVaultObjective.cycleRequirementScale(vault)
                    * HyperVaultObjective.playerRequirementScale(vault, HyperVaultObjective.cfg().getPlayerScaleBingo());
            TaskContext context = bingo.getContext(world, vault);
            for (int index = 0; index < root.getWidth() * root.getHeight(); index++) {
                if (root.isCompleted(index)) {
                    continue;
                }
                root.getChild(index).streamSelfAndDescendants(iskallia.vault.task.ProgressConfiguredTask.class).forEach(task -> {
                    if (task.getCounter() instanceof TargetTaskCounter<?, ?> counter && counter.isPopulated()) {
                        // VH's own scaler: target = base x (1 + additional x contribution).
                        ((BingoObjectiveAccessor) bingo).callScaleTargetWithCondition(task, counter, scale - 1.0, 1, context);
                    }
                });
            }
        });
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
                    addMini(BingoObjective.of(task.get(), boardSize(), boardSize(), false));
                } else {
                    WoldsVaults.LOGGER.error("Default bingo pool is empty too — the bingo mini will report complete immediately.");
                }
            }
            case SCAVENGER -> addMini(ScavengerBingoObjective.of(boardSize(), boardSize(), 0.0F, VaultMod.id("default"), false));
            case ELIXIR -> ensureElixirGoal();
            case BRUTAL -> {
                // The pillar floor rises +1 every 3 kills and +1 per 2 extra runners, capped at
                // the 5-pillar ceiling (at cap every batch demands the full 5).
                int runnersNow = vault.get(Vault.LISTENERS).getAll(Runner.class).size();
                int minObelisks = Math.min(HyperVaultObjective.cfg().getObeliskMax(),
                        HyperVaultObjective.cfg().getObeliskMin()
                                + objective.getOr(HyperVaultObjective.CYCLE, 0) / 3
                                + Math.max(0, runnersNow - 1) / 2);
                int obelisks = minObelisks
                        + random.nextInt(HyperVaultObjective.cfg().getObeliskMax() - minObelisks + 1);
                addMini(BrutalBossesObjective.of(obelisks, () -> random.nextInt(3) + 1,
                        HyperVaultObjective.cfg().getBrutalObeliskProbability(), true));
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

    /** Boards grow to 5x5 once the vault reaches its 6th boss cycle. */
    private int boardSize() {
        return objective.getOr(HyperVaultObjective.CYCLE, 0) >= HyperVaultObjective.cfg().getBoardUpsizeCycle() ? 5 : 4;
    }

    /**
     * Card objectives only learn about players from LISTENER_JOIN, which fired long before a
     * mid-vault batch roll. Without pre-seeding, bingo's EntityTaskSource stays empty and every
     * player action is filtered out (the card never progresses). Bingo's JOINED is pinned at 1
     * (hyper's own +25%/extra-player scaling replaces VH's); the collector's JOINED is seeded
     * with the live runner count so VH's native +50%/extra-player tile scaling applies.
     */
    private void seedCurrentRunners(Objective mini) {
        Collection<Runner> runners = vault.get(Vault.LISTENERS).getAll(Runner.class);
        if (mini instanceof BingoObjective bingo) {
            UUID[] ids = runners.stream().map(Listener::getId).toArray(UUID[]::new);
            // The task source's random populates the card, so mixing the cycle in keeps repeat
            // Bingo cycles distinct while staying deterministic across a reload.
            long seed = vault.get(Vault.SEED) ^ (0x9E3779B97F4A7C15L * (objective.getOr(HyperVaultObjective.CYCLE, 0) + 1));
            bingo.set(BingoObjective.TASK_SOURCE, EntityTaskSource.ofUuids(JavaRandom.ofInternal(seed), ids));
            bingo.set(BingoObjective.JOINED, 1);
        } else if (mini instanceof ScavengerBingoObjective scav) {
            scav.set(ScavengerBingoObjective.JOINED, runners.size());
        }
    }

    /**
     * The shared elixir bar. The task set and base target derive from the vault seed (identical
     * after a restart, identical for every player); the target starts at 75% of a normal
     * vault's requirement and grows +25% of that base per completed cycle.
     */
    private void ensureElixirGoal() {
        rescaleElixirTarget();
        ElixirTask.List tasks = objective.get(HyperVaultObjective.ELIXIR_TASKS);
        if (!tasks.isEmpty()) {
            return;
        }
        // Same draw order as the original roll: the target consumes the seeded random's
        // leading draws, then the goals — so pre-existing saves regenerate identical tasks.
        JavaRandom seeded = JavaRandom.ofInternal(vault.get(Vault.SEED));
        ModConfigs.ELIXIR.generateTarget(level, seeded);
        for (ElixirTask task : ModConfigs.ELIXIR.generateGoals(level, seeded)) {
            tasks.add(task);
        }
        if (tasks.isEmpty()) {
            WoldsVaults.LOGGER.error("ModConfigs.ELIXIR produced no elixir tasks — the shared elixir bar cannot progress.");
        }
    }

    /**
     * target = seed-derived base x (0.75 + 0.25 x cycle) x (1 + 0.45 x extra runners), recomputed
     * every tick. The base only consumes the seeded random's leading draws, so the task set
     * generated after it in ensureElixirGoal is unaffected.
     */
    private void rescaleElixirTarget() {
        JavaRandom seeded = JavaRandom.ofInternal(vault.get(Vault.SEED));
        int baseTarget = ModConfigs.ELIXIR.generateTarget(level, seeded);
        int cycle = objective.getOr(HyperVaultObjective.CYCLE, 0);
        double scale = (HyperVaultObjective.cfg().getElixirTargetMultiplier() + HyperVaultObjective.cfg().getElixirTargetIncrement() * cycle)
                * HyperVaultObjective.playerRequirementScale(vault, HyperVaultObjective.cfg().getPlayerScaleElixir());
        objective.set(HyperVaultObjective.ELIXIR_TARGET, Math.max(1, (int) Math.round(baseTarget * scale)));
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
