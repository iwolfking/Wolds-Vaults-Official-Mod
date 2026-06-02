package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.event.ActiveFlagsCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(value = ActiveFlagsCheck.class, remap = false)
public class MixinActiveFlagsCheck {
    @Inject(method = "isAnyFlagActiveLuckyHit", at = @At(value = "HEAD"), cancellable = true)
    private static void isAnyFlagActiveLuckyHit(CallbackInfoReturnable<Boolean> cir) {
        if(WoldActiveFlags.IS_UNLUCKY_ATTACK.isSet())
            cir.setReturnValue(true);
    }
}
