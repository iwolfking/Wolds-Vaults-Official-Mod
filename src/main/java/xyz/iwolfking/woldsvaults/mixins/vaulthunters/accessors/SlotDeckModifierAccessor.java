package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.card.modifier.deck.SlotDeckModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SlotDeckModifier.class, remap = false)
public interface SlotDeckModifierAccessor {
    @Accessor("slotRoll")
    int getSlotRoll();
}
