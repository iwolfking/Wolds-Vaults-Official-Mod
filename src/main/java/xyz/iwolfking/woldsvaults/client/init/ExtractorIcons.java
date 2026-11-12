package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Picks a part item's icon from the variant stored in its {@link PartData}.
 *
 * <p>There is one ITEM per part type but thirty-plus icons, because the series, drill class
 * or attachment id lives in NBT rather than in the registry. So the icon is chosen by a
 * vanilla item-model override keyed on {@code woldsvaults:variant}, and this class is the
 * one place the index is defined.
 *
 * <p>The lists below MUST stay in the same order as the overrides in
 * {@code models/item/extractor_*.json}. Both are generated from ORDER in the art
 * repository's {@code extractor_items/tools/assets.py} -- regenerate rather than editing
 * either by hand.
 */
public final class ExtractorIcons {
    private static final Map<String, List<String>> ORDER = new LinkedHashMap<>();

    static {
        List<String> series = List.of("wooden", "chromatic", "experimental", "armored", "echosteel");
        ORDER.put("chassis", series);
        ORDER.put("legs", series);
        ORDER.put("power_core", series);
        ORDER.put("shield_array", series);
        ORDER.put("radiator", series);
        ORDER.put("scanner", series);
        ORDER.put("drill", List.of("standard", "laser", "greed_infuser", "archeology",
                "excavator", "refiner", "heavy", "juicer", "glue_derrick", "crystal",
                "containment"));
        ORDER.put("attachment", List.of("auxiliary_radiator", "secondary_shield_charger",
                "driver_override", "extra_plating", "fuel_injector", "battery_array",
                "ore_cleaner", "defense_turret", "system_reinforcement",
                "regenerative_nanobots"));
    }

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

    /**
     * 1-based index of the stack's variant within its type, or 1 when it has no part data
     * yet -- a creative-tab stack has none, and falling back to the first icon is better
     * than a missing texture.
     */
    private static float variantOf(ItemStack stack, ExtractorPartType type) {
        List<String> order = ORDER.get(type.getId());
        if (order == null) {
            return 1.0F;
        }
        PartData part = ExtractorPartItem.getPart(stack);
        if (part == null) {
            return 1.0F;
        }
        String key = switch (type) {
            case DRILL -> part.drillClass;
            case ATTACHMENT -> part.attachmentId;
            default -> part.series;
        };
        int index = order.indexOf(key);
        if (index < 0) {
            WoldLogger.error("Extractor {} has unknown variant '{}'; showing the first icon.", type.getId(), key);
            return 1.0F;
        }
        return index + 1.0F;
    }
}
