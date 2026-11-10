package xyz.iwolfking.woldsvaults.client.spellcraft;

import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.network.packets.GlyphViewSyncMessage;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphCandidate;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphLines;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphView;

import java.util.ArrayList;
import java.util.List;

/**
 * The client's copy of the glyph candidates the server last synced. An arcana item's view is its own lines (read from
 * the stack, the same providers as the server) plus these shared candidates, built by the same {@link GlyphView} code,
 * so the screens and tooltips match what the server casts.
 */
public final class ClientGlyphViews {
    private static final List<GlyphCandidate> SHARED = new ArrayList<>();
    private static final List<GlyphCandidate> AUTO = new ArrayList<>();

    private ClientGlyphViews() {
    }

    public static void update(GlyphViewSyncMessage message) {
        SHARED.clear();
        SHARED.addAll(message.shared());
        AUTO.clear();
        AUTO.addAll(message.auto());
    }

    public static GlyphView view(FocusAugmentsConfig config, ItemStack stack) {
        return GlyphView.of(config, SpellcraftItemData.key(stack), stack.getHoverName().getString(), GlyphLines.own(stack), SHARED);
    }

    public static GlyphView autoLines(FocusAugmentsConfig config) {
        return GlyphView.best(config, AUTO);
    }

    public static List<GlyphCandidate> shared() {
        return SHARED;
    }

    public static void reset() {
        SHARED.clear();
        AUTO.clear();
    }
}
