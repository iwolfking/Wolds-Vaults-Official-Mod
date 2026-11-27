package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

public class IsExtractorPartAttribute extends BooleanAttribute {
    public IsExtractorPartAttribute(Boolean value) {
        super(true);
    }

    @Override
    public Boolean getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ExtractorPartItem)) {
            return null;
        }
        return ExtractorPartItem.getPart(itemStack) != null ? Boolean.TRUE : null;
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part";
    }
}
