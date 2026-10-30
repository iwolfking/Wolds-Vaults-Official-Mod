package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.card.CardNeighborType;
import iskallia.vault.core.card.CardScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(value = CardScaler.Filter.class, remap = false)
public interface CardScaler$FilterAccessor {
    @Accessor("neighborFilter")
    Set<CardNeighborType> getCardNeighborFilters();
}
