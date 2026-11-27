package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.card.modifier.deck.DeckModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DeckModifier.class, remap = false)
public interface DeckModifierValueAccessor {
    @Accessor("modifierValue")
    void woldsvaults$setModifierValue(float value);
}
