package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.DeckModifiersHelper;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.CardScaler$FilterAccessor;
import xyz.iwolfking.woldsvaults.modifiers.deck.CardNeighborTypeDeckModifier;

import java.util.Set;

@Mixin(value = CardScaler.class, remap = false)
public class MixinCardScaler {


    @WrapOperation(method = "getFrequency", at = @At(value = "INVOKE", target = "Ljava/util/Set;size()I"))
    private int multiplyTotalWithFrequencyBonus(Set<?> instance, Operation<Integer> original, @Local(name = "filter") CardScaler.Filter filter, @Local(name = "deck") CardDeck deck) {
        float increasedFrequency = 0.0F;
        for (CardNeighborTypeDeckModifier cardNeighborTypeDeckModifier : DeckModifiersHelper.getDeckModifiersOfType(deck, CardNeighborTypeDeckModifier.class)) {
            if(!((CardScaler$FilterAccessor)filter).getCardNeighborFilters().isEmpty() && cardNeighborTypeDeckModifier.getConfig().getValidNeighorTypes().containsAll(((CardScaler$FilterAccessor)filter).getCardNeighborFilters())) {
                increasedFrequency += cardNeighborTypeDeckModifier.getModifierValue();
            }
        }

        if(increasedFrequency > 0.0F) {
            return Math.round(original.call(instance) * increasedFrequency);
        }

        return original.call(instance);
    }
}
