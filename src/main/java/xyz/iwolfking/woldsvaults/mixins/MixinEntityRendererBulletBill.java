package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.bullet.BulletBillRenderer;

/**
 * Keeps a giant Bullet Bill on screen while any of it is in view. Its player's own box is tiny by comparison, so going
 * by that the whole model would vanish whenever the player at its heart went off screen.
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRendererBulletBill {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$giantBillInView(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player) {
            AABB bounds = BulletBillRenderer.giantBounds(player);
            if (bounds != null) {
                cir.setReturnValue(frustum.isVisible(bounds));
            }
        }
    }
}
