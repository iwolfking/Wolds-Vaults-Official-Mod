package xyz.iwolfking.woldsvaults.mixins.jade;

import iskallia.vault.core.vault.ClientVaults;
import mcp.mobius.waila.overlay.OverlayRenderer;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "jade")
        }
)
@Mixin(value = OverlayRenderer.class, remap = false)
public class MixinOverlayRenderer {
    @Inject(method = "renderOverlay()V", at = @At(value = "INVOKE", target = "Lmcp/mobius/waila/api/config/WailaConfig$ConfigGeneral;getDisplayMode()Lmcp/mobius/waila/api/config/WailaConfig$DisplayMode;"), cancellable = true)
    private static void dontRenderInVaultsWhenDisabled(CallbackInfo ci) {
        if(WoldsVaultsConfig.CLIENT.hideJadeInVaults.get() && ClientVaults.getActive().isPresent()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderOverlay(Lmcp/mobius/waila/overlay/TooltipRenderer;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true)
    private static void dontRenderInVaultsWhenDisabled2(CallbackInfo ci) {
        if(WoldsVaultsConfig.CLIENT.hideJadeInVaults.get() && ClientVaults.getActive().isPresent()) {
            ci.cancel();
        }
    }
}
