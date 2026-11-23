package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.bullet.BulletBillRenderer;

/**
 * The third person views of a Bullet Bill. After vanilla or Shoulder Surfing has placed the camera, it's moved to chase
 * the Bill from straight behind and a little above, trailing a moment behind its turns; a giant Bill's front view swings
 * round to one side instead, where its eye and teeth show. Blocks still pull the camera in, as they do normally.
 */
@Mixin(Camera.class)
public abstract class MixinCameraBulletBill {
    @Shadow
    private Vec3 position;

    @Shadow
    private float xRot;

    @Shadow
    private float yRot;

    @Shadow
    protected abstract void move(double forward, double up, double left);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPosition(Vec3 at);

    @Shadow
    private double getMaxZoom(double distance) {
        throw new AssertionError();
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void woldsvaults$billView(BlockGetter level, Entity entity, boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
        if (!detached || !(entity instanceof Player player)) {
            return;
        }
        BulletBillRenderer.BillView view = BulletBillRenderer.billView(player, this.yRot, this.xRot, mirror, partialTick);
        if (view == null) {
            return;
        }
        Vec3 fromPosition = this.position;
        float fromYaw = this.yRot;
        float fromPitch = this.xRot;
        this.setPosition(view.pivot());
        this.setRotation(view.yaw(), view.pitch());
        this.move(0.0, view.rise(), 0.0);
        this.move(-this.getMaxZoom(view.distance()), 0.0, 0.0);
        if (view.blend() < 1.0F) {
            this.setPosition(fromPosition.lerp(this.position, view.blend()));
            this.setRotation(Mth.rotLerp(view.blend(), fromYaw, view.yaw()), Mth.lerp(view.blend(), fromPitch, view.pitch()));
        }
    }
}
