package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.overlay.PlayerDamageOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.client.rampage.ClientRampageCdm;

@Mixin(value = PlayerDamageOverlay.class, remap = false)
public class MixinPlayerDamageOverlay {

    /**
     * Feeds the Rampage bonus to the damage indicator above the vault bar. Scoped to this call site;
     * {@code StatUtils#getAttackDamage} keeps reading the real registry, and 1.0 hides the row.
     */
    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/client/data/ClientDamageData;getCurrentDamageMultiplier()F"))
    private float woldsvaults$showRampageBonus() {
        return ClientRampageCdm.getMeleeFactor();
    }
}
