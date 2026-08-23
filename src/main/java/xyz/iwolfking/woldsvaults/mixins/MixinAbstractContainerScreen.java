package xyz.iwolfking.woldsvaults.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.renderers.AncientSlotDecorator;

/** Paints the ancient unique slot dressing in any container screen: background under, frame over the item. */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen {
    @Inject(method = "renderSlot", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void drawAncientSlotBackground(PoseStack poseStack, Slot slot, CallbackInfo ci) {
        AncientSlotDecorator.renderBackground(poseStack, slot.getItem(), slot.x, slot.y);
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void drawAncientSlotFrame(PoseStack poseStack, Slot slot, CallbackInfo ci) {
        AncientSlotDecorator.renderFrame(poseStack, slot.getItem(), slot.x, slot.y);
    }
}
