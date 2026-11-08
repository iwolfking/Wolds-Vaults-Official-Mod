package xyz.iwolfking.woldsvaults.client.spellcraft;

import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The client's copy of the player's spell bar, as last synced: the default bar, the selected slot and the effective
 * bar. Read by the HUD tray, the skill menu's bar and the focus screen. Empty until the first sync.
 */
public final class ClientSpellBar {
    public static final int EMPTY = 0;
    public static final int ABILITY = 1;
    public static final int SPELL = 2;

    private static final String[] DEFAULTS = new String[SpellcraftItemData.SLOTS];
    private static final List<SpellBarSyncMessage.Entry> ENTRIES = new ArrayList<>();
    private static final List<String> ITEM_ERRORS = new ArrayList<>();
    private static int selected;
    private static String sourceName = "";
    private static boolean synced;

    static {
        Arrays.fill(DEFAULTS, "");
    }

    private ClientSpellBar() {
    }

    public static void update(SpellBarSyncMessage message) {
        System.arraycopy(message.defaults(), 0, DEFAULTS, 0, Math.min(DEFAULTS.length, message.defaults().length));
        ENTRIES.clear();
        ENTRIES.addAll(message.entries());
        ITEM_ERRORS.clear();
        ITEM_ERRORS.addAll(message.itemErrors());
        selected = message.selected();
        sourceName = message.sourceName();
        synced = true;
    }

    public static boolean isSynced() {
        return synced && ENTRIES.size() == SpellcraftItemData.SLOTS;
    }

    public static String defaultSlot(int index) {
        return DEFAULTS[index];
    }

    public static SpellBarSyncMessage.Entry entry(int index) {
        return ENTRIES.get(index);
    }

    public static int selected() {
        return selected;
    }

    /** The name of the focus or wand whose bar is active, or empty when the default bar is active. */
    public static String sourceName() {
        return sourceName;
    }

    public static List<String> itemErrors() {
        return ITEM_ERRORS;
    }

    /** The filled slot before or after {@code index} in bar order, or -1. */
    public static int neighbour(int index, int direction) {
        if (!isSynced()) {
            return -1;
        }
        for (int step = 1; step <= SpellcraftItemData.SLOTS; step++) {
            int next = Math.floorMod(index + step * direction, SpellcraftItemData.SLOTS);
            if (ENTRIES.get(next).kind() != EMPTY) {
                return next;
            }
        }
        return -1;
    }

    public static void reset() {
        Arrays.fill(DEFAULTS, "");
        ENTRIES.clear();
        ITEM_ERRORS.clear();
        selected = 0;
        sourceName = "";
        synced = false;
    }
}
