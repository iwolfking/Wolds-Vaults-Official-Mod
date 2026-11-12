package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.entity.VaultStormEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.lib.TrackingStorm;

import java.util.UUID;

/**
 * Stormcrown storm tracking: a storm given a target moves toward the target's XZ at its tracking speed
 * each server tick and hovers a fixed height above the target's head.
 */
@Mixin(VaultStormEntity.class)
public abstract class MixinVaultStormEntityTracking extends Entity implements TrackingStorm {
    @Unique
    private static final double woldsvaults$HOVER_MARGIN = 6.0D;

    @Unique
    private UUID woldsvaults$trackedTarget;

    @Unique
    private float woldsvaults$trackSpeed;

    protected MixinVaultStormEntityTracking(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void woldsvaults$track(UUID target, float blocksPerSecond) {
        this.woldsvaults$trackedTarget = target;
        this.woldsvaults$trackSpeed = blocksPerSecond;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void woldsvaults$followTarget(CallbackInfo ci) {
        if (this.woldsvaults$trackedTarget == null || !(this.level instanceof ServerLevel serverLevel) || this.isRemoved()) {
            return;
        }
        Entity entity = serverLevel.getEntity(this.woldsvaults$trackedTarget);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            this.woldsvaults$trackedTarget = null;
            return;
        }
        double step = this.woldsvaults$trackSpeed / 20.0D;
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        double scale = distance <= step || distance < 1.0E-6D ? 1.0D : step / distance;
        this.setPos(this.getX() + dx * scale, target.getY() + target.getBbHeight() + woldsvaults$HOVER_MARGIN, this.getZ() + dz * scale);
    }
}
