package xyz.iwolfking.woldsvaults.items.trinket_pouch;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.slf4j.Logger;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;

public final class PouchContents extends ItemStackHandler {
    public static final int SIZE = 27;
    public static final int SCHEMA = 4;
    public static final int PRESET_COUNT = 3;
    public static final int MAX_PRESET_NAME_LENGTH = 24;
    private static final String UNREADABLE_ENTRIES_KEY = "Unreadable";
    private static final int MAX_REPORTED_FINGERPRINTS = 512;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> REPORTED_FINGERPRINTS = ConcurrentHashMap.newKeySet();
    private static final List<String> DEFAULT_PRESET_NAMES = List.of("Combat", "Looting", "Exploration");
    private final List<String> presetNames = new ArrayList<>(DEFAULT_PRESET_NAMES);
    private final Set<Integer> active = new LinkedHashSet<>();
    private final List<Set<String>> presets = List.of(new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>());
    private final List<CompoundTag> unreadableEntries = new ArrayList<>();
    private CompoundTag preservedUnreadableData;
    private boolean autoReplace;
    private int appliedPreset = -1;

    public PouchContents() {
        super(SIZE);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return isReadable() && PouchHelper.isTrinket(stack);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        requireReadable();
        validateStoredStack(stack);
        if (getStackInSlot(slot) != stack && !ItemStack.matches(getStackInSlot(slot), stack)) {
            forget(slot);
        }
        super.setStackInSlot(slot, stack);
    }

    public void synchronizeStackInSlot(int slot, ItemStack stack) {
        validateStoredStack(stack);
        super.setStackInSlot(slot, stack);
    }

    private static void validateStoredStack(ItemStack stack) {
        if (!stack.isEmpty() && (!PouchHelper.isStoredItem(stack) || stack.getCount() != 1)) {
            throw new IllegalArgumentException("Pouch storage requires one trinket per slot");
        }
    }

    public boolean isReadable() {
        return preservedUnreadableData == null;
    }

    public int unreadableEntryCount() {
        return unreadableEntries.size();
    }

