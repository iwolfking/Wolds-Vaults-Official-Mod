package xyz.iwolfking.woldsvaults.integration.vaultfilters.deck;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardDeckItem;
import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;

public class CardDeckHasModifierAttribute extends BooleanAttribute {

    public CardDeckHasModifierAttribute(Boolean value) {
        super(value);
    }

    @Override
    public String getTranslationKey() {
        return "card_deck_has_modifier";
    }

    @Override
    public Boolean getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof CardDeckItem) {
            return CardDeckItem.getModifiersRoll(itemStack) != null;
        }

        return null;
    }
}
