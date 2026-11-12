package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

public class ExtractorPartRarityAttribute extends StringAttribute {
    public ExtractorPartRarityAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ExtractorBlueprintItem) {
            if (!ExtractorBlueprintItem.getData(itemStack).contains("Rarity")) {
                return null;
            }
            return ExtractorTooltips.rarityName(ExtractorBlueprintItem.getBlueprintRarity(itemStack));
        }
        if (itemStack.getItem() instanceof ExtractorPartItem) {
            PartData part = ExtractorPartItem.getPart(itemStack);
            return part == null ? null : ExtractorTooltips.rarityName(part.rarity);
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part_rarity";
    }
}
