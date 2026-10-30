package xyz.iwolfking.woldsvaults.mixins.servertransfer;

import cofh.thermal.lib.common.ThermalRecipeManagers;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.servertransfer.ServerTransferState;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "thermal")
    }
)
@Mixin(value = ThermalRecipeManagers.class, remap = false)
public class MixinThermalRecipeManagers {
    @Inject(method = "refreshClient", at = @At("HEAD"), cancellable = true)
    private void disableThermalClientRefresh(CallbackInfo ci){
        if (ServerTransferState.disableThermalRecipeRefresh) {
            WoldLogger.debug("Thermal recipe refresh disabled, skipping");
            ServerTransferState.disableThermalRecipeRefresh = false;
            ci.cancel();
        }
    }
}
