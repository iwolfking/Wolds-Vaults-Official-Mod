package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creative Armory copy rules, generated as {@code vault_configs/armory/armory.json}. {@code contentsLimitKb} caps the
 * contents copies one page may hold, in kilobytes of NBT. {@code linkedItems} lists the items whose contents live in
 * a world store under an id in their tag, and which store holds them. {@code strippedTags} lists the tags an issued
 * copy loses: owners and ids that tie an item to one player, run or vault.
 */
public class ArmoryConfig extends Config implements WoldsTable {
    @Expose public int contentsLimitKb;
    @Expose public List<LinkedItem> linkedItems;
    @Expose public List<StrippedTags> strippedTags;

    @Override
    public String getName() {
        return "armory";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.contentsLimitKb <= 0) {
            return "the contents limit is not positive";
        }
        if (this.linkedItems == null || this.strippedTags == null) {
            return "the linked item or stripped tag list is missing";
        }
        Set<String> seen = new HashSet<>();
        for (LinkedItem linked : this.linkedItems) {
            if (linked == null || blank(linked.namespace) || blank(linked.key) || linked.store == null) {
                return "a linked item entry is incomplete";
            }
            if (!seen.add(linked.namespace + "/" + linked.key)) {
                return "linked item " + linked.namespace + "/" + linked.key + " is listed twice";
            }
        }
        for (StrippedTags stripped : this.strippedTags) {
            if (stripped == null || blank(stripped.item) || stripped.keys == null || stripped.keys.isEmpty()) {
                return "a stripped tag entry is incomplete";
            }
            for (String key : stripped.keys) {
                if (blank(key) || key.startsWith(".") || key.endsWith(".") || key.contains("..")) {
                    return "stripped tag " + stripped.item + "/" + key + " is not a valid tag path";
                }
            }
        }
        return null;
    }

    private static boolean blank(@Nullable String text) {
        return text == null || text.isBlank();
    }

    /** Where a linked item's contents live, or {@code NONE} when the Creative Armory cannot copy them. */
    public enum Store {
        BACKPACK,
        STORAGE_ITEM,
        NONE
    }

    /** Items of one namespace whose tag holds a contents id under {@code key}. */
    public static class LinkedItem {
        @Expose public String namespace;
        @Expose public String key;
        @Expose public Store store;

        public LinkedItem() {
        }

        public LinkedItem(String namespace, String key, Store store) {
            this.namespace = namespace;
            this.key = key;
            this.store = store;
        }
    }

    /**
     * The tags removed from an issued copy of one item. A key is a path: dots step into nested compounds, so
     * {@code a.b} removes {@code b} from the compound {@code a}.
     */
    public static class StrippedTags {
        @Expose public String item;
        @Expose public List<String> keys;

        public StrippedTags() {
        }

        public StrippedTags(String item, List<String> keys) {
            this.item = item;
            this.keys = keys;
        }
    }
}
