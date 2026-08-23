package xyz.iwolfking.woldsvaults.milestones.trials;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

/**
 * Every place the hyper objective behaves differently because the vault is a rank-up trial. Each
 * method takes the vault and falls back to the live hyper configuration when it is not a trial, so
 * the call sites inside the hyper code stay one-liners and ordinary hyper vaults are untouched.
 *
 * <p>Two behavior changes, both from the trial spec. The exit pillar is not offered below the
 * required cycle count, so a three-cycle trial cannot be cashed out after one; and once the
 * required count is reached the vault ends itself on the usual fifteen-second countdown instead of
 * waiting for a pillar click.</p>
 */
public final class GreedTrialHyper {
    private GreedTrialHyper() {
    }

    private static GreedTrial trial(Vault vault) {
        GreedTrial trial = GreedTrials.trial(vault);
        return trial != null && trial.getKind() == GreedTrial.Kind.HYPER ? trial : null;
    }

    /**
     * Base boss health/damage escalation. The sheet's "Base Boss Strength" column is in the same
     * unit as {@code bossHealthPercent}: 5.0 means +500%.
     */
    public static double bossStrength(Vault vault, double live) {
        GreedTrial trial = trial(vault);
        return trial == null ? live : trial.getBossStrength();
    }

    /** Per-cycle compounding factor - the sheet's "Scaling Modifier" in place of hyperStatFactor. */
    public static double cycleScaling(Vault vault, double live) {
        GreedTrial trial = trial(vault);
        return trial == null ? live : trial.getCycleScaling();
    }

    /**
     * Flat per-cycle stat increment. Trials use only the sheet's base strength and per-cycle
     * multiplier, so the live vault's extra additive ramp is switched off for them.
     */
    public static double statIncrement(Vault vault, double live) {
        return trial(vault) == null ? live : 0.0D;
    }

    /**
     * Modifiers dumped per boss kill. The sheet's "N modifiers +N" column is read as N modifiers
     * granted per cycle, which with the budget below works out to N at the start of the vault and
     * N more for every cycle cleared.
     */
    public static int chaosPerKill(Vault vault, int live) {
        GreedTrial trial = trial(vault);
        return trial == null ? live : trial.getModifierCount();
    }

    /** Total modifier budget for the whole trial: the per-cycle count times the cycles plus one. */
    public static int chaosCap(Vault vault, int live) {
        GreedTrial trial = trial(vault);
        return trial == null ? live : trial.getModifierCount() * (trial.getRequiredCycles() + 1);
    }

    /** Cycles that have to be cleared before the trial counts as passed; 0 outside a hyper trial. */
    public static int requiredCycles(Vault vault) {
        GreedTrial trial = trial(vault);
        return trial == null ? 0 : trial.getRequiredCycles();
    }

    /**
     * Whether the exit pillar should be offered after this cycle. Ordinary hyper vaults always
     * offer it; a trial withholds it until the required cycles are done, so the only pillar a trial
     * ever spawns is the one on its final cycle - which is the same cycle the trial passes on, so
     * the pillar and the payout arrive together and the player can leave under their own power
     * rather than waiting out the outro.
     */
    public static boolean shouldOfferExit(Vault vault) {
        int required = requiredCycles(vault);
        return required <= 0 || cycles(vault) >= required;
    }

    /** True the moment a trial's cycle target is met and the vault should start closing itself. */
    public static boolean isCycleTargetMet(Vault vault) {
        int required = requiredCycles(vault);
        return required > 0 && cycles(vault) >= required;
    }

    private static int cycles(Vault vault) {
        return HyperVaultObjective.getCycleCount(vault);
    }

    /**
     * Starts the standard victory countdown for every runner still in a trial vault whose cycle
     * target has just been met. Writing straight into the objective's EXTRACTIONS map reuses the
     * exact path an exit-pillar click takes: damage immunity, the per-second actionbar countdown,
     * the completion crate and the teleport out all follow from it.
     */
    public static void completeTrialVault(Vault vault, HyperVaultObjective objective, int transitionTicks) {
        CompoundTag extractions = objective.getOr(HyperVaultObjective.EXTRACTIONS, new CompoundTag());
        CompoundTag updated = extractions.copy();
        int started = 0;
        for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
            if (!(listener instanceof Runner)) {
                continue;
            }
            String key = listener.getId().toString();
            if (updated.contains(key)) {
                continue;
            }
            updated.putInt(key, transitionTicks);
            started++;
        }
        if (started == 0) {
            WoldsVaults.LOGGER.warn("Hyper trial cycle target met but no runner was left to extract.");
            return;
        }
        objective.set(HyperVaultObjective.EXTRACTIONS, updated);
        HyperVaultObjective.broadcast(vault, "Trial complete — extracting in " + (transitionTicks / 20) + " seconds!",
                net.minecraft.ChatFormatting.GOLD);
    }

    /**
     * Pays the trial out for a runner who has just been completed and extracted. Silently does
     * nothing outside a trial vault, so the hyper completion path can call it unconditionally.
     */
    public static void onRunnerCompleted(Vault vault, Runner runner) {
        if (GreedTrials.trialRank(vault) <= 0) {
            return;
        }
        ServerPlayer player = runner.getPlayer().orElse(null);
        if (player == null) {
            WoldsVaults.LOGGER.warn("A hyper trial runner completed while offline — the rank-up could not be paid.");
            return;
        }
        GreedTrials.award(vault, player);
    }
}
