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

/**
 * The Vault Champion's version of the Vessel's combat loop.
 *
 * <p>The base goal is replaced outright for Champions rather than patched at three constants. Two of
 * the changes are new conditions rather than new numbers - a player below the Champion and a player
 * simply far away both now charge a lightning storm, where only a player above them did before - and
 * the counter they share has to live somewhere. Rewriting forty lines against a method this feature
 * already needs to restructure is more stable than three constant injectors bound to ordinal position
 * inside it.
 *
 * <p>The trial Vessel is untouched: the injection returns immediately for anything without the
 * {@code greed_champion} tag, and the base method runs as it always has.</p>
 */
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
     * One counter, three ways to earn it: standing above the Champion, standing well below it, or
     * simply being too far away. Sharing the counter means a player who is both far and below charges
     * one storm rather than two, and that stepping out of one violation straight into another does not
     * reset the clock.
     *
     * <p>The forced storm deliberately bypasses the handler's own cooldown, which is what makes staying
     * out of position genuinely unaffordable rather than merely discouraged.</p>
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

    /**
     * Recalculates the path about twice as often as the base goal does. A Champion moves fast enough
     * that the base four-to-ten tick gap leaves it overshooting corners, and there is only ever one of
     * these per player, so the extra pathfinding is not a cost worth avoiding.
     */
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
