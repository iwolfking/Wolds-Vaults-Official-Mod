package xyz.iwolfking.woldsvaults.client.screens.stats;

import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import net.minecraft.network.chat.TextColor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.StatScreenConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.StatLabelElementBuilderAccessor;
import xyz.iwolfking.woldsvaults.stats.StatSources;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies the generated Statistics tab layout ({@link StatScreenConfig}) to the H screen's stat rows: value colours
 * from the_vault colors.json tags and the player stat categories. Without the table the rows keep the base look and
 * the list stays flat; every such gap is logged once.
 */
public final class StatScreenLayout {
    private static final int MISSING_COLOR = 0xFF00AA;
    private static final Set<String> REPORTED = new HashSet<>();

    private StatScreenLayout() {
    }

    /** Colours the prominent rows beside the player model, matched by label. */
    public static void styleProminent(List<StatLabelElementBuilder<?>> rows) {
        StatScreenConfig table = table();
        for (StatLabelElementBuilder<?> row : rows) {
            String label = label(row);
            String key = StatSources.PROMINENT_PREFIX + label;
            ((StatRowStyle) row).woldsvaults$setStatKey(key);
            if (StatSources.has(key)) {
                ((StatRowStyle) row).woldsvaults$syncFrom(key);
            }
            if (table == null) {
                continue;
            }
            String tag = table.prominent.get(label);
            if (tag == null) {
                report("prominent:" + label, "Statistics tab: prominent stat '" + label + "' has no colour in the stat screen table; drawn plain");
                continue;
            }
            ((StatRowStyle) row).woldsvaults$setValueColor(color(tag));
        }
    }

    /** Keys and colours the player stat rows; rows not built from a gear attribute are keyed by label. */
    public static void stylePlayer(List<StatLabelElementBuilder<?>> rows) {
        StatScreenConfig table = table();
        for (StatLabelElementBuilder<?> row : rows) {
            StatRowStyle style = (StatRowStyle) row;
            if (style.woldsvaults$getStatKey() == null) {
                style.woldsvaults$setStatKey(StatScreenConfig.LABEL_PREFIX + label(row));
            }
            if (StatSources.has(style.woldsvaults$getStatKey())) {
                style.woldsvaults$syncFrom(style.woldsvaults$getStatKey());
            }
            if (table == null) {
                continue;
            }
            StatScreenConfig.Row entry = table.stats.get(style.woldsvaults$getStatKey());
            if (entry != null) {
                style.woldsvaults$setValueColor(color(entry.color));
            }
        }
    }

    /**
     * The player rows grouped by category in table order, each group sorted by label. A row the table does not list
     * goes into the last category with an error logged. Null when the table is missing, so the caller keeps the flat
     * list.
     */
    @Nullable
    public static Map<String, List<StatLabelElementBuilder<?>>> group(List<StatLabelElementBuilder<?>> rows) {
        StatScreenConfig table = table();
        if (table == null) {
            return null;
        }
        Map<String, List<StatLabelElementBuilder<?>>> groups = new LinkedHashMap<>();
        table.categories.forEach(category -> groups.put(category, new ArrayList<>()));
        String last = table.categories.get(table.categories.size() - 1);
        for (StatLabelElementBuilder<?> row : rows) {
            String key = ((StatRowStyle) row).woldsvaults$getStatKey();
            StatScreenConfig.Row entry = key == null ? null : table.stats.get(key);
            if (entry == null) {
                report("stat:" + key, "Statistics tab: stat " + key + " ('" + label(row) + "') is not in the stat screen table; listed under " + last);
                groups.get(last).add(row);
            } else {
                groups.get(entry.category).add(row);
            }
        }
        Comparator<StatLabelElementBuilder<?>> byLabel = Comparator.comparing(StatScreenLayout::label);
        groups.values().forEach(list -> list.sort(byLabel));
        return groups;
    }

    private static String label(StatLabelElementBuilder<?> row) {
        return ((StatLabelElementBuilderAccessor) row).woldsvaults$getLabelSupplier().get();
    }

    private static TextColor color(String tag) {
        TextColor color = iskallia.vault.init.ModConfigs.COLORS.getColor(tag);
        if (color.getValue() == MISSING_COLOR) {
            report("color:" + tag, "Statistics tab: colour tag '" + tag + "' is not in the_vault colors.json; drawn in the missing colour");
        }
        return color;
    }

    @Nullable
    private static StatScreenConfig table() {
        if (ModConfigs.STAT_SCREEN == null) {
            report("table", "Statistics tab: the generated stat screen table is missing; stats are shown as the plain flat list");
        }
        return ModConfigs.STAT_SCREEN;
    }

    private static void report(String key, String message) {
        if (REPORTED.add(key)) {
            WoldsVaults.LOGGER.error(message);
        }
    }
}
