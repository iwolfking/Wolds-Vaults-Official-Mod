package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.recipe.anvil.DeckSocketAnvilRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;

import java.util.List;

@Mixin(value = DeckSocketAnvilRecipe.class, remap = false)
public class MixinDeckSocketAnvilRecipe {
    @WrapOperation(method = "onCraft", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0))
    private int filterImplicitModifiersFromSize(List<DeckModifier<?>> instance, Operation<Integer> original) {
        return original.call(instance.stream().filter(deckModifier -> !(deckModifier instanceof ImplicitDeckModifier)).toList());
    }
}
