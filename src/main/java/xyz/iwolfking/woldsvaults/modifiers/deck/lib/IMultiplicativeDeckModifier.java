package xyz.iwolfking.woldsvaults.modifiers.deck.lib;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;

import javax.annotation.Nullable;

public interface IMultiplicativeDeckModifier {
    float getMultiplierValue(Card card, CardPos pos, CardDeck deck);

    /**
     * Cores that share a group add their bonuses inside one factor; different groups multiply each
     * other. Null puts this core in a group of its own.
     */
    @Nullable
    default String wv$multGroup() {
        return null;
    }
}
