package xyz.iwolfking.woldsvaults.mixins.sophisticatedcore;

import com.bawnorton.mixinsquared.TargetHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.radimous.moddeditemborders.ModdedItemBorders;

/**
 * Modded Item Borders draws the rarity border at the head of the storage screen's {@code renderSlot}, before the item.
 * The border is depth-tested above the item, so every border pixel cuts the sprite. Moves the border to right after the
 * item, matching where Item Borders draws it in vanilla container screens.
 */
@Restriction(require = {
        @Condition(type = Condition.Type.MOD, value = "sophisticatedcore"),
        @Condition(type = Condition.Type.MOD, value = "itemborders"),
        @Condition(type = Condition.Type.MOD, value = "moddeditemborders")
})
@Mixin(value = StorageScreenBase.class, priority = 1500, remap = false)
public abstract class MixinStorageScreenBaseItemBorders {
    @TargetHandler(
        mixin = "com.radimous.moddeditemborders.mixin.sophisticatedcore.MixinStorageScreenBase",
        name = "renderSlot"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$skipEarlyItemBorder(PoseStack poseStack, Slot slot, CallbackInfo originalCi, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderStack", at = @At(value = "INVOKE", remap = true, shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/item/ItemStack;II)V"))
    private void woldsvaults$drawItemBorderOverItem(PoseStack poseStack, int x, int y, ItemStack stack, boolean quickReplace, String countText, CallbackInfo ci) {
        ModdedItemBorders.render(poseStack, stack, x, y);
    }
}
