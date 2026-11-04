package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.config.sigil.SigilDefinitionsConfig;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.scavenger.ScavengerGoal;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.task.ProgressConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.counter.TargetTaskCounter;
import iskallia.vault.task.counter.TaskCounter;
import iskallia.vault.task.renderer.BingoRenderer;
import iskallia.vault.task.renderer.TaskRenderer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.ObjectiveDifficultyConfig;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.TargetTaskCounterConfigAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * The one number every objective scales from: D = (1 + sigil difficulty) x (1 + medallion objective bonus),
 * computed at configure time. Objectives raise their base requirement to base x D^k with k from
 * {@link ObjectiveDifficultyConfig}; the sigil itself carries nothing but its difficulty.
 */
public final class ObjectiveDifficulty {
    private static final ThreadLocal<Double> CURRENT = new ThreadLocal<>();

    private ObjectiveDifficulty() {
    }

    private static final AtomicBoolean WARNED = new AtomicBoolean();

    /** The installed scaling table, or null (logged once) before it is installed; nothing scales without it. */
    @Nullable
    private static ObjectiveDifficultyConfig config() {
        ObjectiveDifficultyConfig config = xyz.iwolfking.woldsvaults.init.ModConfigs.OBJECTIVE_DIFFICULTY;
        if (config == null && WARNED.compareAndSet(false, true)) {
            WoldsVaults.LOGGER.error("Objective difficulty was read before the objectives/difficulty/objective_difficulty table "
                    + "was installed; objectives do not scale with difficulty until it is");
        }
        return config;
    }

    /** The sigil's difficulty from sigils/definitions.json; 0 for no sigil or an unknown one (logged). */
    public static double sigilDifficulty(@Nullable String sigil) {
        if (sigil == null || sigil.isBlank()) {
            return 0.0D;
        }
        Optional<SigilDefinitionsConfig.SigilDefinition> definition = ModConfigs.SIGIL_DEFINITIONS.get(sigil.toLowerCase());
        if (definition.isEmpty()) {
            WoldsVaults.LOGGER.warn("Sigil '{}' has no entry in sigils/definitions.json, counting its difficulty as 0", sigil);
            return 0.0D;
        }
        return Math.max(0.0D, definition.get().getDifficulty());
    }

    public static double multiplier(@Nullable String sigil, @Nullable Vault vault) {
        double multiplier = 1.0D + sigilDifficulty(sigil);
        if (vault != null) {
            multiplier *= GreedMedallionEffects.objectiveDifficultyMultiplier(vault);
        }
        return multiplier;
    }

    /** As {@link #multiplier(String, Vault)} using the sigil the vault already carries (set once configure has run). */
    public static double multiplier(Vault vault) {
        String sigil = vault != null && vault.has(Vault.SIGIL) ? vault.get(Vault.SIGIL) : null;
        return multiplier(sigil, vault);
    }

    public static double increase(double multiplier) {
        return multiplier - 1.0D;
    }

    public static double exponent(String objective) {
        ObjectiveDifficultyConfig config = config();
        return config == null ? 0.0D : config.getExponent(objective);
    }

    public static double factor(double multiplier, String objective) {
        return Math.pow(multiplier, exponent(objective));
    }

    public static int scale(int base, double factor) {
        return Math.max(1, (int) Math.round(base * factor));
    }

    public static float scale(float base, double factor) {
        return (float) (base * factor);
    }

    public static IntRoll scale(IntRoll roll, double factor) {
        if (roll instanceof IntRoll.Constant constant) {
            return IntRoll.ofConstant(scale(constant.getCount(), factor));
        }
        if (!(roll instanceof IntRoll.Uniform)) {
            WoldsVaults.LOGGER.warn("Scaling a {} roll as a uniform {}..{} roll", roll.getClass().getSimpleName(), roll.getMin(), roll.getMax());
        }
        return IntRoll.ofUniform(scale(roll.getMin(), factor), scale(roll.getMax(), factor));
    }

    /**
     * Challenge stacks for a vault at difficulty D: linear below the breakpoint, offset + D^power above it;
     * a vault with no difficulty source gets none.
     */
    public static int challengeStacks(double multiplier) {
        if (multiplier <= 1.0D) {
            return 0;
        }
        ObjectiveDifficultyConfig config = config();
        if (config == null) {
            return 0;
        }
        double stacks = multiplier < config.getStackBreakpoint()
                ? config.getStackLinearFactor() * multiplier
                : config.getStackOffset() + Math.pow(multiplier, config.getStackPower());
        return Math.max(0, (int) Math.round(stacks));
    }

