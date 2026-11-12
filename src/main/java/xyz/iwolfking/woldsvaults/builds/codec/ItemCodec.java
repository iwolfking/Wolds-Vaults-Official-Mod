package xyz.iwolfking.woldsvaults.builds.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.UsesGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.TrinketEffectRegistry;
import iskallia.vault.item.CardDeckItem;
import iskallia.vault.item.gear.TrinketItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.builds.BuildException;
import xyz.iwolfking.woldsvaults.builds.BuildReport;
import xyz.iwolfking.woldsvaults.builds.Json;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.CombinedTrinketItem;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchCapability;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Converts item stacks to and from build JSON. Items with vault gear data are written as their gear fields (see
 * {@link GearCodec}) plus the rest of the stack as SNBT; other items, decks and pouches (whose trinkets live in a
 * Forge capability) are written as SNBT of the whole saved stack. Every written item is rebuilt from its JSON and
 * compared with the original; an item that does not rebuild identically is written as raw SNBT instead.
 */
public final class ItemCodec {
    private static final Set<String> ITEM_KEYS = Set.of("item", "count", "gear", "extra", "raw", "about", "pouch",
            "trinkets", "active", "trinket", "uses", "deck", "combined", "mythic_charm");

    private static final String CLIENT_CACHE = "clientCache";

    private ItemCodec() {
    }

    /** The JSON form of {@code original}, self-checked; {@code path} names the item in fallbacks. */
    public static JsonObject write(ItemStack original, String path, BuildReport report) {
        ItemStack copy = original.copy();
        CompoundTag rawSave = copy.save(new CompoundTag());
        JsonObject json;
        try {
            json = decompose(copy);
        } catch (RuntimeException e) {
            report.fallback(path + ": could not split " + describe(original) + " into named fields ("
                    + e.getClass().getSimpleName() + "); stored as raw SNBT");
            return raw(rawSave, original);
        }
        String mismatch;
        try {
            ItemStack rebuilt = read(json, path, 0, new BuildReport("self-check", path, "save"));
            mismatch = difference(copy, rebuilt);
        } catch (RuntimeException e) {
            mismatch = "it does not read back (" + e.getMessage() + ")";
        }
        if (mismatch != null) {
            report.fallback(path + ": " + describe(original) + " does not rebuild identically from named fields ("
                    + mismatch + "); stored as raw SNBT");
            return raw(rawSave, original);
        }
        return json;
    }

    private static JsonObject decompose(ItemStack copy) {
        AttributeGearData any = AttributeGearData.hasData(copy) ? AttributeGearData.read(copy) : null;
        CompoundTag save = copy.save(new CompoundTag());
        JsonObject json = new JsonObject();
        json.addProperty("item", String.valueOf(copy.getItem().getRegistryName()));
        json.addProperty("count", copy.getCount());
        CompoundTag extra = withoutCache(save);
        extra.remove("id");
        extra.remove("Count");
        if (any instanceof VaultGearData gear && !(copy.getItem() instanceof CardDeckItem)) {
            json.add("gear", GearCodec.write(gear));
            CompoundTag tag = extra.getCompound("tag");
            tag.remove(AttributeGearData.TAG_KEY);
            if (tag.isEmpty()) {
                extra.remove("tag");
            } else {
                extra.put("tag", tag);
            }
        }
        if (!extra.isEmpty()) {
            json.addProperty("extra", extra.toString());
        }
        return json;
    }

    private static JsonObject raw(CompoundTag rawSave, ItemStack original) {
        JsonObject json = new JsonObject();
        json.addProperty("raw", rawSave.toString());
        json.addProperty("about", describe(original));
        return json;
    }

    /** Why {@code rebuilt} is not the same item as {@code original}, or null when it is. */
    static String difference(ItemStack original, ItemStack rebuilt) {
        if (original.getItem() != rebuilt.getItem()) {
            return "the item changed to " + rebuilt.getItem().getRegistryName();
        }
        if (original.getCount() != rebuilt.getCount()) {
            return "the count changed";
        }
        CompoundTag a = withoutGearData(withoutCache(original.save(new CompoundTag())));
        CompoundTag b = withoutGearData(withoutCache(rebuilt.save(new CompoundTag())));
        if (!a.equals(b)) {
            return "the stack NBT outside the gear data differs";
        }
        boolean hasA = AttributeGearData.hasData(original);
        boolean hasB = AttributeGearData.hasData(rebuilt);
        if (hasA != hasB) {
            return "gear data appeared or vanished";
        }
        if (!hasA) {
            return null;
        }
        AttributeGearData dataA = AttributeGearData.read(original.copy());
        AttributeGearData dataB = AttributeGearData.read(rebuilt.copy());
        if (dataA.getClass() != dataB.getClass()) {
            return "the gear data type changed";
        }
        CompoundTag nbtA = dataA.toNbt();
        CompoundTag nbtB = dataB.toNbt();
        nbtA.remove("version");
        nbtB.remove("version");
        if (!nbtA.equals(nbtB)) {
            return "the gear data differs";
        }
        if (dataA instanceof UsesGearData usesA && dataB instanceof UsesGearData usesB
                && (usesA.getVaultUses() != usesB.getVaultUses()
                || !Objects.equals(usesA.getUsedVaults(), usesB.getUsedVaults())
                || !Objects.equals(usesA.getFreeVaultUses(), usesB.getFreeVaultUses()))) {
            return "the vault uses differ";
        }
        return null;
    }

