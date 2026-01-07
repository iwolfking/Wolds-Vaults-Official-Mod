package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.alchemy.CatalystItem;

public class CatalystItemAttribute extends BooleanAttribute {
    public CatalystItemAttribute(Boolean value) {
        super(true);
    }

    public Boolean getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof CatalystItem)) {
            return null;
        }
        return itemStack.getItem() instanceof CatalystItem;
    }

    public String getTranslationKey() {
        return "catalyst_item_attribute";
    }
}
