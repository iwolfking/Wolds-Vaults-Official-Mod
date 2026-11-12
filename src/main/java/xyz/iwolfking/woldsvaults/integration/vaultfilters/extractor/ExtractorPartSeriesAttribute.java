package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;

public class ExtractorPartSeriesAttribute extends StringAttribute {
    public ExtractorPartSeriesAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        PartData part = ExtractorFilterHelper.part(itemStack);
        if (part == null || part.series == null || part.series.isEmpty()) {
            return null;
        }
        return ExtractorTooltips.seriesName(part.series);
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part_series";
    }
}
