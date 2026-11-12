package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.joseph.vaultfilters.attributes.abstracts.IntAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;

/** How much of an attachment's own wear pool is left, as whole percent. */
public class ExtractorAttachmentDurabilityAttribute extends IntAttribute {
    public ExtractorAttachmentDurabilityAttribute(Integer value) {
        super(value);
    }

    @Override
    protected NumComparator getComparator() {
        return NumComparator.AT_LEAST;
    }

    @Override
    public Integer getValue(ItemStack itemStack) {
        PartData part = ExtractorFilterHelper.part(itemStack);
        if (part == null || !part.isAttachment() || part.maxDurability <= 0) {
            return null;
        }
        return Math.round(part.durabilityFraction() * 100.0F);
    }

    @Override
    public String getTranslationKey() {
        return "extractor_attachment_durability";
    }
}
