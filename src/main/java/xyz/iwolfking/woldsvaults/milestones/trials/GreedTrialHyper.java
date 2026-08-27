package xyz.iwolfking.woldsvaults.milestones.trials;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

/** Where the hyper objective differs for a rank-up trial; outside one, the live value passes through. */
public final class GreedTrialHyper {
    private GreedTrialHyper() {
    }

    private static GreedTrial trial(Vault vault) {
        GreedTrial trial = GreedTrials.trial(vault);
        return trial != null && trial.getKind() == GreedTrial.Kind.HYPER ? trial : null;
    }

    /** Base boss health and damage escalation, in {@code bossHealthPercent} units: 5.0 is +500%. */
    public static double bossStrength(Vault vault, double live) {
        GreedTrial trial = trial(vault);
        return trial == null ? live : trial.getBossStrength();
    }

    /** Per-cycle compounding factor, in place of {@code hyperStatFactor}. */
    public static double cycleScaling(Vault vault, double live) {
        GreedTrial trial = trial(vault);
        return trial == null ? live : trial.getCycleScaling();
    }

    /**
     * Multiplier on the elixir target and the bingo/collector requirement scale, so a trial can
     * run its objectives easier than a live hyper vault does. 1.0 outside a hyper trial.
     */
    public static double objectiveScale(Vault vault) {
        GreedTrial trial = trial(vault);
        return trial == null ? 1.0D : trial.getObjectiveScale();
    }

    /** True when the trial this vault is running refuses the given modifier outright. */
    public static boolean isBannedInTrial(Vault vault, ResourceLocation id) {
        GreedTrial trial = trial(vault);
        return trial != null && id != null && trial.getBannedModifiers().contains(id.toString());
    }

    /** Mob movement speed ceiling as a multiple of base; the live cap outside a trial. */
    public static double speedCapFactor(Vault vault, double live) {
        GreedTrial trial = trial(vault);
        return trial == null || trial.getSpeedCapFactor() <= 0.0D ? live : trial.getSpeedCapFactor();
    }

    /** Ticks between ambient negative modifier pulls; the live period outside a trial. */
    public static int ambientPeriodTicks(Vault vault, int live) {
        GreedTrial trial = trial(vault);
        return trial == null || trial.getAmbientPeriodTicks() <= 0 ? live : trial.getAmbientPeriodTicks();
    }

    /** Whether brutal pillar kills pay their own positive bb_ pool instead of a hyper negative. */
    public static boolean hasPositiveBrutalRewards(Vault vault) {
        GreedTrial trial = trial(vault);
        return trial != null && trial.hasPositiveBrutalRewards();
    }

    /** Flat per-cycle stat increment, always 0 inside a trial. */
    public static double statIncrement(Vault vault, double live) {
        return trial(vault) == null ? live : 0.0D;
    }

    /** Modifiers granted per boss cycle. */
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

    /** Whether the exit pillar is offered; a trial withholds it until its cycles are done. */
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

    /** Starts the standard victory countdown for every runner still in the vault. */
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

    /** Pays the trial out for a completed runner. Does nothing outside a trial vault. */
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
