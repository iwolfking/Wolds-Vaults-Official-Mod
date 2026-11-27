package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks a part item's icon from the variant stored in its {@link PartData}.
 *
 * <p>There is one item per part type but many icons, because the series, drill class or attachment id lives in NBT
 * rather than in the registry. The icon is chosen by a vanilla item-model override keyed on {@code woldsvaults:variant}:
 * its value is the 1-based position of the part's id in the parts table (series, drill classes or attachments, in
 * table order), so the overrides in {@code models/item/extractor_*.json} follow the table's entry order.
 */
public final class ExtractorIcons {
    private ExtractorIcons() {
    }

    /** Registers the variant property on every part item. */
    public static void register() {
        for (ExtractorPartType type : ExtractorPartType.values()) {
            ExtractorPartItem item = ModItems.EXTRACTOR_PARTS.get(type);
            if (item == null) {
                continue;
            }
            ItemProperties.register(item, WoldsVaults.id("variant"),
                    (stack, level, entity, seed) -> variantOf(stack, type));
        }
    }

    /** The table's ids for a part type, in the order the icons are numbered. */
    private static List<String> order(ExtractorPartType type) {
        ExtractorPartsConfig cfg = ModConfigs.EXTRACTOR_PARTS;
        return switch (type) {
            case DRILL -> new ArrayList<>(cfg.DRILL_CLASSES.keySet());
            case ATTACHMENT -> new ArrayList<>(cfg.ATTACHMENTS.keySet());
            default -> new ArrayList<>(cfg.SERIES.keySet());
        };
    }

    /**
     * 1-based index of the stack's variant within its type, or 1 when it has no part data yet: a creative-tab stack
     * has none, and the first icon is better than a missing texture.
     */
    private static float variantOf(ItemStack stack, ExtractorPartType type) {
        PartData part = ExtractorPartItem.getPart(stack);
        if (part == null) {
            return 1.0F;
        }
        String key = switch (type) {
            case DRILL -> part.drillClass;
            case ATTACHMENT -> part.attachmentId;
            default -> part.series;
        };
        int index = order(type).indexOf(key);
        if (index < 0) {
            WoldLogger.errorOnce("extractor_icons." + type.getId() + "." + key,
                    "Extractor {} has variant '{}', which the parts table does not list; showing the first icon.", type.getId(), key);
            return 1.0F;
        }
        return index + 1.0F;
    }
}
