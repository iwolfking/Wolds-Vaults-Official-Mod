package xyz.iwolfking.woldsvaults.mixins.ae2.terminal;

import appeng.menu.AEBaseMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseMenu.class, remap = false)
public class MixinAEBaseMenu {
    @Inject(method = "lockPlayerInventorySlot", at = @At("HEAD"), cancellable = true)
    private void terminalInCurioNoLock(int invSlot, CallbackInfo ci) {
        if (invSlot == -1) { // -1 is expected for curio
            ci.cancel();
        }
    }
}
