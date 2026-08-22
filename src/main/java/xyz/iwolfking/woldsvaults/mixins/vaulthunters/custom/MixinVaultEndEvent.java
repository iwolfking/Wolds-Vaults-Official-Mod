package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@link CommonEvents#VAULT_END}, which the base mod declares and never invokes.
 *
 * <p>Verified against {@code the_vault-1.18.2-3.21.6.6884.jar}: the only class referencing the
 * event is {@code CommonEvents} itself, so every listener the addon registers on it was dead. The
 * teardown that {@code GodNodeState}, the medallion vault state, the milestone scratch and the
 * four god trees all describe as their vault-end contract therefore never ran.
 *
 * <p>{@code Vault#releaseServer} is the real end of a server-side vault: {@code
 * ClassicListenersLogic} calls it once the last listener has left, immediately after setting
 * {@code Vault.FINISHED} and marking the world for deletion. The injection is at HEAD because
 * {@code releaseServer} opens by calling {@code CommonEvents.release(this)}, which drops every
 * listener keyed by this vault - invoking any later would deliver the event to an already
 * released bus.
 */
@Mixin(value = Vault.class, remap = false)
public class MixinVaultEndEvent {

    @Inject(method = "releaseServer", at = @At("HEAD"))
    private void woldsvaults$invokeVaultEnd(CallbackInfo ci) {
        CommonEvents.VAULT_END.invoke((Vault) (Object) this);
    }
}
