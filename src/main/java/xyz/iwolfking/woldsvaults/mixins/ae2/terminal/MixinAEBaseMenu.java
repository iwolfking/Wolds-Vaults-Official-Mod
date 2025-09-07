package xyz.iwolfking.woldsvaults.mixins.ae2.terminal;

import appeng.menu.AEBaseMenu;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "ae2")
    }
)
@Mixin(value = AEBaseMenu.class, remap = false)
public class MixinAEBaseMenu {
    @Inject(method = "lockPlayerInventorySlot", at = @At("HEAD"), cancellable = true)
    private void terminalInCurioNoLock(int invSlot, CallbackInfo ci) {
        if (invSlot == -1) { // -1 is expected for curio
            ci.cancel();
        }
    }
}
