package xyz.iwolfking.woldsvaults.spellcraft.source;

import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;

/** A glyph line some equipped source offers, with the source's identity and display name. */
public record GlyphCandidate(GlyphLine line, String sourceKey, String sourceName) {
}
