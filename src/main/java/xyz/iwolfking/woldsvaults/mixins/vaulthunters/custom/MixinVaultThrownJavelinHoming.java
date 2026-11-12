package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.entity.VaultThrownJavelin;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.lib.HomingJavelin;
import xyz.iwolfking.woldsvaults.entities.projectiles.MagicMissileEntity;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

/**
 * Helm of the Warbound multi-hit: when a Scatter javelin splits on a block hit (or the ancient pre-split
 * fires), each child keeps its own pierced-entity set and homes in on the nearest enemy, so every child
 * of the split can hit a lone boss.
 */
@Mixin(VaultThrownJavelin.class)
public abstract class MixinVaultThrownJavelinHoming extends AbstractArrow implements HomingJavelin {
    @Unique
    private static final double woldsvaults$HOMING_RANGE = 16.0D;

    @Unique
    private static final float woldsvaults$TURN_RADIANS = 0.2617994F;

    @Shadow(remap = false)
    private IntOpenHashSet piercingIgnoreEntityIds;

    @Shadow(remap = false)
    private int bounceCount;

    @Shadow(remap = false)
    private boolean grounded;

    @Shadow(remap = false)
    private boolean maxPierced;

    @Unique
    private boolean woldsvaults$homing = false;

    @Unique
    private LivingEntity woldsvaults$homingTarget;

    protected MixinVaultThrownJavelinHoming(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    @Shadow(remap = false)
    public abstract Player getThrower();

    @Shadow(remap = false)
    public abstract VaultThrownJavelin.JavelinType getJavelinType();

    @Override
    public boolean woldsvaults$isHoming() {
        return this.woldsvaults$homing;
    }

    @Override
    public void woldsvaults$startHoming() {
        this.woldsvaults$homing = true;
        this.piercingIgnoreEntityIds = new IntOpenHashSet(30);
        this.setNoGravity(true);
    }

    @Inject(method = "createBouncingJavelin", at = @At("RETURN"), remap = false)
    private void woldsvaults$homingSplitChild(Level level, LivingEntity thrower, int bounceCount, CallbackInfoReturnable<VaultThrownJavelin> cir) {
        VaultThrownJavelin child = cir.getReturnValue();
        if (child == null || this.bounceCount != 0 || this.getJavelinType() != VaultThrownJavelin.JavelinType.SCATTER) {
            return;
        }
        if (woldsvaults$hasMultiHit(this.getThrower()) && child instanceof HomingJavelin homing) {
            homing.woldsvaults$startHoming();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void woldsvaults$steerHoming(CallbackInfo ci) {
        if (!this.woldsvaults$homing || this.level.isClientSide || this.grounded || this.inGround || this.maxPierced) {
            return;
        }
        LivingEntity target = this.woldsvaults$validTarget(this.woldsvaults$homingTarget) ? this.woldsvaults$homingTarget : this.woldsvaults$findTarget();
        this.woldsvaults$homingTarget = target;
        if (target == null) {
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        if (speed < 1.0E-4D) {
            return;
        }
        Vec3 aim = target.getBoundingBox().getCenter().subtract(this.position());
        if (aim.lengthSqr() < 1.0E-6D) {
            return;
        }
        Vec3 direction = MagicMissileEntity.rotateToward(motion.scale(1.0D / speed), aim.normalize(), woldsvaults$TURN_RADIANS);
        this.setDeltaMovement(direction.scale(speed));
        this.hasImpulse = true;
    }

    @Unique
    private boolean woldsvaults$validTarget(LivingEntity target) {
        return target != null && target.isAlive() && !target.isRemoved() && target.level == this.level
                && target.distanceToSqr(this) <= woldsvaults$HOMING_RANGE * woldsvaults$HOMING_RANGE
                && this.canHitEntity(target);
    }

    @Unique
    private LivingEntity woldsvaults$findTarget() {
        LivingEntity best = null;
        double bestDistance = woldsvaults$HOMING_RANGE * woldsvaults$HOMING_RANGE;
        for (LivingEntity candidate : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(woldsvaults$HOMING_RANGE), LivingEntity::isAlive)) {
            double distance = candidate.distanceToSqr(this);
            if (distance <= bestDistance && this.canHitEntity(candidate)) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    @Unique
    private static boolean woldsvaults$hasMultiHit(Player thrower) {
        if (thrower == null || !AttributeSnapshotHelper.canHaveSnapshot(thrower)) {
            return false;
        }
        return AttributeSnapshotHelper.getInstance().getSnapshot(thrower).getAttributeValue(ModGearAttributes.JAVELIN_MULTI_HIT, VaultGearAttributeTypeMerger.anyTrue());
    }
}
