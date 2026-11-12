package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

public class ExtractorPartTypeAttribute extends StringAttribute {
    public ExtractorPartTypeAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ExtractorPartItem item) {
            return item.getPartType().getDisplayName();
        }
        if (itemStack.getItem() instanceof ExtractorBlueprintItem blueprint) {
            return blueprint.getPartType().getDisplayName();
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part_type";
    }
}
