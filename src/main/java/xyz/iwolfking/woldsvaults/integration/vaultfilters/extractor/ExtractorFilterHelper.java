package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

import javax.annotation.Nullable;

/** The rolled part behind a filtered stack: a fabricated part, or the part an identified blueprint describes. */
final class ExtractorFilterHelper {
    private ExtractorFilterHelper() {
    }

    @Nullable
    static PartData part(ItemStack stack) {
        if (stack.getItem() instanceof ExtractorPartItem) {
            return ExtractorPartItem.getPart(stack);
        }
        if (stack.getItem() instanceof ExtractorBlueprintItem) {
            return ExtractorBlueprintItem.getPart(stack);
        }
        return null;
    }
}
