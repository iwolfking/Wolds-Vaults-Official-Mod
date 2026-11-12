package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;

import java.util.ArrayList;
import java.util.List;

public class ExtractorPartAffixAttribute extends StringAttribute {
    public ExtractorPartAffixAttribute(String value) {
        super(value);
    }

    /** Which of the part's affixes this attribute speaks for; the base attribute takes prefixes and suffixes alike. */
    protected boolean shouldList(PartData.Affix affix) {
        return true;
    }

    @Override
    public String getValue(ItemStack itemStack) {
        return null;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        PartData part = ExtractorFilterHelper.part(itemStack);
        if (part == null) {
            return false;
        }
        for (PartData.Affix affix : part.affixes) {
            if (this.shouldList(affix) && this.value.equals(ExtractorTooltips.affixName(affix.id))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        List<ItemAttribute> attributes = new ArrayList<>();
        PartData part = ExtractorFilterHelper.part(itemStack);
        if (part == null) {
            return attributes;
        }
        for (PartData.Affix affix : part.affixes) {
            if (this.shouldList(affix)) {
                attributes.add(this.withValue(ExtractorTooltips.affixName(affix.id)));
            }
        }
        return attributes;
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part_affix";
    }
}
