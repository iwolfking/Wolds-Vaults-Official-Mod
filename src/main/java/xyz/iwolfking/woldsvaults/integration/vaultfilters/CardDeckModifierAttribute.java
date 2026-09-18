package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.card.modifier.deck.DummyDeckModifier;
import iskallia.vault.item.CardDeckItem;
import net.joseph.vaultfilters.attributes.abstracts.StringListAttribute;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class CardDeckModifierAttribute extends StringListAttribute {

    public CardDeckModifierAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        return "";
    }

    @Override
    public String getNBTKey() {
        return "card_deck_modifiers";
    }



    @Override
    public List<String> getValues(ItemStack itemStack) {
        if(itemStack.getItem() instanceof CardDeckItem) {

            if (!(Objects.requireNonNullElse(CardDeckItem.getModifiersRoll(itemStack), "")).isEmpty()) {
                return null;
            }

            CardDeck deck = CardDeckItem.getCardDeck(itemStack).orElse(null);
            if(deck == null) {
                return null;
            }

            return deck.getModifiers().stream().filter(modifier -> !(modifier instanceof DummyDeckModifier)).map(DeckModifier::getId).toList();
        }

        return null;
    }
}