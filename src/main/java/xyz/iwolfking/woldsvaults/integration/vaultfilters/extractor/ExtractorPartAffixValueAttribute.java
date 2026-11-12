package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;

import java.util.ArrayList;
import java.util.List;

/**
 * One affix of an extractor part together with the value it rolled, matched at or above that value.
 * The stored name is the affix's plain display name; the attribute's own value is the line the tooltip shows.
 */
public class ExtractorPartAffixValueAttribute extends ExtractorPartAffixAttribute {
    public final String name;
    public final float level;

    public ExtractorPartAffixValueAttribute(String value) {
        this(value, value, 0.0F);
    }

    public ExtractorPartAffixValueAttribute(String value, String name, float level) {
        super(value);
        this.name = name;
        this.level = level;
    }

    private static String displayName(PartData.Affix affix) {
        return ExtractorTooltips.affixName(affix.id) + " (" + ExtractorTooltips.statText(affix.stat, affix.value, affix.percent, true) + ")";
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        PartData part = ExtractorFilterHelper.part(itemStack);
        if (part == null) {
            return false;
        }
        for (PartData.Affix affix : part.affixes) {
            if (this.name.equals(ExtractorTooltips.affixName(affix.id)) && affix.value >= this.level) {
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
            attributes.add(new ExtractorPartAffixValueAttribute(displayName(affix), ExtractorTooltips.affixName(affix.id), affix.value));
        }
        return attributes;
    }

    @Override
    public void writeNBT(CompoundTag compoundTag) {
        super.writeNBT(compoundTag);
        compoundTag.putString(this.getNBTKey() + "_simple", this.name);
        compoundTag.putFloat(this.getNBTKey() + "_level", this.level);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag compoundTag) {
        String key = this.getNBTKey();
        return new ExtractorPartAffixValueAttribute(compoundTag.getString(key), compoundTag.getString(key + "_simple"), compoundTag.getFloat(key + "_level"));
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part_affix_value";
    }
}
