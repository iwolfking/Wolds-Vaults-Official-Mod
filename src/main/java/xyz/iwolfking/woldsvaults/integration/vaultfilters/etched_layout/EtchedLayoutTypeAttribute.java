package xyz.iwolfking.woldsvaults.integration.vaultfilters.etched_layout;

import iskallia.vault.util.StringUtils;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;

public class EtchedLayoutTypeAttribute extends StringAttribute {
    public EtchedLayoutTypeAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof LayoutModificationItem) {
            if(itemStack.getOrCreateTag().contains("layout")) {
               return StringUtils.convertToTitleCase(itemStack.getOrCreateTag().getString("layout"));
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "etched_layout_type";
    }
}
