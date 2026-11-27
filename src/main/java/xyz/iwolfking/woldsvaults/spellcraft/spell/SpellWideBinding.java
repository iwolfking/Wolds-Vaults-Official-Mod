package xyz.iwolfking.woldsvaults.spellcraft.spell;

import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel.Bundle;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel.Cast;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel.Step;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class SpellWideBinding {
    private SpellWideBinding() {
    }

    /**
     * Binds the spell-wide glyphs once every step is read: each joins the glyphs of every live ability step and active
     * orb it fits, and goes dormant when it fits none.
     */
    static void bind(SpellParser parser) {
        if (parser.spellWide.isEmpty()) {
            return;
        }
        List<Target> targets = new ArrayList<>();
        collectTargets(parser.config, parser.root, targets);
        for (PlacedGlyph placed : parser.spellWide) {
            GlyphType type = placed.type();
            List<Target> fitting = targets.stream().filter(target -> type.fits(parser.config, target.traits(), target.spec())).toList();
            PlacedGlyph glyph = placed;
            if (fitting.isEmpty()) {
                parser.refuse(parser.spellWideNew.contains(placed.token()), type.displayName() + " fits no ability or orb in the spell");
                if (glyph.active()) {
                    glyph = new PlacedGlyph(type, glyph.potency(), glyph.quality(), glyph.cost(), GlyphState.DORMANT, false, glyph.token());
                }
            }
            glyph = parser.force(glyph);
            parser.record(glyph);
            for (Target target : fitting) {
                target.glyphs().add(glyph);
            }
            parser.applySpellEffects(parser.config.glyph(type), glyph);
        }
    }

    private static void collectTargets(SpellcraftConfig config, List<Step> steps, List<Target> targets) {
        for (Step step : steps) {
            if (step instanceof Cast cast && cast.live()) {
                targets.add(new Target(cast.glyphs(), SpellModel.traits(config, cast.spec()), cast.spec()));
            } else if (step instanceof Bundle bundle) {
                if (bundle.glyph().active()) {
                    targets.add(new Target(bundle.glyphs(), Set.of(GlyphType.BUNDLE_TRAIT), null));
                }
                collectTargets(config, bundle.payload(), targets);
            }
        }
    }

    private record Target(List<PlacedGlyph> glyphs, Set<String> traits, @Nullable String spec) {
    }
}
