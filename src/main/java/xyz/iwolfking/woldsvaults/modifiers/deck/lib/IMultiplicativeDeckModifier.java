package xyz.iwolfking.woldsvaults.modifiers.deck.lib;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;

public interface IMultiplicativeDeckModifier {
    float getMultiplierValue(Card card, CardPos pos, CardDeck deck);
}
