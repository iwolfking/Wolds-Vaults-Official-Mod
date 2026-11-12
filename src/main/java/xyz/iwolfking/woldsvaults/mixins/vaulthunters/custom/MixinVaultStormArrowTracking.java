package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.entity.IPlayerAttackIgnore;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.entity.entity.VaultStormArrow;
import iskallia.vault.entity.entity.VaultStormEntity;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.lib.TrackingStorm;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.modifiers.gear.unique.StormTrackingAttribute;

import java.util.Comparator;

/**
 * Stormcrown storm tracking: when the owner has {@code storm_tracking} for this storm's ability, the storm
 * spawned on hit locks onto the hit mob, or else the mob closest to the hit point.
 */
@Mixin(VaultStormArrow.class)
public abstract class MixinVaultStormArrowTracking extends AbstractArrow {
    @Unique
    private static final double woldsvaults$TARGET_RANGE = 16.0D;

    protected MixinVaultStormArrowTracking(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void woldsvaults$trackStorm(HitResult result, CallbackInfo ci, @Local VaultStormEntity storm) {
        if (this.level.isClientSide || !(this.getOwner() instanceof ServerPlayer player) || !AttributeSnapshotHelper.canHaveSnapshot(player)) {
            return;
        }
        String abilityKey = storm.getStormArrowType() == VaultStormArrow.StormType.BASE ? "Storm_Arrow_Base" : "Storm_Arrow_Blizzard";
        StormTrackingAttribute tracking = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValueList(ModGearAttributes.STORM_TRACKING).stream()
                .filter(attribute -> attribute.isFlag() && abilityKey.equals(attribute.getAbilityKey()))
                .max(Comparator.comparing(StormTrackingAttribute::getSpeed))
                .orElse(null);
        if (tracking == null) {
            return;
        }
        LivingEntity target = null;
        if (result instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity hit && woldsvaults$isEnemy(player, hit)) {
            target = hit;
        }
        if (target == null) {
            target = woldsvaults$nearestEnemy(player, result.getLocation());
        }
        if (target != null && storm instanceof TrackingStorm trackingStorm) {
            trackingStorm.woldsvaults$track(target.getUUID(), tracking.getSpeed());
        }
    }

    @Unique
    private LivingEntity woldsvaults$nearestEnemy(ServerPlayer player, Vec3 center) {
        LivingEntity best = null;
        double bestDistance = woldsvaults$TARGET_RANGE * woldsvaults$TARGET_RANGE;
        for (LivingEntity candidate : this.level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(woldsvaults$TARGET_RANGE), entity -> woldsvaults$isEnemy(player, entity))) {
            double distance = candidate.distanceToSqr(center);
            if (distance <= bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    @Unique
    private static boolean woldsvaults$isEnemy(ServerPlayer player, LivingEntity entity) {
        return entity.isAlive() && !(entity instanceof Player) && !(entity instanceof PetEntity) && !(entity instanceof EternalEntity)
                && !IPlayerAttackIgnore.isIgnoredByPlayerAttack(player, entity);
    }
}
