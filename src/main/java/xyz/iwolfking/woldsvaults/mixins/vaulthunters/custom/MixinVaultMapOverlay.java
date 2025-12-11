package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Mixin(value = VaultMapOverlayRenderer.class, remap = false)
public class MixinVaultMapOverlay {

    @Inject(method = "eventHandler", at = @At("HEAD"), cancellable = true)
    private static void cancelRender(RenderGameOverlayEvent.Post event, CallbackInfo ci) {
        if(!WoldsVaultsConfig.CLIENT.showVanillaVaultMap.get()) {
            ci.cancel();
        }
    }
}
