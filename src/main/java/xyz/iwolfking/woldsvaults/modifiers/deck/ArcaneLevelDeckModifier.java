package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ArcaneLevelDeckModifier extends DeckModifier<DeckModifier.Config> {

    public ArcaneLevelDeckModifier(Config config) {
        super(config);
    }

    public ArcaneLevelDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        tooltip.add(new TextComponent("Arcane cards have +" + Math.round(this.getModifierValue()) + " ability level(s)."));
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }
}
