package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.util.VaultUtil;

@Mixin(value = Vault.class, remap = false)
public class MixinVault {
    @Inject(method = "releaseClient", at = @At("HEAD"))
    private void releaseCorrupted(CallbackInfo ci) {
        if (VaultUtil.isVaultCorrupted) {
            Minecraft mc = Minecraft.getInstance();
            GameRenderer render = mc.gameRenderer;

            if (render.currentEffect() != null) { // Check if any shader is active
                render.shutdownEffect(); // Unload the current shader
            }
            VaultUtil.isVaultCorrupted = false;
        }
    }
}
