package xyz.iwolfking.woldsvaults.integration.vaultfilters.deck;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardDeckItem;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

public class CardDeckTypeAttribute extends StringAttribute {
    public CardDeckTypeAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof CardDeckItem) {
            return ModConfigs.CARD_DECK.getName(CardDeckItem.getId(itemStack)).orElse(null);
        }

        return null;
    }

    @Override
    public String getTranslationKey() {
        return "card_deck_type";
    }
}
