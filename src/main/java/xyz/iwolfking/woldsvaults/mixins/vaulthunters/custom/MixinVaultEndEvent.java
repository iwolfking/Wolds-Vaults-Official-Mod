package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@link CommonEvents#VAULT_END}, which the base mod declares and never invokes, at the head of
 * {@code Vault#releaseServer} - before {@code CommonEvents.release} drops the vault's listeners.
 */
@Mixin(value = Vault.class, remap = false)
public class MixinVaultEndEvent {

    @Inject(method = "releaseServer", at = @At("HEAD"))
    private void woldsvaults$invokeVaultEnd(CallbackInfo ci) {
        CommonEvents.VAULT_END.invoke((Vault) (Object) this);
    }
}
