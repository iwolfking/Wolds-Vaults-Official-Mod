package xyz.iwolfking.woldsvaults.armory;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The fixed slot keys a page mirrors, in menu order: armour (head to feet), offhand, hotbar 0-8, main inventory
 * 0-26, then one curio key per known curio slot id. A key's index in {@link #ALL} is its page slot index and its
 * mark-toggle button offset.
 */
public final class SlotKeys {
    public static final List<String> CURIO_IDS = List.of("trinket_pouch", "charm", "deck", "belt", "necklace", "ring",
            "back", "head", "wallet", "shard_pouch", "compass", "angelring", "wireless_terminal");
    public static final Set<String> DEFAULT_MARKED_CURIOS = Set.of("trinket_pouch", "charm", "deck", "belt", "necklace");
    public static final Set<String> NEVER_TOUCHED_CURIOS = Set.of("red_trinket", "blue_trinket", "green_trinket");
    public static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final String OFFHAND = "offhand";
    public static final int ARMOR_START = 0;
    public static final int OFFHAND_INDEX = 4;
    public static final int HOTBAR_START = 5;
    public static final int INV_START = 14;
    public static final int CURIO_START = 41;
    public static final List<String> ALL;

    static {
        List<String> keys = new ArrayList<>();
        for (EquipmentSlot slot : ARMOR) {
            keys.add(armor(slot));
        }
        keys.add(OFFHAND);
        for (int i = 0; i < 9; i++) {
            keys.add("hotbar:" + i);
        }
        for (int i = 0; i < 27; i++) {
            keys.add("inv:" + i);
        }
        for (String id : CURIO_IDS) {
            keys.add(curio(id, 0));
        }
        ALL = Collections.unmodifiableList(keys);
    }

    private SlotKeys() {
    }

    public static int size() {
        return ALL.size();
    }

    public static String armor(EquipmentSlot slot) {
        return "armor:" + slot.getName();
    }

    public static String curio(String id, int index) {
        return "curio:" + id + ":" + index;
    }

    public static int indexOf(String key) {
        return ALL.indexOf(key);
    }

    /** Whether a key is marked on a fresh page: armour, offhand, hotbar and the five build curios. */
    public static boolean markedByDefault(String key) {
        if (key.startsWith("armor:") || key.equals(OFFHAND) || key.startsWith("hotbar:")) {
            return true;
        }
        if (key.startsWith("curio:")) {
            return DEFAULT_MARKED_CURIOS.contains(curioId(key));
        }
        return false;
    }

    public static String curioId(String key) {
        String rest = key.substring("curio:".length());
        return rest.substring(0, rest.lastIndexOf(':'));
    }

    public static int curioIndex(String key) {
        return Integer.parseInt(key.substring(key.lastIndexOf(':') + 1));
    }
}
