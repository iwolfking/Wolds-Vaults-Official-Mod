package xyz.iwolfking.woldsvaults.items.trinket_pouch;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;

public final class PouchMigration {
    private static final ThreadLocal<Boolean> RESTORING = ThreadLocal.withInitial(() -> false);
    private static final UUID LEGACY_POUCH_MODIFIER = UUID.nameUUIDFromBytes("trinket_pouch0".getBytes(StandardCharsets.UTF_8));
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_REPORTED_RETENTIONS = 512;
    private static final Set<String> REPORTED_RETENTIONS = ConcurrentHashMap.newKeySet();

    private PouchMigration() {}

    public static boolean restoring() {
        return RESTORING.get();
    }

    public static void setRestoring(boolean value) {
        if (value) {
            RESTORING.set(true);
        } else {
            RESTORING.remove();
        }
    }

    public static Tag serializedCurios(Tag original) {
        if (!(original instanceof CompoundTag originalCompound)) {
            return original;
        }
        CompoundTag converted = originalCompound.copy();
        ListTag curios = converted.getList("Curios", Tag.TAG_COMPOUND);
        CompoundTag pouchEntry = equippedPouchEntry(curios);
        if (pouchEntry == null) {
            return original;
        }
        ItemStack pouch = ItemStack.of(pouchEntry);
        if (!PouchHelper.isPouch(pouch) || !PouchCapability.get(pouch).isReadable()) {
            return original;
        }
        boolean changed = stored(pouch);
        PouchContents contents = PouchCapability.get(pouch);
        List<Integer> moved = new ArrayList<>();
        List<String> retainedItems = new ArrayList<>();
        for (int index = 0; index < curios.size(); index++) {
            CompoundTag curio = curios.getCompound(index);
            if (!PouchHelper.COLORS.contains(curio.getString("Identifier"))) {
                continue;
            }
            CompoundTag handler = curio.getCompound("StacksHandler");
            ListTag items = handler.getCompound("Stacks").getList("Items", Tag.TAG_COMPOUND);
            ListTag retained = new ListTag();
            for (int item = 0; item < items.size(); item++) {
                CompoundTag entry = items.getCompound(item);
                ItemStack stack = ItemStack.of(entry);
                if (canMove(stack, contents)) {
                    moved.add(insert(contents, stack));
                } else {
                    retained.add(entry);
                    retainedItems.add(describe(entry, stack));
                }
            }
            if (retained.size() != items.size()) {
                handler.getCompound("Stacks").put("Items", retained);
                changed = true;
            }
            if (retained.isEmpty() && removeLegacyModifiers(handler)) {
                changed = true;
            }
        }
        activate(pouch, contents, moved);
        reportRetainedOnce("saved Curios data", retainedItems);
        if (!changed) {
            return original;
        }
        CompoundTag serialized = pouch.save(new CompoundTag());
        for (String key : serialized.getAllKeys()) {
            pouchEntry.put(key, serialized.get(key));
        }
        return converted;
    }

    public static boolean stored(ItemStack pouch) {
        CompoundTag tag = pouch.getTag();
        if (tag == null || !tag.contains("StoredCurios", Tag.TAG_LIST)) {
            return false;
        }
        PouchContents contents = PouchCapability.get(pouch);
        if (!contents.isReadable()) {
            return false;
        }
        ListTag legacy = tag.getList("StoredCurios", Tag.TAG_COMPOUND);
        ListTag retained = new ListTag();
        List<Integer> moved = new ArrayList<>();
        List<String> retainedItems = new ArrayList<>();
        for (int index = 0; index < legacy.size(); index++) {
            CompoundTag entry = legacy.getCompound(index);
            ItemStack stack = ItemStack.of(entry);
            if (canMove(stack, contents)) {
                moved.add(insert(contents, stack));
            } else {
                retained.add(entry.copy());
                retainedItems.add(describe(entry, stack));
            }
        }
        reportRetainedOnce("a stored pouch", retainedItems);
        if (moved.isEmpty()) {
            return false;
        }
        activate(pouch, contents, moved);
        if (retained.isEmpty()) {
            tag.remove("StoredCurios");
        } else {
            tag.put("StoredCurios", retained);
        }
        return true;
    }

