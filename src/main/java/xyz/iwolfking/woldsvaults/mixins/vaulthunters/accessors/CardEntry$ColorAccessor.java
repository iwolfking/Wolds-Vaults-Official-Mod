package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.card.CardEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CardEntry.Color.class, remap = false)
public interface CardEntry$ColorAccessor {
    @Accessor("color")
    int getColor();
}
