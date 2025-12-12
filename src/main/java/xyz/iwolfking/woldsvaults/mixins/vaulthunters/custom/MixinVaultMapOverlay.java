package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import iskallia.vault.client.render.hud.module.vault.VaultMinimapModule;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Mixin(value = VaultMinimapModule.class, remap = false)
public class MixinVaultMapOverlay {

    @Inject(method = "renderVault", at = @At("HEAD"), cancellable = true)
    private void cancelRender(ModuleRenderContext context, Vault vault, Listener listener, CallbackInfo ci) {
        if(!WoldsVaultsConfig.CLIENT.showVanillaVaultMap.get()) {
            ci.cancel();
        }
    }
}
