package xyz.iwolfking.woldsvaults.armory;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The slot keys a page mirrors. {@link #ALL} holds the fixed keys in menu order: armour (head to feet), offhand,
 * hotbar 0-8, main inventory 0-26. Curio keys ({@code curio:<id>:<index>}) follow the player's own curio slots and
 * are listed per opening by {@link #pageKeys}.
 */
public final class SlotKeys {
    public static final Set<String> DEFAULT_MARKED_CURIOS = Set.of("trinket_pouch", "charm", "deck", "belt", "necklace");
    public static final Set<String> NEVER_TOUCHED_CURIOS = Set.of("red_trinket", "blue_trinket", "green_trinket");
    public static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final String OFFHAND = "offhand";
    public static final int ARMOR_START = 0;
    public static final int OFFHAND_INDEX = 4;
    public static final int HOTBAR_START = 5;
    public static final int INV_START = 14;
    public static final String CURIO_PREFIX = "curio:";
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
        return CURIO_PREFIX + id + ":" + index;
    }

    public static boolean isCurio(String key) {
        return key.startsWith(CURIO_PREFIX);
    }

    /**
     * Every key one opening of {@code page} shows: the fixed keys, the player's curio keys in their order, then any
     * other curio key the page holds a saved or stored item for, so nothing on the page is out of reach.
     */
    public static List<String> pageKeys(List<String> playerCurios, ArmoryPage page) {
        List<String> keys = new ArrayList<>(ALL);
        keys.addAll(playerCurios);
        Set<String> extra = new TreeSet<>();
        page.expectedMap().keySet().stream().filter(SlotKeys::isCurio).forEach(extra::add);
        page.storedMap().keySet().stream().filter(SlotKeys::isCurio).forEach(extra::add);
        extra.removeAll(playerCurios);
        keys.addAll(extra);
        return keys;
    }

    /** The marks of a fresh page: armour, offhand, hotbar and the first slot of each build curio. */
    public static List<String> defaultMarks() {
        List<String> marks = new ArrayList<>();
        for (String key : ALL) {
            if (key.startsWith("armor:") || key.equals(OFFHAND) || key.startsWith("hotbar:")) {
                marks.add(key);
            }
        }
        DEFAULT_MARKED_CURIOS.stream().sorted().forEach(id -> marks.add(curio(id, 0)));
        return marks;
    }

    public static String curioId(String key) {
        String rest = key.substring(CURIO_PREFIX.length());
        return rest.substring(0, rest.lastIndexOf(':'));
    }

    public static int curioIndex(String key) {
        return Integer.parseInt(key.substring(key.lastIndexOf(':') + 1));
    }
}
