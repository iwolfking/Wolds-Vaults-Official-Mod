package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import iskallia.vault.client.render.hud.module.vault.VaultMapOverlayModule;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTerritory;

@Mixin(value = VaultMapOverlayModule.class, remap = false)
public class MixinVaultMapOverlayModule {
    // a pocket isn't on the vault's map, so the full map stays shut there
    @Inject(method = "renderVault", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$hideInPockets(ModuleRenderContext context, Vault vault, Listener listener, CallbackInfo ci) {
        if (PocketTerritory.inPocket(Minecraft.getInstance().player)) {
            ci.cancel();
        }
    }
}
