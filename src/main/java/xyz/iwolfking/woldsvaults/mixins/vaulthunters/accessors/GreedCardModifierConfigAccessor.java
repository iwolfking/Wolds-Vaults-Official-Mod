package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardNeighborType;
import iskallia.vault.core.card.modifier.card.GreedCardModifier;
import iskallia.vault.core.world.roll.FloatRoll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(value = GreedCardModifier.Config.class, remap = false)
public interface GreedCardModifierConfigAccessor {
    @Accessor("targetNeighborTypes")
    Set<CardNeighborType> getTargetNeighborTypes();

    @Accessor("targetColorFilter")
    void setTargetColorFilter(Set<CardEntry.Color> colors);

    @Accessor("multiplierPool")
    Map<Integer, FloatRoll> getMultiplierPool();
}