    public static void equipped(Player player) {
        if (player.level.isClientSide) {
            return;
        }
        ItemStack pouch = PouchHelper.equipped(player);
        if (!PouchHelper.isPouch(pouch) || !PouchCapability.get(pouch).isReadable()) {
            return;
        }
        stored(pouch);
        PouchContents contents = PouchCapability.get(pouch);
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            List<Integer> moved = new ArrayList<>();
            List<String> retainedItems = new ArrayList<>();
            Set<String> colorsWithRetainedItems = new HashSet<>();
            for (String color : PouchHelper.COLORS) {
                handler.getStacksHandler(color).ifPresent(slots -> {
                    for (int index = 0; index < slots.getStacks().getSlots(); index++) {
                        ItemStack stack = slots.getStacks().getStackInSlot(index);
                        if (stack.isEmpty()) {
                            continue;
                        }
                        if (canMove(stack, contents)) {
                            moved.add(insert(contents, stack));
                            slots.getStacks().setStackInSlot(index, ItemStack.EMPTY);
                        } else {
                            retainedItems.add(stack.toString());
                            colorsWithRetainedItems.add(color);
                        }
                    }
                });
            }
            activate(pouch, contents, moved);
            Multimap<String, AttributeModifier> obsolete = HashMultimap.create();
            for (String color : PouchHelper.COLORS) {
                if (colorsWithRetainedItems.contains(color)) {
                    continue;
                }
                for (AttributeModifier modifier : handler.getModifiers().get(color)) {
                    if (modifier.getId().equals(LEGACY_POUCH_MODIFIER)) {
                        obsolete.put(color, modifier);
                    }
                }
            }
            if (!obsolete.isEmpty()) {
                handler.removeSlotModifiers(obsolete);
            }
            if (reportRetainedOnce(player.getGameProfile().getName(), retainedItems)) {
                player.displayClientMessage(new TranslatableComponent("gui.woldsvaults.pouch.legacy_retained", retainedItems.size()), false);
            }
        });
    }

    private static CompoundTag equippedPouchEntry(ListTag curios) {
        for (int index = 0; index < curios.size(); index++) {
            CompoundTag curio = curios.getCompound(index);
            if (curio.getString("Identifier").equals("trinket_pouch")) {
                ListTag items = curio.getCompound("StacksHandler").getCompound("Stacks").getList("Items", Tag.TAG_COMPOUND);
                for (int item = 0; item < items.size(); item++) {
                    if (items.getCompound(item).getInt("Slot") == 0) {
                        return items.getCompound(item);
                    }
                }
            }
        }
        return null;
    }

    private static boolean canMove(ItemStack stack, PouchContents contents) {
        return PouchHelper.isStoredItem(stack) && stack.getCount() == 1 && contents.firstEmpty() >= 0;
    }

    private static int insert(PouchContents contents, ItemStack stack) {
        int slot = contents.firstEmpty();
        contents.setStackInSlot(slot, stack);
        return slot;
    }

    private static void activate(ItemStack pouch, PouchContents contents, List<Integer> moved) {
        if (moved.isEmpty()) {
            return;
        }
        List<Integer> active = new ArrayList<>(contents.activeIndices());
        active.addAll(moved);
        contents.setActive(PouchHelper.validSelection(pouch, contents, active));
    }

    private static boolean removeLegacyModifiers(CompoundTag handler) {
        boolean removed = false;
        for (String key : List.of("CachedModifiers", "PersistentModifiers")) {
            ListTag modifiers = handler.getList(key, Tag.TAG_COMPOUND);
            for (int index = modifiers.size() - 1; index >= 0; index--) {
                CompoundTag modifier = modifiers.getCompound(index);
                if (modifier.hasUUID("UUID") && modifier.getUUID("UUID").equals(LEGACY_POUCH_MODIFIER)) {
                    modifiers.remove(index);
                    removed = true;
                }
            }
        }
        return removed;
    }

    private static String describe(CompoundTag entry, ItemStack stack) {
        return stack.isEmpty() ? "undecodable " + entry.getString("id") : stack.toString();
    }

    private static boolean reportRetainedOnce(String owner, List<String> retainedItems) {
        if (retainedItems.isEmpty()) {
            return false;
        }
        if (REPORTED_RETENTIONS.size() >= MAX_REPORTED_RETENTIONS) {
            REPORTED_RETENTIONS.clear();
        }
        if (!REPORTED_RETENTIONS.add(owner + ":" + retainedItems)) {
            return false;
        }
        LOGGER.warn("Kept {} legacy trinket slot item(s) of {} in place (not a trinket, undecodable, or no free pouch entry): {}",
                retainedItems.size(), owner, retainedItems);
        return true;
    }
}
