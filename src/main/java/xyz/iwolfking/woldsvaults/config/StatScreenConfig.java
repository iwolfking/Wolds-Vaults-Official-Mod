package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Layout of the H menu Statistics tab, generated as {@code vault_configs/gui/stat_screen/player_stats.json}: the
 * player stat categories in display order, each stat's category and value colour, and the value colours of the
 * prominent stats beside the player model. Colours are tag names from the_vault {@code colors.json}, the palette the
 * ability descriptions use. A stat is keyed by its gear attribute id, or {@code label:<name>} for rows that are not
 * built from a gear attribute.
 */
public class StatScreenConfig extends Config implements WoldsTable {
    public static final String LABEL_PREFIX = "label:";

    @Expose public List<String> categories;
    @Expose public Map<String, Row> stats;
    @Expose public Map<String, String> prominent;

    @Override
    public String getName() {
        return "gui" + File.separator + "stat_screen";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.categories == null || this.categories.isEmpty()) {
            return "no categories are listed";
        }
        Set<String> seen = new HashSet<>();
        for (String category : this.categories) {
            if (category == null || category.isBlank() || !seen.add(category)) {
                return "category '" + category + "' is blank or listed twice";
            }
        }
        if (this.stats == null || this.stats.isEmpty()) {
            return "no stats are listed";
        }
        for (Map.Entry<String, Row> entry : this.stats.entrySet()) {
            Row row = entry.getValue();
            if (row == null || row.category == null || !seen.contains(row.category)) {
                return "stat " + entry.getKey() + " has no listed category";
            }
            if (row.color == null || row.color.isBlank()) {
                return "stat " + entry.getKey() + " has no colour tag";
            }
        }
        if (this.prominent == null) {
            return "prominent colours are missing";
        }
        for (Map.Entry<String, String> entry : this.prominent.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                return "prominent stat " + entry.getKey() + " has no colour tag";
            }
        }
        return null;
    }

    /** One player stat row: the category it is listed under and the colours.json tag its value is drawn in. */
    public static class Row {
        @Expose public String category;
        @Expose public String color;

        public Row() {
        }

        public Row(String category, String color) {
            this.category = category;
            this.color = color;
        }
    }
}
