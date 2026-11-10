package xyz.iwolfking.woldsvaults.spellcraft.pool;

import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;

/** A glyph line some equipped source offers, with the source's identity and display name. */
public record GlyphCandidate(SpellcraftItemData.Line line, String sourceKey, String sourceName) {
}