    private void requireReadable() {
        if (!isReadable()) {
            throw new IllegalStateException("This trinket pouch holds data this version cannot read; it is read-only to preserve it");
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (getStackInSlot(slot).isEmpty()) {
            forget(slot);
        }
    }

    private void forget(int slot) {
        if (active.remove(slot)) appliedPreset = -1;
    }

    public List<Integer> activeIndices() {
        return List.copyOf(active);
    }

    public List<ItemStack> activeStacks() {
        return active.stream().map(this::getStackInSlot).filter(stack -> !stack.isEmpty()).toList();
    }

    public boolean isActive(int slot) {
        return active.contains(slot);
    }

    public void setActive(List<Integer> indices) {
        requireReadable();
        for (int index : indices) {
            validateSlotIndex(index);
            if (getStackInSlot(index).isEmpty()) {
                throw new IllegalArgumentException("Cannot activate an empty pouch slot: " + index);
            }
        }
        if (appliedPreset >= 0 && !effectKeys(activeIndices()).equals(effectKeys(indices))) appliedPreset = -1;
        active.clear();
        active.addAll(indices);
    }

    public void savePreset(int preset) {
        requireReadable();
        Set<String> selection = new LinkedHashSet<>();
        for (ItemStack stack : activeStacks()) selection.add(validatedEffectKey(PouchHelper.effectKey(stack)));
        presets.get(preset).clear();
        presets.get(preset).addAll(selection);
        if (appliedPreset == preset && selection.isEmpty()) appliedPreset = -1;
    }

    private Set<String> effectKeys(List<Integer> indices) {
        Set<String> keys = new LinkedHashSet<>();
        for (int index : indices) keys.add(PouchHelper.effectKey(getStackInSlot(index)));
        return keys;
    }

    public int appliedPreset() {
        return appliedPreset;
    }

    public void markPresetApplied(int preset) {
        requireReadable();
        Set<String> desired = presets.get(preset);
        if (!desired.containsAll(effectKeys(activeIndices()))) {
            throw new IllegalStateException("Applied trinkets must belong to the saved preset");
        }
        appliedPreset = desired.isEmpty() ? -1 : preset;
    }

    public List<String> preset(int preset) {
        return List.copyOf(presets.get(preset));
    }

    public List<Integer> resolvePreset(int preset) {
        return preset(preset).stream().map(this::usableIndex).filter(index -> index >= 0).toList();
    }

    public int usableIndex(String effectKey) {
        int chosen = -1;
        int fewestUses = Integer.MAX_VALUE;
        for (int index = 0; index < SIZE; index++) {
            ItemStack stack = getStackInSlot(index);
            if (!PouchHelper.isTrinket(stack) || !effectKey.equals(PouchHelper.effectKey(stack))) continue;
            int uses = PouchHelper.remainingUses(stack);
            if (uses == 0) continue;
            if (isActive(index)) return index;
            if (uses < fewestUses) {
                chosen = index;
                fewestUses = uses;
            }
        }
        return chosen;
    }

    public String presetName(int preset) {
        return presetNames.get(preset);
    }

    public void renamePreset(int preset, String name) {
        requireReadable();
        presetNames.set(preset, validatedPresetName(name));
    }

    public static String validatedPresetName(String name) {
        if (!isValidPresetName(name)) {
            throw new IllegalArgumentException("Preset names need 1-24 characters without formatting or control codes");
        }
        return name.strip();
    }

    private static boolean isValidPresetName(String name) {
        String trimmed = name.strip();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_PRESET_NAME_LENGTH
                && trimmed.codePoints().noneMatch(character -> Character.isISOControl(character) || character == 0xA7);
    }

    public boolean autoReplace() {
        return autoReplace;
    }

    public void toggleAutoReplace() {
        requireReadable();
        autoReplace = !autoReplace;
    }

    public int firstEmpty() {
        for (int index = 0; index < SIZE; index++) {
            if (getStackInSlot(index).isEmpty()) {
                return index;
            }
        }
        return -1;
    }

    public int emptySlots() {
        int empty = 0;
        for (int index = 0; index < SIZE; index++) {
            if (getStackInSlot(index).isEmpty()) {
                empty++;
            }
        }
        return empty;
    }

    @Override
    public CompoundTag serializeNBT() {
        if (!isReadable()) {
            return preservedUnreadableData.copy();
        }
        CompoundTag result = super.serializeNBT();
        result.putInt("Schema", SCHEMA);
        result.putIntArray("Active", active.stream().mapToInt(Integer::intValue).toArray());
        for (int index = 0; index < presets.size(); index++) {
            ListTag entries = new ListTag();
            presets.get(index).forEach(key -> entries.add(StringTag.valueOf(key)));
            result.put("Preset" + index, entries);
            result.putString("PresetName" + index, presetNames.get(index));
        }
        result.putBoolean("AutoReplace", autoReplace);
        result.putInt("AppliedPreset", appliedPreset);
        if (!unreadableEntries.isEmpty()) {
            ListTag preserved = new ListTag();
            unreadableEntries.forEach(entry -> preserved.add(entry.copy()));
            result.put(UNREADABLE_ENTRIES_KEY, preserved);
        }
        return result;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.isEmpty()) {
            return;
        }
        resetToEmpty();
        try {
            String unsupportedFormat = unsupportedFormatReason(tag);
            if (unsupportedFormat != null) {
                preserveUnreadable(tag, unsupportedFormat, null);
                return;
            }
            decodeSupportedFormat(tag);
        } catch (RuntimeException exception) {
            resetToEmpty();
            preserveUnreadable(tag, "decoding failed", exception);
        }
    }

    private void resetToEmpty() {
        for (int slot = 0; slot < SIZE; slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }
        active.clear();
        for (int index = 0; index < PRESET_COUNT; index++) {
            presets.get(index).clear();
            presetNames.set(index, DEFAULT_PRESET_NAMES.get(index));
        }
        unreadableEntries.clear();
        preservedUnreadableData = null;
        autoReplace = false;
        appliedPreset = -1;
    }

