package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.RoyaleVaultCache;
import xyz.iwolfking.woldsvaults.api.util.VaultMapTierCache;

@Mixin(value = Vault.class, remap = false)
public class MixinVaultCacheInvalidator {

    @Inject(method = "releaseServer", at = @At("HEAD"))
    private void onReleaseServer(CallbackInfo ci) {
        Vault vault = (Vault) (Object) this;
        if (vault.has(Vault.ID)) {
            RoyaleVaultCache.invalidate(vault.get(Vault.ID));
            VaultMapTierCache.invalidate(vault.get(Vault.ID));
        }
    }
}