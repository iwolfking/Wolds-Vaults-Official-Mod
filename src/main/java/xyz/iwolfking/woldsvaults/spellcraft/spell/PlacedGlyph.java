package xyz.iwolfking.woldsvaults.spellcraft.spell;

import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import java.util.List;
import java.util.function.Function;

/**
 * One placed glyph with its view line's potency, quality and cost, its state, and its token index in the spell. {@code
 * dependent}: it would act, but the Bundled Spell it rides on is inactive ({@code state} is that glyph's).
 */
public record PlacedGlyph(GlyphType type, float potency, float quality, int cost, GlyphState state, boolean dependent, int token) {
    public boolean active() {
        return this.state == GlyphState.ACTIVE;
    }

    boolean potential() {
        return this.state != GlyphState.DORMANT;
    }

    static int countActive(List<PlacedGlyph> glyphs, GlyphType type) {
        return (int) glyphs.stream().filter(glyph -> glyph.type() == type && glyph.active()).count();
    }

    static int countPotential(List<PlacedGlyph> glyphs, GlyphType type) {
        return (int) glyphs.stream().filter(glyph -> glyph.type() == type && glyph.potential()).count();
    }

    /** The stacked step stats of a step's active glyphs, before Shotgun's per-copy damage and the size clamp. */
    public static GlyphStacking.Pool pool(Function<GlyphType, SpellcraftConfig.GlyphDefinition> definitions, List<PlacedGlyph> glyphs) {
        GlyphStacking.Pool pool = new GlyphStacking.Pool();
        for (PlacedGlyph glyph : glyphs) {
            if (glyph.active()) {
                SpellcraftConfig.GlyphDefinition definition = definitions.apply(glyph.type());
                pool.add(definition, definition.step, glyph.potency());
            }
        }
        return pool;
    }
}
