package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.joseph.vaultfilters.attributes.abstracts.DoubleAttribute;
import net.joseph.vaultfilters.attributes.abstracts.IntAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

public class ExtractorPartQualityAttribute extends DoubleAttribute {
    public ExtractorPartQualityAttribute(Double value) {
        super(value);
    }

    @Override
    protected IntAttribute.NumComparator getComparator() {
        return IntAttribute.NumComparator.AT_LEAST;
    }

    @Override
    public Double getValue(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ExtractorBlueprintItem) {
            if (!ExtractorBlueprintItem.hasQuality(itemStack)) {
                return null;
            }
            return (double) ExtractorBlueprintItem.getQuality(itemStack);
        }
        if (itemStack.getItem() instanceof ExtractorPartItem) {
            PartData part = ExtractorPartItem.getPart(itemStack);
            return part == null ? null : (double) part.quality;
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part_quality";
    }
}
