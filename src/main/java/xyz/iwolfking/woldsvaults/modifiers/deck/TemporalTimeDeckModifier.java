package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.card.modifier.deck.SlotDeckModifier;
import iskallia.vault.core.random.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.SlotDeckModifierAccessor;

import java.util.ArrayList;
import java.util.List;

public class TemporalTimeDeckModifier extends DeckModifier<DeckModifier.Config> {

    public TemporalTimeDeckModifier(Config config) {
        super(config);
    }

    public TemporalTimeDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        tooltip.add(new TextComponent("Temporal cards last twice as long."));
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }
}
