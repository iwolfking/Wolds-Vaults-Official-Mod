package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.overlay.PlayerDamageOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.client.rampage.ClientRampageCdm;

@Mixin(value = PlayerDamageOverlay.class, remap = false)
public class MixinPlayerDamageOverlay {

    /**
     * Repurposes the damage indicator above the vault bar to show the Rampage bonus.
     *
     * <p>The row used to show Rampage, then stopped: the_vault 3.21.6 removed Rampage's
     * {@code PlayerDamageHelper} registration, and that registry is the only thing the indicator
     * reads. What is left in it is Aspect of Berserk, the Berserker archetype and Barbarian's
     * rage - a number that means "all your damage" and moves rarely. The Rampage bonus is the one
     * that swings second to second and the one a player is actually watching, so it takes the row.
     * Aspect of Berserk keeps working exactly as before; it is simply no longer displayed.
     *
     * <p>Redirecting the call rather than the overlay leaves every piece of presentation the base
     * mod already implements in place - the icon, the placement, the {@code left_height}
     * bookkeeping, the {@code +N%} formatting, and the early-return that hides the row when the
     * value is 1.0, which is what makes a player with no Fury see nothing.
     *
     * <p>Scoped to this call site on purpose. {@code ClientDamageData} has a second consumer,
     * {@code StatUtils#getAttackDamage}, which feeds the character screen's damage estimate and
     * must keep reading the real registry; redirecting the getter itself would corrupt it.
     */
    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/client/data/ClientDamageData;getCurrentDamageMultiplier()F"))
    private float woldsvaults$showRampageBonus() {
        return ClientRampageCdm.getMeleeFactor();
    }
}
