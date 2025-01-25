package xyz.iwolfking.woldsvaults.mixins.hexal;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ram.talia.hexal.common.blocks.entity.BlockEntitySlipway;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "hexal")
    }
)
@Mixin(value = BlockEntitySlipway.class, remap = false)
public class MixinBlockEntitySlipway {
    @Inject(method = "serverTick", cancellable = true, at = @At(value="HEAD"))
    private void serverTick(CallbackInfo ci) {
        if (WoldsVaultsConfig.COMMON.disableWanderingWispSpawning.get()) {
            ci.cancel();
        }
    }
}