    private static String unsupportedFormatReason(CompoundTag tag) {
        if (!tag.contains("Schema", Tag.TAG_ANY_NUMERIC) || tag.getInt("Schema") < 1) {
            return "missing or invalid schema";
        }
        if (tag.getInt("Schema") > SCHEMA) {
            return "saved with newer schema " + tag.getInt("Schema") + " (this version reads up to " + SCHEMA + ")";
        }
        if (tag.getInt("Size") != SIZE) {
            return "unsupported capacity " + tag.getInt("Size");
        }
        if (isMalformedCompoundList(tag.get("Items")) || tag.contains(UNREADABLE_ENTRIES_KEY) && isMalformedCompoundList(tag.get(UNREADABLE_ENTRIES_KEY))) {
            return "malformed item list";
        }
        return null;
    }

    private static boolean isMalformedCompoundList(Tag tag) {
        return !(tag instanceof ListTag list) || !list.isEmpty() && list.getElementType() != Tag.TAG_COMPOUND;
    }

    private void preserveUnreadable(CompoundTag tag, String reason, RuntimeException cause) {
        preservedUnreadableData = tag.copy();
        String message = "Trinket pouch data could not be read (" + reason + "). It is preserved unchanged and the pouch is read-only until a compatible Wolds version loads it.";
        if (firstReport("unreadable:" + reason, tag)) {
            if (cause == null) {
                LOGGER.error(message);
            } else {
                LOGGER.error(message, cause);
            }
        }
    }

    private void decodeSupportedFormat(CompoundTag tag) {
        int schema = tag.getInt("Schema");
        List<String> repairs = new ArrayList<>();
        List<DecodedEntry> homeless = new ArrayList<>();
        Set<Integer> placedAtSavedSlot = new LinkedHashSet<>();
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int index = 0; index < items.size(); index++) {
            CompoundTag entry = items.getCompound(index);
            ItemStack stack = decodeStoredStack(entry);
            int slot = entry.getInt("Slot");
            if (stack.isEmpty()) {
                unreadableEntries.add(entry.copy());
            } else if (slot >= 0 && slot < SIZE && stacks.get(slot).isEmpty()) {
                stacks.set(slot, stack);
                placedAtSavedSlot.add(slot);
            } else {
                repairs.add("relocated entry " + index + " with invalid or duplicate slot " + slot);
                homeless.add(new DecodedEntry(entry, stack));
            }
        }
        ListTag previouslyUnreadable = tag.getList(UNREADABLE_ENTRIES_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < previouslyUnreadable.size(); index++) {
            CompoundTag entry = previouslyUnreadable.getCompound(index);
            ItemStack stack = decodeStoredStack(entry);
            if (stack.isEmpty()) {
                unreadableEntries.add(entry.copy());
            } else {
                repairs.add("restored previously unreadable " + entry.getString("id"));
                homeless.add(new DecodedEntry(entry, stack));
            }
        }
        homeless.forEach(this::placeHomeless);

