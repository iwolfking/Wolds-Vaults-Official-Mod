package xyz.iwolfking.woldsvaults.spellcraft.item;

import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import javax.annotation.Nullable;

/**
 * One rolled glyph line: its type, rolled potency, arcana cost per placement and how many placements it allows. {@code
 * transferable} lines may be used by other arcana items; {@code cap}, when set, replaces the type's per-spell cap for
 * this line's own item only.
 */
public record GlyphLine(GlyphType type, float potency, float quality, int cost, int uses, boolean transferable, @Nullable Integer cap) {
    public GlyphLine(GlyphType type, float potency, float quality, int cost, int uses) {
        this(type, potency, quality, cost, uses, false, null);
    }
}
