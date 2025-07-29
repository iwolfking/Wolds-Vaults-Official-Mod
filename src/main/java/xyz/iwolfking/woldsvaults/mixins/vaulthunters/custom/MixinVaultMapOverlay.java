package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.overlay.VaultMapOverlay;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Mixin(value = VaultMapOverlay.class, remap = false)
public class MixinVaultMapOverlay {
    @Unique
    private static boolean woldsvaults$initialized;

    @Unique
    private static boolean woldsvaults$shouldShowMap;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelRender(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height, CallbackInfo ci) {
        if(!WoldsVaultsConfig.CLIENT.showVanillaVaultMap.get()) {
            ci.cancel();
        }
    }
}
