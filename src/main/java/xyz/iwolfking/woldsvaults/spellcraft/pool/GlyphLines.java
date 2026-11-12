package xyz.iwolfking.woldsvaults.spellcraft.pool;

import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The glyph lines an item grants, from every registered provider. Providers read only the stack, so the server (views
 * of equipped sources) and the client (the view of the item a screen edits) get the same lines. The built-in provider
 * gives an arcana item's rolled lines; gear glyph conversion adds a provider for converted affixes.
 */
public final class GlyphLines {
    private static final List<Provider> PROVIDERS = new CopyOnWriteArrayList<>(List.of(GlyphLines::arcanaLines));

    private GlyphLines() {
    }

    /**
     * One line a source grants. {@code transferable}: other arcana items may use it (always true for gear without
     * arcana). {@code auto}: it augments plain base-bar casts of the ability it is locked to; only unique glyphs may be auto
     * lines: converted affixes on gear without arcana, and etchings on any source.
     */
    public record SourceLine(SpellcraftItemData.Line line, boolean transferable, boolean auto) {
    }

    /** Adds the lines one item grants. {@code arcana} is true for an identified focus, wand or other arcana carrier. */
    @FunctionalInterface
    public interface Provider {
        void collect(ItemStack stack, boolean arcana, List<SourceLine> out);
    }

    /** Registers a provider; call during common setup. */
    public static void register(Provider provider) {
        PROVIDERS.add(provider);
    }

    public static List<SourceLine> of(ItemStack stack, boolean arcana) {
        List<SourceLine> out = new ArrayList<>();
        for (Provider provider : PROVIDERS) {
            provider.collect(stack, arcana, out);
        }
        return out;
    }

    /** An arcana item's own lines as it would see them: every line any provider grants it. */
    public static List<SpellcraftItemData.Line> own(ItemStack stack) {
        List<SpellcraftItemData.Line> lines = new ArrayList<>();
        of(stack, true).forEach(line -> lines.add(line.line()));
        return lines;
    }

    private static void arcanaLines(ItemStack stack, boolean arcana, List<SourceLine> out) {
        if (!arcana) {
            return;
        }
        for (SpellcraftItemData.Line line : SpellcraftItemData.lines(stack)) {
            out.add(new SourceLine(line, line.transferable(), false));
        }
    }
}
