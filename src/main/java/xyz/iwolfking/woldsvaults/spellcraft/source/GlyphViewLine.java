package xyz.iwolfking.woldsvaults.spellcraft.source;

import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

/**
 * The line an arcana item's spells use for one glyph type: potency, quality and cost of the strongest candidate, and
 * the largest per-spell limit any candidate allows. The sources name where each half came from; {@code ownPotency} and
 * {@code ownUses} say whether that is the item itself, and {@code transferable} whether the item's own line of this
 * type may be lent to other arcana items.
 */
public record GlyphViewLine(GlyphType type, float potency, float quality, int cost, int limit, String potencySource, String usesSource,
                       boolean ownPotency, boolean ownUses, boolean transferable) {
    public boolean borrowed() {
        return !this.ownPotency || !this.ownUses;
    }
}
