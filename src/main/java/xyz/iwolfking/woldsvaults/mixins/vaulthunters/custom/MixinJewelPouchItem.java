package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.JewelPouchItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = JewelPouchItem.class, remap = false)
public abstract class MixinJewelPouchItem {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void alwaysInstantOpen(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND) {
            if (!level.isClientSide()) {
                if(player instanceof FakePlayer) {
                    return;
                }
                if (instantOpen(player, stack, true)) {
                    cir.setReturnValue(InteractionResultHolder.success(stack));
                }
            }

           cir.setReturnValue(InteractionResultHolder.pass(stack));
        }
    }

    @Shadow
    private static boolean instantOpen(@NotNull Player player, ItemStack jewelPouch, boolean playEffects) {
        return false;
    }
}
