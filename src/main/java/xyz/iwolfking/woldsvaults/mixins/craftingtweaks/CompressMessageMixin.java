package xyz.iwolfking.woldsvaults.mixins.craftingtweaks;

import net.blay09.mods.craftingtweaks.network.CompressMessage;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CompressMessage.class, remap = false)
public abstract class CompressMessageMixin {

//
//    @Unique
//    private static final ThreadLocal<Boolean> woldsVaults$insufficientInputs =
//            ThreadLocal.withInitial(() -> Boolean.FALSE);
//
//    @Redirect(
//        method = "compressMouseSlot",
//        at = @At(value = "INVOKE",
//                 target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
//        remap = true)
//    private static void woldsvaults$validateBeforeShrink(ItemStack mouseStack, int amount) {
//        if (mouseStack.getCount() < amount) {
//            woldsVaults$insufficientInputs.set(Boolean.TRUE);
//            return;
//        }
//        woldsVaults$insufficientInputs.set(Boolean.FALSE);
//        mouseStack.shrink(amount);
//    }
//
//    @ModifyArg(
//        method = "compressMouseSlot",
//        at = @At(value = "INVOKE",
//                 target = "Lnet/blay09/mods/craftingtweaks/network/CompressMessage;"
//                        + "addCraftedItemsToInventory(Lnet/minecraft/world/entity/player/Player;"
//                        + "Lnet/minecraft/world/item/ItemStack;I)V"),
//        index = 2)
//    private static int woldsvaults$zeroCraftsWhenInsufficient(int timesCrafted) {
//        if (woldsVaults$insufficientInputs.get()) {
//            woldsVaults$insufficientInputs.set(Boolean.FALSE);
//            return 0;
//        }
//        return timesCrafted;
//    }
}