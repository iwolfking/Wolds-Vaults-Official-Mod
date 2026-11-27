package xyz.iwolfking.woldsvaults.mixins.sophisticatedcore;

import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.renderers.AncientSlotDecorator;

/**
 * Sophisticated storage screens replace {@code renderSlot} without calling super, so the ancient unique slot dressing
 * from {@code MixinAbstractContainerScreen} never runs there. Paints it from their own item path instead.
 */
@Restriction(require = @Condition(type = Condition.Type.MOD, value = "sophisticatedcore"))
@Mixin(value = StorageScreenBase.class, remap = false)
public abstract class MixinStorageScreenBase {
    @Inject(method = "renderStack", at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/item/ItemStack;II)V"))
    private void woldsvaults$drawAncientSlotBackground(PoseStack poseStack, int x, int y, ItemStack stack, boolean quickReplace, String countText, CallbackInfo ci) {
        AncientSlotDecorator.renderBackground(poseStack, stack, x, y);
    }

    @Inject(method = "renderStack", at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void woldsvaults$drawAncientSlotFrame(PoseStack poseStack, int x, int y, ItemStack stack, boolean quickReplace, String countText, CallbackInfo ci) {
        AncientSlotDecorator.renderFrame(poseStack, stack, x, y);
    }
}
