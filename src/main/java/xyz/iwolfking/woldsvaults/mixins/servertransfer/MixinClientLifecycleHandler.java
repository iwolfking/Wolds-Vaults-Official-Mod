package xyz.iwolfking.woldsvaults.mixins.servertransfer;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mezz.jei.forge.startup.ClientLifecycleHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.servertransfer.ServerTransferState;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "jei")
    }
)
@Mixin(value = ClientLifecycleHandler.class, remap = false)
public class MixinClientLifecycleHandler {
    @Inject(method = "startJei", at = @At("HEAD"), cancellable = true)
    private void disableJEIStart(CallbackInfo ci){
        if (ServerTransferState.disableJEIRefresh) {
            WoldLogger.debug("JEI refresh disabled, skipping startJei");
            ci.cancel();
        }
        ServerTransferState.disableJEIRefresh = false;
    }

    @Inject(method = "stopJei", at = @At("HEAD"), cancellable = true)
    private void disableJEIStop(CallbackInfo ci){
        if (ServerTransferState.disableJEIRefresh) {
            WoldLogger.debug("JEI refresh disabled, skipping stopJei");
            ci.cancel();
        }
    }
}
