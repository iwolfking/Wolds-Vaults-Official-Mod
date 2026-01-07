package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.item.BossRuneItem;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;

public class AlchemyIngredientTypeAttribute extends StringAttribute {
    public AlchemyIngredientTypeAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof AlchemyIngredientItem alchemyIngredientItem) {
            return alchemyIngredientItem.getType().getSerializedName();
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "alchemy_ingredient_type";
    }
}
