package xyz.iwolfking.woldsvaults.mixins.servertransfer;

import com.blamejared.crafttweaker.impl.script.RecipeManagerScriptLoader;
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
        @Condition(type = Condition.Type.MOD, value = "crafttweaker")
    }
)
@Mixin(value = RecipeManagerScriptLoader.class, remap = false)
public class MixinRecipeManagerScriptLoader {
    @Inject(method = "updateState", at = @At("HEAD"), cancellable = true)
    private static void disableCraftTweaker(CallbackInfo ci){
        if (ServerTransferState.disableCraftTweaker) {
            WoldLogger.debug("CraftTweaker recipe sync disabled, skipping updateState");
            ServerTransferState.disableCraftTweaker = false;
            ci.cancel();
        }
    }
}
