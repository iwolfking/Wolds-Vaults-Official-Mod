package xyz.iwolfking.woldsvaults.mixins.vaulthunters.multicell;

import iskallia.vault.core.vault.Vault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellRooms;

@Mixin(value = Vault.class, remap = false)
public abstract class MixinVaultMultiCell {

    /** Drops the vault's multi-cell rooms when it unloads; initialisation registers them again on the next load. */
    @Inject(method = "releaseServer", at = @At("HEAD"))
    private void woldsvaults$releaseMultiCellRooms(CallbackInfo ci) {
        Vault vault = (Vault) (Object) this;
        if (vault.has(Vault.ID)) {
            MultiCellRooms.release(vault.get(Vault.ID));
        }
    }
}
