package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;

public class AlchemyItemAttribute extends BooleanAttribute {
    public AlchemyItemAttribute(Boolean value) {
        super(true);
    }

    public Boolean getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof AlchemyIngredientItem)) {
            return null;
        }
        return itemStack.getItem() instanceof AlchemyIngredientItem;
    }

    public String getTranslationKey() {
        return "alchemy_item_attribute";
    }
}