    /** A copy of a saved stack without the gear data cache the game rebuilds on every read ({@code clientCache}). */
    private static CompoundTag withoutCache(CompoundTag save) {
        CompoundTag copy = save.copy();
        if (copy.contains("tag")) {
            CompoundTag tag = copy.getCompound("tag");
            tag.remove(CLIENT_CACHE);
            if (tag.isEmpty()) {
                copy.remove("tag");
            } else {
                copy.put("tag", tag);
            }
        }
        return copy;
    }

    private static CompoundTag withoutGearData(CompoundTag save) {
        CompoundTag copy = save.copy();
        if (copy.contains("tag")) {
            CompoundTag tag = copy.getCompound("tag");
            tag.remove(AttributeGearData.TAG_KEY);
            if (tag.isEmpty()) {
                copy.remove("tag");
            } else {
                copy.put("tag", tag);
            }
        }
        return copy;
    }

    /** Builds the stack a build JSON item describes. {@code defaultLevel} feeds authored gear without a level. */
    public static ItemStack read(JsonObject json, String path, int defaultLevel, BuildReport report) {
        Json.onlyKeys(json, path, ITEM_KEYS);
        if (json.has("raw")) {
            return readRaw(Json.string(json, "raw", path), path);
        }
        if (json.has("pouch")) {
            return readPouch(json, path, defaultLevel, report);
        }
        if (json.has("trinket")) {
            return readTrinket(json, path);
        }
        if (json.has("combined")) {
            return readCombined(json, path);
        }
        if (json.has("deck")) {
            return DeckCodec.read(Json.object(json, "deck", path), path + ".deck", report);
        }
        if (json.has("mythic_charm")) {
            return MythicCharmCodec.read(Json.object(json, "mythic_charm", path), path + ".mythic_charm", report);
        }
        ResourceLocation id = Json.id(Json.string(json, "item", path), path + ".item");
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR || !ForgeRegistries.ITEMS.containsKey(id)) {
            throw new BuildException(path + ".item: no item '" + id + "' exists in this game");
        }
        Integer count = Json.optInt(json, "count", path);
        if (count != null && (count < 1 || count > 127)) {
            throw new BuildException(path + ".count: must be 1 to 127");
        }
        CompoundTag full = new CompoundTag();
        String extra = Json.optString(json, "extra", path);
        if (extra != null) {
            full.merge(ValueCodec.parseCompound(extra, path + ".extra"));
        }
        full.putString("id", id.toString());
        full.putByte("Count", (byte) (count == null ? 1 : (int) count));
        JsonObject gear = Json.optObject(json, "gear", path);
        if (gear != null && full.getCompound("tag").contains(AttributeGearData.TAG_KEY)) {
            throw new BuildException(path + ": give the gear data either as 'gear' or inside 'extra', not both");
        }
        ItemStack stack = ItemStack.of(full);
        if (stack.isEmpty()) {
            throw new BuildException(path + ": the game could not create this item from its NBT");
        }
        if (gear != null) {
            if (!(AttributeGearData.read(stack.copy()) instanceof VaultGearData) || stack.getItem() instanceof CardDeckItem) {
                throw new BuildException(path + ".gear: " + id + " does not hold vault gear data");
            }
            GearCodec.read(stack, gear, path + ".gear", defaultLevel, report);
        }
        return stack;
    }

    private static ItemStack readRaw(String snbt, String path) {
        CompoundTag tag = ValueCodec.parseCompound(snbt, path + ".raw");
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) {
            throw new BuildException(path + ".raw: no item '" + tag.getString("id") + "' exists in this game");
        }
        ItemStack stack = ItemStack.of(tag);
        if (stack.isEmpty()) {
            throw new BuildException(path + ".raw: the game could not create this item from its NBT");
        }
        return stack;
    }

    private static ItemStack readTrinket(JsonObject json, String path) {
        ResourceLocation effectId = Json.id(Json.string(json, "trinket", path), path + ".trinket");
        TrinketEffect<?> effect = TrinketEffectRegistry.getEffect(effectId);
        if (effect == null) {
            throw new BuildException(path + ".trinket: no trinket effect '" + effectId + "' exists in this game");
        }
        Integer uses = Json.optInt(json, "uses", path);
        if (uses == null) {
            throw new BuildException(path + ".uses: missing (vault uses on the trinket)");
        }
        return TrinketItem.createTrinket(effect, uses);
    }

    private static ItemStack readCombined(JsonObject json, String path) {
        JsonArray ids = Json.optArray(json, "combined", path);
        if (ids == null || ids.size() < 2) {
            throw new BuildException(path + ".combined: list the two or more trinket effects to fuse");
        }
        List<TrinketEffect<?>> effects = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            String entryPath = path + ".combined[" + i + "]";
            ResourceLocation effectId = Json.id(Json.asString(ids.get(i), entryPath), entryPath);
            TrinketEffect<?> effect = TrinketEffectRegistry.getEffect(effectId);
            if (effect == null) {
                throw new BuildException(entryPath + ": no trinket effect '" + effectId + "' exists in this game");
            }
            effects.add(effect);
        }
        Integer uses = Json.optInt(json, "uses", path);
        if (uses == null) {
            throw new BuildException(path + ".uses: missing (vault uses on the fused trinket)");
        }
        return CombinedTrinketItem.createCombined(effects, uses);
    }

    private static ItemStack readPouch(JsonObject json, String path, int defaultLevel, BuildReport report) {
        ResourceLocation pouchId = Json.id(Json.string(json, "pouch", path), path + ".pouch");
        if (ModConfigs.TRINKET_POUCH.TRINKET_POUCH_CONFIGS == null
                || !ModConfigs.TRINKET_POUCH.TRINKET_POUCH_CONFIGS.containsKey(pouchId)) {
            throw new BuildException(path + ".pouch: no trinket pouch '" + pouchId + "' exists in this game");
        }
        ItemStack pouch = TrinketPouchItem.create(pouchId);
        PouchContents contents = PouchCapability.get(pouch);
        if (contents == null) {
            throw new BuildException(path + ".pouch: the pouch has no contents capability");
        }
        JsonArray trinkets = Json.optArray(json, "trinkets", path);
        int count = trinkets == null ? 0 : trinkets.size();
        if (count > PouchContents.SIZE) {
            throw new BuildException(path + ".trinkets: a pouch holds at most " + PouchContents.SIZE + " trinkets");
        }
        for (int i = 0; i < count; i++) {
            String entryPath = path + ".trinkets[" + i + "]";
            ItemStack trinket = read(Json.asObject(trinkets.get(i), entryPath), entryPath, defaultLevel, report);
            if (!PouchHelper.isStoredItem(trinket) || trinket.getCount() != 1) {
                throw new BuildException(entryPath + ": this pouch cannot hold " + describe(trinket));
            }
            contents.setStackInSlot(i, trinket);
        }
        List<Integer> active = new ArrayList<>();
        JsonArray activeJson = Json.optArray(json, "active", path);
        if (activeJson == null) {
            for (int i = 0; i < count; i++) {
                active.add(i);
            }
            if (count > 0) {
                report.fallback(path + ": no active trinkets listed; activating all " + count + " (the pouch then drops any "
                        + "its colour capacity cannot hold)");
            }
        } else {
            for (int i = 0; i < activeJson.size(); i++) {
                int index = Json.asInt(activeJson.get(i), path + ".active[" + i + "]");
                if (index < 0 || index >= count) {
                    throw new BuildException(path + ".active[" + i + "]: no trinket at index " + index);
                }
                active.add(index);
            }
        }
        contents.setActive(active);
        checkCapacity(pouch, contents, active, path, report);
        return pouch;
    }

    /**
     * Reports active trinkets the pouch has no slot for. A fused trinket takes the slot colour of its first trinket,
     * so a pair listed in the wrong order can overflow one colour while another has room.
     */
    private static void checkCapacity(ItemStack pouch, PouchContents contents, List<Integer> active, String path,
                                      BuildReport report) {
        Map<String, Integer> capacities = PouchHelper.capacities(pouch);
        Map<String, List<Integer>> byColor = new LinkedHashMap<>();
        for (int index : active) {
            byColor.computeIfAbsent(PouchHelper.color(contents.getStackInSlot(index)), color -> new ArrayList<>()).add(index);
        }
        byColor.forEach((color, indices) -> {
            int capacity = capacities.getOrDefault(color, 0);
            if (indices.size() > capacity) {
                report.fallback(path + ": " + indices.size() + " active trinkets take a "
                        + (color.isEmpty() ? "no-colour" : color) + " slot (indices " + indices + ") but the pouch has "
                        + capacity + "; the extra ones will not apply. A fused trinket takes the slot of its first trinket.");
            }
        });
    }

    /** A short human name for an item, for reports. */
    public static String describe(ItemStack stack) {
        return stack.getCount() + "x " + stack.getItem().getRegistryName();
    }
}