    public static int crateTiers(@Nullable String sigil) {
        ObjectiveDifficultyConfig config = config();
        return config == null ? 0 : config.getCrateTiers(sigilDifficulty(sigil));
    }

    public static int scavengerGoalFloor(double multiplier, int level) {
        ObjectiveDifficultyConfig config = config();
        return config == null ? 0 : config.getScavengerGoalFloor(increase(multiplier), level);
    }

    public static int ballisticGridBonus(double multiplier) {
        ObjectiveDifficultyConfig config = config();
        return config == null ? 0 : config.getBallisticGridBonus(increase(multiplier));
    }

    public static int ballisticGridCap() {
        ObjectiveDifficultyConfig config = config();
        return config == null ? 0 : config.getBallisticGridCap();
    }

    public static int survivalStartWave(double multiplier, int waveGroups) {
        ObjectiveDifficultyConfig config = config();
        if (config == null) {
            return 0;
        }
        int latest = waveGroups - 1 - config.getSurvivalReservedWaveGroups();
        return Math.max(0, Math.min((int) Math.floor(increase(multiplier)), latest));
    }

    /**
     * A runner's scavenger goals at difficulty D: the list is padded from {@code more} up to the goal-count floor,
     * then every goal's item count is raised by the objective's exponent.
     */
    public static List<ScavengerGoal> scavengerGoals(List<ScavengerGoal> generated, Supplier<List<ScavengerGoal>> more,
                                                     double multiplier, int level, String objective) {
        List<ScavengerGoal> goals = new ArrayList<>(generated);
        int floor = scavengerGoalFloor(multiplier, level);
        for (int attempt = 0; goals.size() < floor && attempt < 8; attempt++) {
            for (ScavengerGoal extra : more.get()) {
                if (goals.size() >= floor) {
                    break;
                }
                goals.add(extra);
            }
        }
        if (goals.size() < floor) {
            WoldsVaults.LOGGER.warn("Scavenger goal floor {} at difficulty {} could not be reached, {} goals rolled", floor, multiplier, goals.size());
        }
        scaleScavengerGoals(goals, multiplier, objective);
        return goals;
    }

    public static void scaleScavengerGoals(List<ScavengerGoal> goals, double multiplier, String objective) {
        double factor = factor(multiplier, objective);
        for (ScavengerGoal goal : goals) {
            int total = scale(goal.get(ScavengerGoal.TOTAL), factor);
            goal.set(ScavengerGoal.TOTAL, total);
            goal.set(ScavengerGoal.BASE_TOTAL, total);
        }
    }

    /** Raises every target counter under a bingo task by D^k, k from the tile's renderer name class. */
    public static void scaleBingoTask(Task task, double multiplier) {
        if (task == null || multiplier == 1.0D) {
            return;
        }
        for (ProgressConfiguredTask<?, ?> progress : task.getDescendants(ProgressConfiguredTask.class)) {
            TaskCounter<?, ?> counter = progress.getCounter();
            if (!(counter instanceof TargetTaskCounter<?, ?> targetCounter)) {
                continue;
            }
            TargetTaskCounter.Config<?, ?> counterConfig = targetCounter.getConfig();
            if (!(counterConfig.getTarget() instanceof IntRoll roll)) {
                continue;
            }
            ObjectiveDifficultyConfig config = config();
            double factor = config == null ? 1.0D : Math.pow(multiplier, config.getTileExponent(tileName(progress)));
            ((TargetTaskCounterConfigAccessor) counterConfig).woldsvaults$setTarget(scale(roll, factor));
        }
    }

    @Nullable
    private static String tileName(Task task) {
        for (Task current = task; current != null; current = current.getParent()) {
            TaskRenderer<?, ?> renderer = current.getRenderer();
            if (renderer instanceof BingoRenderer.Leaf leaf && leaf.name != null) {
                return leaf.name;
            }
        }
        return null;
    }

    public static void begin(double multiplier) {
        CURRENT.set(multiplier);
    }

    public static double current() {
        Double multiplier = CURRENT.get();
        if (multiplier == null) {
            WoldsVaults.LOGGER.error("Objective difficulty read outside a crystal configure call, using 1.0");
            return 1.0D;
        }
        return multiplier;
    }

    public static void end() {
        CURRENT.remove();
    }
}
