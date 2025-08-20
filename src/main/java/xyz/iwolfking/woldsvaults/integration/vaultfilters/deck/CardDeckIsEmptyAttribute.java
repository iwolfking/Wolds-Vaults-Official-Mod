package xyz.iwolfking.woldsvaults.integration.vaultfilters.deck;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.item.CardDeckItem;
import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.minecraft.world.item.ItemStack;

public class CardDeckIsEmptyAttribute extends BooleanAttribute {

    public CardDeckIsEmptyAttribute(Boolean value) {
        super(value);
    }

    @Override
    public String getTranslationKey() {
        return "card_deck_empty";
    }

    @Override
    public Boolean getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof CardDeckItem) {
            CardDeck deck = CardDeckItem.getCardDeck(itemStack).orElse(null);
            if(deck == null) {
                return null;
            }

            return deck.getCards().isEmpty();
        }

        return null;
    }
}
