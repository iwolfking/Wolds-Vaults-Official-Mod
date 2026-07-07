package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.ChaosObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.elixir.ElixirTask;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.task.BingoTask;
import net.minecraft.ChatFormatting;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.HyperMini;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective.Phase;
import xyz.iwolfking.woldsvaults.objectives.lib.ObjectiveManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Rolls each cycle's mini-objective batch and flips the phase to ARMED once every
 * batch entry reports complete. Two random minis + the forced brutal pillars per cycle.
 */
public class HyperCycleManager extends ObjectiveManager<HyperVaultObjective> {
    private static final List<HyperMini> ROLLABLE = Arrays.stream(HyperMini.values())
            .filter(mini -> mini != HyperMini.BRUTAL).toList();

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
        objective.set(HyperVaultObjective.TREASURE_DOORS, 0);
        objective.set(HyperVaultObjective.CHESTS_WOODEN, 0);
        objective.set(HyperVaultObjective.CHESTS_GILDED, 0);
        objective.set(HyperVaultObjective.CHESTS_ORNATE, 0);
        objective.set(HyperVaultObjective.CHESTS_LIVING, 0);
        objective.set(HyperVaultObjective.ELIXIR_PROGRESS, 0);

        JavaRandom random = JavaRandom.ofNanoTime();
        List<HyperMini> pool = new ArrayList<>(ROLLABLE);
        int mask = 1 << HyperMini.BRUTAL.ordinal();
        StringBuilder names = new StringBuilder("Brutal Pillars");
        for (int i = 0; i < HyperVaultObjective.MINIS_PER_CYCLE && !pool.isEmpty(); i++) {
            HyperMini rolled = pool.remove(random.nextInt(pool.size()));
            mask |= 1 << rolled.ordinal();
            names.append(", ").append(displayName(rolled));
            initMini(rolled, random);
        }
        objective.set(HyperVaultObjective.BATCH_MASK, mask);
        initMini(HyperMini.BRUTAL, random);
        objective.set(HyperVaultObjective.PHASE, Phase.MINIS);
        HyperVaultObjective.broadcast(vault, "New objectives: " + names, ChatFormatting.GOLD);
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
            case CHAOS -> {
                // Sub-objective count comes from the chaos config's default pool (pack-tunable).
                ModConfigs.CHAOS.generate(VaultMod.id("default"), level).ifPresentOrElse(
                        task -> addMini(ChaosObjective.of(task)),
                        () -> WoldsVaults.LOGGER.error("Default chaos pool is empty — the chaos mini will report complete immediately."));
            }
            case ELIXIR -> ensureElixirGoal();
            case TREASURE_DOORS, CHEST_COLLECT -> {
                // Counter-based minis live directly on the orchestrator's fields.
            }
            case BRUTAL -> {
                int obelisks = HyperVaultObjective.OBELISK_MIN
                        + random.nextInt(HyperVaultObjective.OBELISK_MAX - HyperVaultObjective.OBELISK_MIN + 1);
                addMini(BrutalBossesObjective.of(obelisks, () -> random.nextInt(3) + 1,
                        HyperVaultObjective.BRUTAL_OBELISK_PROBABILITY, true));
            }
        }
    }

    private void addMini(Objective mini) {
        objective.get(HyperVaultObjective.MINIS).add(mini);
        mini.initServer(world, vault);
    }

    /**
     * The shared elixir bar: task set and target are rolled once per vault from the vault seed
     * (identical after a restart, identical for every player) and reused for later elixir cycles.
     */
    private void ensureElixirGoal() {
        if (objective.getOr(HyperVaultObjective.ELIXIR_TARGET, 0) > 0) {
            return;
        }
        JavaRandom seeded = JavaRandom.ofInternal(vault.get(Vault.SEED));
        int baseTarget = ModConfigs.ELIXIR.generateTarget(level, seeded);
        int target = Math.max(1, Math.round(baseTarget * HyperVaultObjective.ELIXIR_TARGET_MULTIPLIER));
        objective.set(HyperVaultObjective.ELIXIR_TARGET, target);
        ElixirTask.List tasks = objective.get(HyperVaultObjective.ELIXIR_TASKS);
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
            case CHAOS -> completeOrMissing(ChaosObjective.class, ChaosObjective::isCompleted);
            case BRUTAL -> completeOrMissing(BrutalBossesObjective.class, BrutalBossesObjective::isCompleted);
            case ELIXIR -> objective.getOr(HyperVaultObjective.ELIXIR_PROGRESS, 0) >= objective.getOr(HyperVaultObjective.ELIXIR_TARGET, Integer.MAX_VALUE);
            case TREASURE_DOORS -> objective.getOr(HyperVaultObjective.TREASURE_DOORS, 0) >= HyperVaultObjective.TREASURE_DOOR_TARGET;
            case CHEST_COLLECT -> objective.getOr(HyperVaultObjective.CHESTS_WOODEN, 0) >= HyperVaultObjective.CHEST_TARGET_EACH
                    && objective.getOr(HyperVaultObjective.CHESTS_GILDED, 0) >= HyperVaultObjective.CHEST_TARGET_EACH
                    && objective.getOr(HyperVaultObjective.CHESTS_ORNATE, 0) >= HyperVaultObjective.CHEST_TARGET_EACH
                    && objective.getOr(HyperVaultObjective.CHESTS_LIVING, 0) >= HyperVaultObjective.CHEST_TARGET_EACH;
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
            case CHAOS -> "Chaos";
            case TREASURE_DOORS -> "Treasure Doors";
            case CHEST_COLLECT -> "Chest Collection";
            case BRUTAL -> "Brutal Pillars";
        };
    }
}
