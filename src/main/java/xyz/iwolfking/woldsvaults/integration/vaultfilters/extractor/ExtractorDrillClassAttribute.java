package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;

public class ExtractorDrillClassAttribute extends StringAttribute {
    public ExtractorDrillClassAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        PartData part = ExtractorFilterHelper.part(itemStack);
        if (part == null || part.type != ExtractorPartType.DRILL || part.drillClass == null || part.drillClass.isEmpty()) {
            return null;
        }
        return ExtractorTooltips.drillClassName(part.drillClass);
    }

    @Override
    public String getTranslationKey() {
        return "extractor_drill_class";
    }
}
