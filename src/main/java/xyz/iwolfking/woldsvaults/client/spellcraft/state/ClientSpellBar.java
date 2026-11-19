package xyz.iwolfking.woldsvaults.client.spellcraft.state;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The client's copy of the player's spell bar as last synced, empty until the first sync and cleared on logout. */
public final class ClientSpellBar {
    private static final String[] DEFAULTS = new String[SpellcraftItemData.SLOTS];
    private static final List<SpellBarSyncMessage.Entry> ENTRIES = new ArrayList<>();
    private static final List<SpellBarSyncMessage.Source> SOURCES = new ArrayList<>();
    private static int selected;
    private static BarMode mode = BarMode.CLASSIC;
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
        mode = message.mode();
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

    public static EffectiveBar.Kind kind(int index) {
        int kind = ENTRIES.get(index).kind();
        EffectiveBar.Kind[] kinds = EffectiveBar.Kind.values();
        if (kind < 0 || kind >= kinds.length) {
            WoldLogger.errorOnce("ClientSpellBar:kind:" + kind, "Spell bar sync: slot kind {} is out of range (0 to {}); the slot is drawn as empty",
                    kind, kinds.length - 1);
            return EffectiveBar.Kind.EMPTY;
        }
        return kinds[kind];
    }

    public static int selected() {
        return selected;
    }

    /** The player's bar mode as last synced; classic mode until the first sync. */
    public static BarMode mode() {
        return mode;
    }

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

    /** Whether any slot holds an item's spell, a locked clash included. */
    public static boolean hasOverlays() {
        for (SpellBarSyncMessage.Entry entry : ENTRIES) {
            if (!entry.itemKey().isEmpty()) {
                return true;
            }
        }
        return false;
    }

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

    /** The next filled slot from {@code index} in {@code direction} (1 or -1), wrapping around, or -1. */
    public static int neighbour(int index, int direction) {
        if (!isSynced()) {
            return -1;
        }
        for (int step = 1; step <= SpellcraftItemData.SLOTS; step++) {
            int next = Math.floorMod(index + step * direction, SpellcraftItemData.SLOTS);
            if (kind(next) != EffectiveBar.Kind.EMPTY) {
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
        mode = BarMode.CLASSIC;
        synced = false;
    }
}
