package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.RebirthObjective;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.milestones.trials.GreedTrialVessel;

import java.util.List;
import java.util.UUID;

/**
 * make the greed boss damage target use a float instead of an int capped at MAX_INT. Also hosts the
 * greed rank-up trial layer: a single phase won the moment the trial row's health pool is emptied.
 */
@Mixin(value = RebirthObjective.class, remap = false)
abstract class MixinRebirthObjective extends Objective {

    @Shadow @Final private List<UUID> phaseKnightIds;

    @Shadow protected abstract void completePhaseTransition(VirtualWorld world, Vault vault);

    /** The vault this objective belongs to, captured every tick; the objective holds no back-reference. */
    @Unique
    private Vault woldsVaults$vault;

    @Unique
    private static float woldsVaults$getPhaseDamageGoalFloat(int phase) {
        return (float)(50000.0 * Math.pow(2, phase));
    }

    /** Trials are single-phase, so the transition is refused outright. */
    @Inject(method = "checkPhaseTransition",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/core/vault/objective/RebirthObjective;getPhaseDamageGoal(I)I",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void checkPhaseTransition(CallbackInfo ci, @Local float damageDealt, @Local int currentPhase) {
        if (this.woldsVaults$vault != null && GreedTrialVessel.isTrial(this.woldsVaults$vault)) {
            ci.cancel();
            return;
        }
        if (damageDealt < woldsVaults$getPhaseDamageGoalFloat(currentPhase))
            ci.cancel();
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void woldsVaults$tickGreedTrial(VirtualWorld world, Vault vault, CallbackInfo ci) {
        this.woldsVaults$vault = vault;
        GreedTrialVessel.tick(this, world, vault);
    }

    /** A rank-up trial fails on death, replacing the base ending outright and running its own outro. */
    @Inject(method = "endGreedTrial", at = @At("HEAD"), cancellable = true)
    private void woldsVaults$failTrialOnDeath(VirtualWorld world, Vault vault, ServerPlayer player, CallbackInfo ci) {
        if (!GreedTrialVessel.isTrial(vault)) {
            return;
        }
        GreedTrialVessel.finish(this, world, vault, false);
        ci.cancel();
    }
  
    @ModifyConstant(method = "tickServer",
                    constant = @Constant(intValue = 1),
                    remap = false)
    private int onlyCountOnline(int constant, @Local(argsOnly = true) Vault vault) {
        return vault.get(Vault.CLOCK).has(TickClock.PAUSED) ? 0 : 1;
    }


    @Inject(method = "tickPhaseTransition", at = @At("TAIL"))
    private void transitionWhenMinionsAreDead(VirtualWorld world, Vault vault, CallbackInfo ci, @Local(name = "countdown") int countdown){
        if (countdown <= 390) {
            for (var knight : this.phaseKnightIds) {
                if (world.getEntity(knight) != null) {
                    return;
                }
            }
            this.set(RebirthObjective.PHASE_TRANSITION_COUNTDOWN, 0);
            this.completePhaseTransition(world, vault);
        }
    }
}
