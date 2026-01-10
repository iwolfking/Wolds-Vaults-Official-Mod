package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.card.CardModifier;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import net.joseph.vaultfilters.attributes.card.CardIsScalingAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.modifiers.deck.NitwitDeckModifier;

import java.util.List;
import java.util.Set;

@Mixin(value = CardEntry.class, remap = false)
public class MixinCardEntry {
    @Shadow
    private Set<String> groups;

    @Inject(method = "getSnapshotAttributes", at = @At(value = "INVOKE", target = "Liskallia/vault/core/card/modifier/card/CardModifier;getSnapshotAttributes(I)Ljava/util/List;"), cancellable = true)
    private void disableArcaneInNitwitDecks(int tier, CardPos pos, CardDeck deck, CallbackInfoReturnable<List<VaultGearAttributeInstance<?>>> cir) {
        if(!deck.getModifiersOfType(NitwitDeckModifier.class).isEmpty()) {
            if(this.groups.contains("Arcane")) {
                cir.setReturnValue(List.of());
            }
        }
    }
}
