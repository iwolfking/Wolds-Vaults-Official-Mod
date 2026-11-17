package xyz.iwolfking.woldsvaults.armory.service;

import iskallia.vault.gear.data.AttributeGearData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.LinkedItem;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.Store;
import xyz.iwolfking.woldsvaults.config.ArmoryConfig.StrippedTags;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.integration.sophisticatedbackpacks.BackpackContentsStore;
import xyz.iwolfking.woldsvaults.integration.sophisticatedstorage.StorageItemContentsStore;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creative Armory copies. {@link #capture} copies the contents of every linked item reachable from an item when a
 * page is saved. {@link #issue} prepares a fresh copy of a page item for Load or a hand-take: every linked item gets
 * a new contents id holding its saved contents, stripped tags are removed and Vault gear gets a new gear id. Nothing
 * reaches a contents store until {@link Issue#commit}.
 */
public final class CreativeCopies {
    private static final String STORE = "store";
    private static final String CONTENTS = "contents";
    private static final String ITEM_ID = "id";
    private static final String ITEM_TAG = "tag";

    private CreativeCopies() {
    }

    /** The contents copies reachable from one item, keyed by contents id, or why the item cannot be copied. */
    public record Capture(CompoundTag contents, @Nullable String refusal) {
    }

    private record Write(Store store, UUID id, CompoundTag contents) {
    }

    /** A prepared copy: the item to hand out and the contents still to write, or why it cannot be issued. */
    public static final class Issue {
        private final ItemStack stack;
        @Nullable
        private final String refusal;
        private final List<Write> writes;
        private final Map<UUID, CompoundTag> backpacks;

        private Issue(ItemStack stack, @Nullable String refusal, List<Write> writes, Map<UUID, CompoundTag> backpacks) {
            this.stack = stack;
            this.refusal = refusal;
            this.writes = writes;
            this.backpacks = backpacks;
        }

        private static Issue refused(String refusal) {
            return new Issue(ItemStack.EMPTY, refusal, List.of(), Map.of());
        }

        public ItemStack stack() {
            return this.stack;
        }

        @Nullable
        public String refusal() {
            return this.refusal;
        }

        /** Writes every copied contents under its new id and records {@code player} as the backpacks' last opener. */
        public void commit(ServerPlayer player) {
            for (Write write : this.writes) {
                if (write.store() == Store.BACKPACK) {
                    BackpackContentsStore.write(write.id(), write.contents());
                } else if (write.store() == Store.STORAGE_ITEM) {
                    StorageItemContentsStore.write(write.id(), write.contents());
                }
            }
            this.backpacks.forEach((id, item) -> BackpackContentsStore.logAccess(player, ItemStack.of(item), id));
        }
    }

    /** Copies the contents of every linked item in {@code stack}, nested ones included. */
    public static Capture capture(ItemStack stack) {
        ArmoryConfig table = table();
        if (table == null) {
            return new Capture(new CompoundTag(), "The armory table is missing");
        }
        CompoundTag out = new CompoundTag();
        Deque<Tag> pending = new ArrayDeque<>();
        pending.add(stack.save(new CompoundTag()));
        while (!pending.isEmpty()) {
            CompoundTag item = nextItem(pending);
            if (item == null) {
                continue;
            }
            LinkedItem linked = linked(table, item);
            if (linked == null) {
                continue;
            }
            String problem = readable(item, linked);
            if (problem != null) {
                return new Capture(out, problem);
            }
            UUID id = item.getCompound(ITEM_TAG).getUUID(linked.key);
            if (out.contains(id.toString())) {
                continue;
            }
            CompoundTag contents = read(linked.store, id);
            if (contents == null) {
                return new Capture(out, name(item) + ": its storage mod is not loaded");
            }
            CompoundTag entry = new CompoundTag();
            entry.putString(STORE, linked.store.name());
            entry.put(CONTENTS, contents);
            out.put(id.toString(), entry);
            pending.add(contents);
        }
        return new Capture(out, null);
    }

    /**
     * Prepares a fresh copy of {@code template} from the contents copies saved with it. Refuses when a linked item
     * has no saved copy or cannot be copied; nothing is written either way.
     */
    public static Issue issue(ItemStack template, @Nullable CompoundTag contents) {
        ArmoryConfig table = table();
        if (table == null) {
            return Issue.refused("The armory table is missing");
        }
        CompoundTag copies = contents == null ? new CompoundTag() : contents;
        CompoundTag item = template.save(new CompoundTag());
        Map<UUID, UUID> fresh = new LinkedHashMap<>();
        Deque<Tag> pending = new ArrayDeque<>();
        pending.add(item);
        while (!pending.isEmpty()) {
            CompoundTag next = nextItem(pending);
            LinkedItem linked = next == null ? null : linked(table, next);
            if (linked == null) {
                continue;
            }
            String problem = readable(next, linked);
            if (problem != null) {
                return Issue.refused(problem);
            }
            UUID id = next.getCompound(ITEM_TAG).getUUID(linked.key);
            if (fresh.containsKey(id)) {
                continue;
            }
            CompoundTag entry = copies.getCompound(id.toString());
            Store store = storeOf(entry);
            if (store != linked.store) {
                WoldLogger.error("Creative Armory: {} has no usable contents copy (saved before contents were copied); not issued",
                        name(next));
                return Issue.refused(name(next) + " was saved without its contents; save the build again");
            }
            fresh.put(id, UUID.randomUUID());
            pending.add(entry.getCompound(CONTENTS));
        }
        Map<UUID, CompoundTag> backpacks = new HashMap<>();
        rewrite(item, table, fresh, backpacks);
        List<Write> writes = new ArrayList<>();
        fresh.forEach((old, id) -> {
            CompoundTag entry = copies.getCompound(old.toString());
            CompoundTag copy = entry.getCompound(CONTENTS).copy();
            rewrite(copy, table, fresh, backpacks);
            writes.add(new Write(storeOf(entry), id, copy));
        });
        return new Issue(ItemStack.of(item), null, writes, backpacks);
    }

    /** The size of {@code tag} as written to disk, in bytes; {@link Integer#MAX_VALUE} when it cannot be measured. */
    public static int sizeBytes(CompoundTag tag) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            NbtIo.write(tag, out);
        } catch (IOException e) {
            WoldLogger.error("Creative Armory: could not measure a contents copy; counted as over the limit", e);
            return Integer.MAX_VALUE;
        }
        return bytes.size();
    }

    /** The page's contents limit in bytes, or 0 when the armory table is missing. */
    public static int limitBytes() {
        ArmoryConfig table = table();
        return table == null ? 0 : table.contentsLimitKb * 1024;
    }

    /** The installed limit in kilobytes, for player text. */
    public static int limitKb() {
        ArmoryConfig table = table();
        return table == null ? 0 : table.contentsLimitKb;
    }

    @Nullable
    private static ArmoryConfig table() {
        if (ModConfigs.ARMORY == null) {
            WoldLogger.errorOnce("armory-table", "Creative Armory: the armory table is not installed; every copy is refused");
        }
        return ModConfigs.ARMORY;
    }

    private static void rewrite(CompoundTag root, ArmoryConfig table, Map<UUID, UUID> fresh, Map<UUID, CompoundTag> backpacks) {
        Deque<Tag> pending = new ArrayDeque<>();
        pending.add(root);
        while (!pending.isEmpty()) {
            CompoundTag item = nextItem(pending);
            if (item == null) {
                continue;
            }
            CompoundTag tag = item.getCompound(ITEM_TAG);
            String itemId = item.getString(ITEM_ID);
            for (StrippedTags stripped : table.strippedTags) {
                if (stripped.item.equals(itemId)) {
                    stripped.keys.forEach(path -> removePath(tag, path));
                }
            }
            renewGearId(item, tag);
            LinkedItem linked = linked(table, item);
            if (linked != null && tag.hasUUID(linked.key)) {
                UUID id = fresh.get(tag.getUUID(linked.key));
                if (id != null) {
                    tag.putUUID(linked.key, id);
                    if (linked.store == Store.BACKPACK) {
                        backpacks.put(id, item.copy());
                    }
                }
            }
        }
    }

    /** Removes the tag at {@code path}, whose dots step into nested compounds; a missing step leaves the tag as it is. */
    private static void removePath(CompoundTag tag, String path) {
        String[] steps = path.split("\\.");
        CompoundTag parent = tag;
        for (int i = 0; i < steps.length - 1; i++) {
            if (!parent.contains(steps[i], Tag.TAG_COMPOUND)) {
                return;
            }
            parent = parent.getCompound(steps[i]);
        }
        parent.remove(steps[steps.length - 1]);
    }

    /** Rewrites the Vault gear data in {@code tag}, which gives the copy its own gear id. */
    private static void renewGearId(CompoundTag item, CompoundTag tag) {
        if (!tag.contains(AttributeGearData.TAG_KEY)) {
            return;
        }
        ItemStack stack = ItemStack.of(item);
        if (stack.isEmpty() || !AttributeGearData.hasData(stack)) {
            return;
        }
        AttributeGearData.read(stack).write(stack);
        Tag renewed = stack.getOrCreateTag().get(AttributeGearData.TAG_KEY);
        if (renewed == null) {
            WoldLogger.error("Creative Armory: {} lost its gear data while getting a new gear id; issued with the saved id",
                    name(item));
            return;
        }
        tag.put(AttributeGearData.TAG_KEY, renewed.copy());
    }

    /**
     * Pops tags until it finds an item compound (an {@code id} string beside a {@code tag} compound), queueing the
     * children of everything it pops; null when the popped tag is not an item.
     */
    @Nullable
    private static CompoundTag nextItem(Deque<Tag> pending) {
        Tag next = pending.pop();
        if (next instanceof ListTag list) {
            pending.addAll(list);
            return null;
        }
        if (!(next instanceof CompoundTag compound)) {
            return null;
        }
        for (String key : compound.getAllKeys()) {
            pending.add(compound.get(key));
        }
        boolean item = compound.contains(ITEM_ID, Tag.TAG_STRING) && compound.contains(ITEM_TAG, Tag.TAG_COMPOUND);
        return item ? compound : null;
    }

    @Nullable
    private static LinkedItem linked(ArmoryConfig table, CompoundTag item) {
        ResourceLocation id = ResourceLocation.tryParse(item.getString(ITEM_ID));
        if (id == null) {
            return null;
        }
        CompoundTag tag = item.getCompound(ITEM_TAG);
        for (LinkedItem linked : table.linkedItems) {
            if (linked.namespace.equals(id.getNamespace()) && tag.contains(linked.key)) {
                return linked;
            }
        }
        return null;
    }

    @Nullable
    private static String readable(CompoundTag item, LinkedItem linked) {
        if (linked.store == Store.NONE) {
            return name(item) + " keeps its contents outside the item and cannot be copied";
        }
        if (!item.getCompound(ITEM_TAG).hasUUID(linked.key)) {
            WoldLogger.error("Creative Armory: {} has an unreadable contents id under {}; not copied", name(item), linked.key);
            return name(item) + " has an unreadable contents id";
        }
        if (linked.store == Store.STORAGE_ITEM && !ModList.get().isLoaded(WoldConstants.SOPHISTICATED_STORAGE_MOD)) {
            return name(item) + ": its storage mod is not loaded";
        }
        return null;
    }

    @Nullable
    private static CompoundTag read(Store store, UUID id) {
        if (store == Store.BACKPACK) {
            return BackpackContentsStore.read(id);
        }
        if (store == Store.STORAGE_ITEM && ModList.get().isLoaded(WoldConstants.SOPHISTICATED_STORAGE_MOD)) {
            return StorageItemContentsStore.read(id);
        }
        return null;
    }

    @Nullable
    private static Store storeOf(CompoundTag entry) {
        if (!entry.contains(STORE, Tag.TAG_STRING) || !entry.contains(CONTENTS, Tag.TAG_COMPOUND)) {
            return null;
        }
        try {
            return Store.valueOf(entry.getString(STORE));
        } catch (IllegalArgumentException e) {
            WoldLogger.error("Creative Armory: a contents copy names unknown store {}", entry.getString(STORE));
            return null;
        }
    }

    private static String name(CompoundTag item) {
        ItemStack stack = ItemStack.of(item);
        return stack.isEmpty() ? item.getString(ITEM_ID) : stack.getHoverName().getString();
    }
}
