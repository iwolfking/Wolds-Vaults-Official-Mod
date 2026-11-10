package xyz.iwolfking.woldsvaults.client.spellcraft;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The client's copy of the player's spell bar, as last synced: the default bar, the selected slot, the effective bar
 * the server resolved and the equipped arcana items it came from. Read by the HUD tray, the skill menu's bar and the
 * focus screen. Empty until the first sync.
 */
public final class ClientSpellBar {
    public static final int EMPTY = 0;
    public static final int ABILITY = 1;
    public static final int SPELL = 2;

    private static final String[] DEFAULTS = new String[SpellcraftItemData.SLOTS];
    private static final List<SpellBarSyncMessage.Entry> ENTRIES = new ArrayList<>();
    private static final List<SpellBarSyncMessage.Source> SOURCES = new ArrayList<>();
    private static int selected;
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
        SOURCES.clear();
        SOURCES.addAll(message.sources());
        selected = message.selected();
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

    /** The equipped arcana items the bar was resolved from, with their arcana, budget-greyed glyphs and errors. */
    public static List<SpellBarSyncMessage.Source> sources() {
        return SOURCES;
    }

    @Nullable
    public static SpellBarSyncMessage.Source source(String key) {
        for (SpellBarSyncMessage.Source source : SOURCES) {
            if (source.key().equals(key)) {
                return source;
            }
        }
        return null;
    }

    /** Whether any slot holds an item's spell (or a LOCKED clash of them). */
    public static boolean hasOverlays() {
        for (SpellBarSyncMessage.Entry entry : ENTRIES) {
            if (!entry.itemKey().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** The stack in the player's hands whose spellcraft key is {@code key}, or empty. */
    public static ItemStack stackOf(String key) {
        if (Minecraft.getInstance().player == null || key.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = Minecraft.getInstance().player.getItemInHand(hand);
            if (SpellcraftItemData.has(stack) && SpellcraftItemData.key(stack).equals(key)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
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
        SOURCES.clear();
        selected = 0;
        synced = false;
    }
}
