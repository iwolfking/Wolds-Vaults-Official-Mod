package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.gui.screen.block.ReclamationAltarScreen;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;

import java.util.List;

@Mixin(value = ReclamationAltarScreen.class, remap = false)
public class MixinReclamationAltarScreen {
    @WrapOperation(method = "hasValidDeck", at = @At(value = "INVOKE", target = "Liskallia/vault/core/card/CardDeck;getModifiers()Ljava/util/List;"))
    private List<DeckModifier<?>> filterImplicitModifiers(CardDeck instance, Operation<List<DeckModifier<?>>> original) {
        return original.call(instance).stream().filter(deckModifier -> !(deckModifier instanceof ImplicitDeckModifier)).toList();
    }
}
