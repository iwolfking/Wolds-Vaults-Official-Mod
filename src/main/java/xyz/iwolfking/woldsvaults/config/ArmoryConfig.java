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
 * a world store under an id in their tag, and which store holds them. {@code ownerTags} lists the tags an issued
 * copy loses.
 */
public class ArmoryConfig extends Config implements WoldsTable {
    @Expose public int contentsLimitKb;
    @Expose public List<LinkedItem> linkedItems;
    @Expose public List<OwnerTags> ownerTags;

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
        if (this.linkedItems == null || this.ownerTags == null) {
            return "the linked item or owner tag list is missing";
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
        for (OwnerTags owner : this.ownerTags) {
            if (owner == null || blank(owner.item) || owner.keys == null || owner.keys.isEmpty()) {
                return "an owner tag entry is incomplete";
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

    /** The tag keys removed from an issued copy of one item. */
    public static class OwnerTags {
        @Expose public String item;
        @Expose public List<String> keys;

        public OwnerTags() {
        }

        public OwnerTags(String item, List<String> keys) {
            this.item = item;
            this.keys = keys;
        }
    }
}
