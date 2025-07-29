package xyz.iwolfking.woldsvaults.mixins.inventoryhud;

import dlovin.inventoryhud.InventoryHUD;
import dlovin.inventoryhud.gui.InventoryGui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Mixin(value = InventoryGui.class, remap = false)
public class MixinInventoryHudRenderer {
    @Inject(method = "onPreRenderGui", at = @At("HEAD"), cancellable = true)
    private void disableInventoryGUIWhenVHIsEnabled(RenderGameOverlayEvent.Pre event, CallbackInfo ci) {
        if(WoldsVaultsConfig.CLIENT.showVanillaVaultHud.get()) {
            ci.cancel();
        }
    }
}
