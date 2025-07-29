package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.render.InventoryHudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Mixin(value = InventoryHudRenderer.class, remap = false)
public class MixinInventoryHudRenderer {
    @Unique
    private static boolean woldsvaults$initialized;

    @Unique
    private static boolean woldsvaults$shouldShowHud;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void cancelRenderWhenDisabled(PoseStack poseStack, CallbackInfo ci) {
        if(!WoldsVaultsConfig.CLIENT.showVanillaVaultHud.get()) {
            ci.cancel();
        }
    }
}
