package xyz.iwolfking.woldsvaults.mixins.quark;

import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.quark.base.handler.InventoryTransferHandler;

@Mixin(value = InventoryTransferHandler.class, remap = false)
public class MixinInventoryTransferHandler {
    @Inject(method = "accepts", at = @At("HEAD"), cancellable = true)
    private static void cancelSguiInteraction(AbstractContainerMenu container, Player player, CallbackInfoReturnable<Boolean> cir) {
        if(container instanceof VirtualScreenHandler) {
            cir.setReturnValue(false);
        }
    }
}
