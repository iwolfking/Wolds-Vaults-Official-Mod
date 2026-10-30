package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.vault.objective.AwardCrateObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.DeckModifiersHelper;

import java.util.List;

@Mixin(value = AwardCrateObjective.class, remap = false)
public class MixinAwardCrateObjective {

    @WrapOperation(method = "lambda$awardCrate$6", at = @At(value = "INVOKE", target = "Liskallia/vault/core/card/CardDeck;getModifiersOfType(Ljava/lang/Class;)Ljava/util/List;"))
    private static <T extends DeckModifier<?>> List<T> useWoldsDeckHelperInstead(CardDeck instance, Class<T> modifierClass, Operation<List<T>> original) {
        return DeckModifiersHelper.getDeckModifiersOfType(instance, modifierClass);
    }
}
