package xyz.iwolfking.woldsvaults.client.spellcraft.state;

import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.network.packets.GlyphViewSyncMessage;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphCandidate;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphLineProviders;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;

import java.util.ArrayList;
import java.util.List;

/**
 * The shared glyph candidates the server last synced, cleared on logout. An item's view is built from its own lines
 * plus these candidates with the same {@link GlyphView} code the server uses, so screens and tooltips match what the
 * server casts.
 */
public final class ClientGlyphViews {
    private static final List<GlyphCandidate> SHARED = new ArrayList<>();

    private ClientGlyphViews() {
    }

    public static void update(GlyphViewSyncMessage message) {
        SHARED.clear();
        SHARED.addAll(message.shared());
    }

    public static GlyphView view(SpellcraftConfig config, ItemStack stack) {
        return GlyphView.of(config, SpellcraftItemData.key(stack), stack.getHoverName().getString(), GlyphLineProviders.own(stack), SHARED);
    }

    public static void reset() {
        SHARED.clear();
    }
}
