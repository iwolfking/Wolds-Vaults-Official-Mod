package xyz.iwolfking.woldsvaults.mixins.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ProjectileGlyphTags;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcherGlyphScale {

    /** Draws a projectile resized by Size Up or Size Down at its augmented scale. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private <E extends Entity> void woldsvaults$scaleGlyphed(EntityRenderer<? super E> renderer, E entity, float yaw, float partialTicks,
                                                                PoseStack pose, MultiBufferSource buffers, int light, Operation<Void> original) {
        if (!(entity instanceof Projectile) || !entity.getPersistentData().contains(ProjectileGlyphTags.SCALE)) {
            original.call(renderer, entity, yaw, partialTicks, pose, buffers, light);
            return;
        }
        float scale = ProjectileGlyphTags.scale(entity);
        pose.pushPose();
        pose.scale(scale, scale, scale);
        original.call(renderer, entity, yaw, partialTicks, pose, buffers, light);
        pose.popPose();
    }
}