        Map<Integer, String> keysBySlot = new LinkedHashMap<>();
        for (int slot : placedAtSavedSlot) {
            keysBySlot.put(slot, PouchHelper.effectKey(stacks.get(slot)));
        }
        List<Integer> decodedActive = decodeIndices(tag.getIntArray("Active"), keysBySlot.keySet(), "active selection", repairs);
        for (int index = 0; index < PRESET_COUNT; index++) {
            presets.get(index).addAll(schema < 3
                    ? migrateSlotPreset(tag.getIntArray("Preset" + index), keysBySlot, index, repairs)
                    : decodePreset(tag, "Preset" + index, repairs));
            if (schema > 1) {
                String name = tag.getString("PresetName" + index);
                if (isValidPresetName(name)) {
                    presetNames.set(index, name.strip());
                } else {
                    repairs.add("reset invalid name of preset " + index);
                }
            }
        }
        active.addAll(decodedActive);
        appliedPreset = schema >= 4 ? decodeAppliedPreset(tag, decodedActive, keysBySlot, repairs) : -1;
        autoReplace = tag.getBoolean("AutoReplace");
        reportRepairs(tag, repairs);
    }

    private static ItemStack decodeStoredStack(CompoundTag entry) {
        ItemStack stack = ItemStack.of(entry);
        return !stack.isEmpty() && stack.getCount() == 1 && PouchHelper.isStoredItem(stack) ? stack : ItemStack.EMPTY;
    }

    private void placeHomeless(DecodedEntry entry) {
        int savedSlot = entry.raw().getInt("Slot");
        int slot = savedSlot >= 0 && savedSlot < SIZE && stacks.get(savedSlot).isEmpty() ? savedSlot : firstEmpty();
        if (slot < 0) {
            unreadableEntries.add(entry.raw().copy());
        } else {
            stacks.set(slot, entry.stack());
        }
    }

    private record DecodedEntry(CompoundTag raw, ItemStack stack) {}

    private static List<Integer> decodeIndices(int[] values, Set<Integer> validSlots, String description, List<String> repairs) {
        Set<Integer> result = new LinkedHashSet<>();
        for (int value : values) {
            if (!validSlots.contains(value) || !result.add(value)) {
                repairs.add("dropped invalid " + description + " index " + value);
            }
        }
        return List.copyOf(result);
    }

    private static Set<String> migrateSlotPreset(int[] slots, Map<Integer, String> keysBySlot, int preset, List<String> repairs) {
        Set<String> migrated = new LinkedHashSet<>();
        for (int slot : decodeIndices(slots, keysBySlot.keySet(), "preset " + preset, repairs)) {
            String key = keysBySlot.get(slot);
            if (isValidEffectKey(key)) {
                migrated.add(key);
            } else {
                repairs.add("dropped unresolvable trinket from preset " + preset);
            }
        }
        return migrated;
    }

    private static Set<String> decodePreset(CompoundTag tag, String name, List<String> repairs) {
        Set<String> result = new LinkedHashSet<>();
        if (!(tag.get(name) instanceof ListTag entries) || !entries.isEmpty() && entries.getElementType() != Tag.TAG_STRING) {
            repairs.add("cleared malformed " + name);
            return result;
        }
        for (int index = 0; index < entries.size(); index++) {
            String key = entries.getString(index);
            if (!isValidEffectKey(key) || result.size() >= SIZE || !result.add(key)) {
                repairs.add("dropped invalid or duplicate " + name + " entry " + key);
            }
        }
        return result;
    }

    private int decodeAppliedPreset(CompoundTag tag, List<Integer> decodedActive, Map<Integer, String> keysBySlot, List<String> repairs) {
        int applied = tag.contains("AppliedPreset", Tag.TAG_ANY_NUMERIC) ? tag.getInt("AppliedPreset") : -1;
        if (applied < -1 || applied >= PRESET_COUNT) {
            repairs.add("cleared out-of-range applied preset " + applied);
            return -1;
        }
        if (applied >= 0 && (presets.get(applied).isEmpty()
                || decodedActive.stream().anyMatch(slot -> !presets.get(applied).contains(keysBySlot.get(slot))))) {
            repairs.add("cleared applied preset " + applied + " that no longer matches the active trinkets");
            return -1;
        }
        return applied;
    }

    private void reportRepairs(CompoundTag tag, List<String> repairs) {
        if (!unreadableEntries.isEmpty() && firstReport("entries", tag)) {
            List<String> ids = unreadableEntries.stream().map(entry -> entry.getString("id")).toList();
            LOGGER.error("Trinket pouch kept {} unreadable item entries {} unchanged; they are retried whenever the pouch loads.",
                    unreadableEntries.size(), ids);
        }
        if (!repairs.isEmpty() && firstReport("repairs", tag)) {
            LOGGER.warn("Trinket pouch selection state was repaired while loading: {}", repairs);
        }
    }

    private static boolean firstReport(String category, CompoundTag tag) {
        if (REPORTED_FINGERPRINTS.size() >= MAX_REPORTED_FINGERPRINTS) {
            REPORTED_FINGERPRINTS.clear();
        }
        return REPORTED_FINGERPRINTS.add(category + ":" + tag.hashCode());
    }

    private static String validatedEffectKey(String key) {
        if (!isValidEffectKey(key)) {
            throw new IllegalStateException("Invalid pouch preset effect key: " + key);
        }
        return key;
    }

    private static boolean isValidEffectKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        Set<String> effects = new LinkedHashSet<>();
        for (String effect : key.split("\\+", -1)) {
            if (!effect.contains(":") || ResourceLocation.tryParse(effect) == null || !effects.add(effect)) {
                return false;
            }
        }
        return true;
    }
}
