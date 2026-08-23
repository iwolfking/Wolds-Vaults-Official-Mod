package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.boss.goal.VesselMeleeAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;

/** The Champion's version of the Vessel combat loop; anything without {@code greed_champion} is untouched. */
@Mixin(value = VesselMeleeAttackGoal.class, remap = false)
public abstract class MixinVesselMeleeAttackGoal {

    @Shadow @Final private TheVesselEntity vessel;

    @Shadow private int pathRecalcCooldown;

    @Unique
    private int woldsvaults$outOfPositionTicks;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = true)
    private void woldsvaults$championCombatLoop(CallbackInfo ci) {
        if (!VaultChampion.isChampion(this.vessel)) {
            return;
        }
        ci.cancel();
        LivingEntity target = this.vessel.getTarget();
        if (target == null) {
            this.woldsvaults$outOfPositionTicks = 0;
            return;
        }
        this.vessel.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.vessel.isAttacking()) {
            return;
        }
        double distSq = this.vessel.distanceToSqr(target);
        if (this.woldsvaults$chargeLightning(target, distSq)) {
            return;
        }
        if (distSq > 144.0D && this.vessel.canStartTeleport()) {
            this.vessel.getNavigation().stop();
            this.vessel.startRunawayClap();
            return;
        }
        if (distSq > 25.0D && distSq <= 144.0D && this.vessel.canStartMeleeAttack() && this.vessel.tryDashAttack()) {
            return;
        }
        this.woldsvaults$repath(target, distSq);
        double reach = this.vessel.getAttackReach();
        double reachSq = reach * reach + target.getBbWidth();
        if (distSq <= reachSq && this.vessel.canStartMeleeAttack()) {
            this.vessel.startMeleeAttack();
        }
    }

    /**
     * One counter shared by three ways to earn it: standing above the Champion, well below it, or too
     * far away. The forced storm bypasses the handler's own cooldown.
     */
    @Unique
    private boolean woldsvaults$chargeLightning(LivingEntity target, double distSq) {
        GreedChampionConfig.Lightning lightning = VaultChampion.config().getLightning();
        double heightDiff = target.getY() - this.vessel.getY();
        boolean outOfPosition = heightDiff > lightning.aboveBlocks
                || -heightDiff > lightning.belowBlocks
                || distSq > lightning.farBlocks * lightning.farBlocks;
        if (!outOfPosition) {
            this.woldsvaults$outOfPositionTicks = 0;
            return false;
        }
        if (++this.woldsvaults$outOfPositionTicks < lightning.chargeTicks) {
            return false;
        }
        this.woldsvaults$outOfPositionTicks = 0;
        return this.vessel.forceSpellLightningStorm();
    }

    /** Recalculates the path about twice as often as the base goal does. */
    @Unique
    private void woldsvaults$repath(LivingEntity target, double distSq) {
        this.pathRecalcCooldown = Math.max(0, this.pathRecalcCooldown - 1);
        if (this.pathRecalcCooldown > 0) {
            return;
        }
        this.vessel.getNavigation().moveTo(target, 1.0D);
        this.pathRecalcCooldown = 2 + this.vessel.getRandom().nextInt(4);
        if (distSq > 1024.0D) {
            this.pathRecalcCooldown += 5;
        } else if (distSq > 256.0D) {
            this.pathRecalcCooldown += 2;
        }
    }
}
