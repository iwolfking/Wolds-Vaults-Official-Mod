package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.RebirthObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.milestones.trials.GreedTrialVessel;

import static iskallia.vault.core.vault.objective.RebirthObjective.PHASE_DAMAGE_DEALT;
import static iskallia.vault.core.vault.objective.RebirthObjective.PHASE_DAMAGE_GOAL;

/**
 * make the greed boss damage target use a float instead of an int capped at MAX_INT
 *
 * <p>Both helpers route through {@code GreedTrialVessel#displayGoal}, which keeps the float curve
 * for an ordinary greed trial and switches to the synced goal when a greed rank-up trial has
 * replaced it with the trial row's health pool.</p>
 */
@Mixin(RebirthObjective.class)
abstract class MixinRebirthObjectiveClient extends Objective {

    @Unique
    private float woldsVaults$getPhaseDamageGoalFloat(int phase) {
        return GreedTrialVessel.displayGoal(phase, this.getOr(PHASE_DAMAGE_GOAL, 0));
    }

    @ModifyArg(method = "render",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/client/gui/GuiComponent;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIFFIIII)V",
                             ordinal = 1),
                    index = 5)
    private int makeProgressUseFloat(int pWidth, @Local boolean transitioning, @Local(ordinal = 2) int currentPhase) {
        final float goal = this.woldsVaults$getPhaseDamageGoalFloat(currentPhase);
        return (int)(15 + 130.0F * (transitioning ? 1.0F : Math.min(this.getOr(PHASE_DAMAGE_DEALT,0.0F)/goal, 1.0F)));
    }

    @ModifyVariable(method = "render",
            at = @At("STORE"),
            remap = false, name = "damageText")
    private String makeTextUseFloat(String value, @Local boolean transitioning, @Local(ordinal = 2) int currentPhase) {
        final float dealt = this.getOr(PHASE_DAMAGE_DEALT,0.0F);
        final float goal = this.woldsVaults$getPhaseDamageGoalFloat(currentPhase);
        final float thr = 10000000000.0F; //10bn
        String format = (dealt>=thr ? "%.5e/":"%.0f/") + (goal>=thr ? "%.5e":"%.0f");
        return transitioning ? "PHASE SHIFT" : String.format(format, dealt, goal);
    }

}
