package xyz.iwolfking.woldsvaults.spellcraft.spell;

import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import java.util.List;

final class SpellArcana {
    private SpellArcana() {
    }

    record Arcana(float total, float escalation) {
    }

    /**
     * Live arcana over active glyphs. Common glyphs pay their view line's cost and escalate, and unique glyphs are free
     * while every active glyph in the spell is unique, otherwise costing the table's {@code uniqueMixCost} each without
     * escalating. Combos count active glyphs.
     */
    static Arcana arcana(SpellcraftConfig config, SpellParser parser) {
        List<PlacedGlyph> active = parser.byToken.values().stream().filter(PlacedGlyph::active).toList();
        boolean allUnique = active.stream().allMatch(glyph -> config.glyph(glyph.type()).isUnique());
        float common = 0.0F;
        float unique = 0.0F;
        int escalating = 0;
        for (PlacedGlyph glyph : active) {
            if (config.glyph(glyph.type()).isUnique()) {
                unique += allUnique ? 0.0F : config.rules.uniqueMixCost;
            } else {
                common += glyph.cost();
                if (!config.glyph(glyph.type()).noEscalation) {
                    escalating++;
                }
            }
        }
        int steps = Math.max(0, parser.castGroups.size() - 1);
        int n = escalating + steps;
        float escalation = 1.0F + config.rules.escalationPerGlyph * Math.max(0, n - 1);
        float total = (common + config.rules.abilityStepCost * steps) * escalation + unique;
        for (SpellcraftConfig.Combo combo : config.combos) {
            GlyphType a = GlyphType.byId(combo.a);
            GlyphType b = GlyphType.byId(combo.b);
            boolean hit;
            if (isPair(a, b, GlyphType.SHOTGUN, GlyphType.BUNDLED_SPELL)) {
                hit = parser.shotgunOnBundle;
            } else {
                hit = parser.activeUses.containsKey(a) && parser.activeUses.containsKey(b);
            }
            if (hit) {
                total += combo.cost;
            }
        }
        return new Arcana(total, escalation);
    }

    private static boolean isPair(GlyphType a, GlyphType b, GlyphType x, GlyphType y) {
        return (a == x && b == y) || (a == y && b == x);
    }
}
