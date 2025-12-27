package xyz.iwolfking.woldsvaults.mixins.effortlessbuilding;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.base.InventoryRetainerBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.world.BlockEvent;
import nl.requios.effortlessbuilding.EventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EventHandler.class, remap = true)
public class MixinEventHandler {
    @Inject(method = "onBlockPlaced", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private static void cancelPlacingInventoryRetainerBlocks(BlockEvent.EntityPlaceEvent event, CallbackInfo ci, @Local ServerPlayer player) {
        if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof InventoryRetainerBlock<?>) {
            event.setCanceled(true);
            ci.cancel();
        }
    }
}
