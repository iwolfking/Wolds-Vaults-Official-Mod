package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;

public class ExtractorBlueprintIdentifiedAttribute extends BooleanAttribute {
    public ExtractorBlueprintIdentifiedAttribute(Boolean value) {
        super(true);
    }

    @Override
    public Boolean getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ExtractorBlueprintItem)) {
            return null;
        }
        return ExtractorBlueprintItem.isIdentified(itemStack) ? Boolean.TRUE : null;
    }

    @Override
    public String getTranslationKey() {
        return "extractor_blueprint_identified";
    }
}
