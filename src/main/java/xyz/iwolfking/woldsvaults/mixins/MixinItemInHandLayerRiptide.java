package xyz.iwolfking.woldsvaults.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class MixinItemInHandLayerRiptide {
    /**
     * Skips the off-hand item while its holder is in a riptide spin, so a large off-hand item cannot block the view
     * of the dash.
     */
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$hideOffhandWhileRiptiding(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, HumanoidArm arm,
                                                      PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity.isAutoSpinAttack() && arm != entity.getMainArm()) {
            ci.cancel();
        }
    }
}
