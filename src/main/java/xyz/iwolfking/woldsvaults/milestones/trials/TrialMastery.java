package xyz.iwolfking.woldsvaults.milestones.trials;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import net.minecraft.ChatFormatting;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

/**
 * The rank-up trial's reward for beating its objective splits.
 *
 * <p>A trial's clock is cut into one segment per required cycle, and each segment into thirds: the
 * first two are that cycle's objective time, the last is its boss time. Arming the boss with part of
 * the objective window unspent pays that fraction of the cycle's share of a {@value #MAX_BONUS_BASIS}
 * basis point pool, so a whole trial run perfectly still tops out at the same total however many
 * cycles it has. Overrunning the window pays nothing and is never a penalty.
 *
 * <p>Windows do not sit on absolute vault time: each one is a fresh countdown opened when that
 * cycle's objectives are rolled, so time saved in one cycle never carries into the next. Awards sum,
 * and the running total is one multiplicative layer on everything the runner deals for the rest of
 * the vault.
 */
public final class TrialMastery {
    /** Total damage bonus a flawless run can hold, in basis points: 15000 is +150%. */
    public static final int MAX_BONUS_BASIS = 15000;
    private static final int OBJECTIVE_THIRDS = 2;
    private static final int SEGMENT_THIRDS = 3;

    private TrialMastery() {
    }

    private static GreedTrial trial(Vault vault) {
        GreedTrial trial = GreedTrials.trial(vault);
        return trial != null && trial.getKind() == GreedTrial.Kind.HYPER ? trial : null;
    }

    /** Ticks of objective time one cycle of this trial is allowed, or 0 outside a hyper trial. */
    public static int objectiveWindowTicks(Vault vault) {
        GreedTrial trial = trial(vault);
        if (trial == null) {
            return 0;
        }
        int cycles = Math.max(1, trial.getRequiredCycles());
        return trial.getTicks() / cycles * OBJECTIVE_THIRDS / SEGMENT_THIRDS;
    }

    /** The share of the pool one cycle can pay, in basis points. */
    private static int cycleShareBasis(GreedTrial trial) {
        return MAX_BONUS_BASIS / Math.max(1, trial.getRequiredCycles());
    }

    /**
     * Opens a cycle's objective window. Called once when the vault starts and again whenever the
     * next batch of objectives is rolled; whatever was left of the previous window is discarded.
     */
    public static void openObjectiveWindow(Vault vault, HyperVaultObjective objective) {
        if (trial(vault) == null) {
            return;
        }
        objective.set(HyperVaultObjective.MASTERY_WINDOW, remainingTicks(vault));
    }

    /**
     * Opens the first cycle's window if none is recorded yet. Safe to call every tick: a window
     * already in progress is left alone, so a reload cannot restart one that is half spent.
     */
    public static void ensureObjectiveWindow(Vault vault, HyperVaultObjective objective) {
        if (trial(vault) == null || objective.has(HyperVaultObjective.MASTERY_WINDOW)) {
            return;
        }
        objective.set(HyperVaultObjective.MASTERY_WINDOW, remainingTicks(vault));
    }

    /**
     * Scores the objective window that just closed and adds its bonus to the running total. Called
     * when the podium arms the boss, so the runner carries the award into the fight it earned it for.
     */
    public static void onBossArmed(Vault vault, HyperVaultObjective objective) {
        GreedTrial trial = trial(vault);
        if (trial == null) {
            return;
        }
        int window = objectiveWindowTicks(vault);
        if (window <= 0) {
            WoldsVaults.LOGGER.warn("Trial Mastery has no objective window for the rank {} trial; awarding nothing.",
                    trial.getTargetRank());
            return;
        }
        int opened = objective.getOr(HyperVaultObjective.MASTERY_WINDOW, remainingTicks(vault));
        int spent = Math.max(0, opened - remainingTicks(vault));
        double unspent = Math.max(0.0D, Math.min(1.0D, 1.0D - (double) spent / window));
        int award = (int) Math.round(unspent * cycleShareBasis(trial));
        if (award <= 0) {
            HyperVaultObjective.broadcast(vault, "Trial Mastery: no bonus — the objective window is spent.",
                    ChatFormatting.GRAY);
            return;
        }
        int total = Math.min(MAX_BONUS_BASIS, getBonusBasis(vault) + award);
        objective.set(HyperVaultObjective.MASTERY_BONUS, total);
        WoldsVaults.LOGGER.info(
                "Trial Mastery: objective window {}s of {}s spent -> +{}% this cycle, +{}% total damage.",
                spent / 20, window / 20, format(award), format(total));
        HyperVaultObjective.broadcast(vault, "Trial Mastery +" + format(award) + "% damage (total +"
                + format(total) + "%)", ChatFormatting.GOLD);
    }

    /** The accumulated bonus in basis points, 0 outside a hyper trial or before the first award. */
    public static int getBonusBasis(Vault vault) {
        if (vault == null) {
            return 0;
        }
        return HyperVaultObjective.get(vault)
                .map(objective -> objective.getOr(HyperVaultObjective.MASTERY_BONUS, 0))
                .orElse(0);
    }

    /** The damage multiplier the accumulated bonus is worth: 1.0 with no bonus, 1.75 at +75%. */
    public static double getMultiplier(Vault vault) {
        return 1.0D + getBonusBasis(vault) / 10000.0D;
    }

    private static int remainingTicks(Vault vault) {
        return vault.getOptional(Vault.CLOCK).map(clock -> clock.getOr(TickClock.DISPLAY_TIME, 0)).orElse(0);
    }

    private static String format(int basis) {
        return basis % 100 == 0 ? String.valueOf(basis / 100) : String.format("%.1f", basis / 100.0D);
    }
}
