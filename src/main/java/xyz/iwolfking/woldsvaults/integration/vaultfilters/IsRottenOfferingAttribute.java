package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.item.BossRuneItem;
import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.minecraft.world.item.ItemStack;

public class IsRottenOfferingAttribute extends BooleanAttribute {
    public IsRottenOfferingAttribute(Boolean value) {
        super(true);
    }

    public Boolean getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BossRuneItem)) {
            return null;
        }
        return itemStack.getOrCreateTag().contains("rotten");
    }

    public String getTranslationKey() {
        return "rotten_offering";
    }
}
