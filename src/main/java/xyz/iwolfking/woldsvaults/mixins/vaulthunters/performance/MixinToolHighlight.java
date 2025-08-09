package xyz.iwolfking.woldsvaults.mixins.vaulthunters.performance;

import iskallia.vault.item.tool.ToolHighlight;
import net.minecraftforge.client.event.DrawSelectionEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ToolHighlight.class, remap = false)
public class MixinToolHighlight {

    // Your performance sucks, go away
    @Inject(method = "onBlockHighlight", at = @At("HEAD"), cancellable = true)
    private static void onBlockHighlight(DrawSelectionEvent.HighlightBlock event, CallbackInfo ci) {
        ci.cancel();
    }


}
