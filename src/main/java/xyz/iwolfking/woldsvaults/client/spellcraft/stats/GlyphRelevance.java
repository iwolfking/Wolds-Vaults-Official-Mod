package xyz.iwolfking.woldsvaults.client.spellcraft.stats;

import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.ability.SpellAbilities;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStats;
import xyz.iwolfking.woldsvaults.spellcraft.hit.HitSources;
import xyz.iwolfking.woldsvaults.spellcraft.spell.PlacedGlyph;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Which glyph stats do anything on a given step, so tooltips list only the side effects that apply. A step stat applies
 * by the traits of the step's spec, mana and efficiency apply when any step costs mana, and cooldown and haste always
 * apply.
 */
public final class GlyphRelevance {

    /** Whether a stat does anything here, where {@code pooled} marks a common upside that adds into the step's pool. */
    @FunctionalInterface
    public interface Filter {
        boolean applies(String stat, boolean pooled);
    }

    public static final Filter ALL = (stat, pooled) -> true;

    private GlyphRelevance() {
    }

    /**
     * The filter for every step that holds the glyph at {@code token}, where a stat applies if it applies on any of
     * them, or for the spell alone when no step holds it.
     */
    public static Filter forToken(SpellcraftConfig config, SpellModel model, int token) {
        List<SpellModel.Step> owners = new ArrayList<>();
        ownersOf(model.steps(), token, owners);
        if (owners.size() <= 1) {
            return of(config, model, owners.isEmpty() ? null : owners.get(0));
        }
        List<Filter> filters = owners.stream().map(step -> of(config, model, step)).toList();
        return (stat, pooled) -> filters.stream().anyMatch(filter -> filter.applies(stat, pooled));
    }

    /** A null step stands for a glyph outside any step, such as a Delay, and keeps every step stat. */
    public static Filter of(SpellcraftConfig config, SpellModel model, @Nullable SpellModel.Step step) {
        boolean costsMana = costsMana(config, model.steps());
        return (stat, pooled) -> {
            switch (stat) {
                case GlyphStats.MANA, GlyphStats.EFFICIENCY -> {
                    return costsMana;
                }
                case GlyphStats.COOLDOWN, GlyphStats.HASTE -> {
                    return true;
                }
                default -> {
                }
            }
            if (!GlyphStacking.STEP_STATS.contains(stat)) {
                WoldLogger.errorOnce("GlyphRelevance:stat:" + stat, "Glyph relevance: unknown stat '{}'; it is listed as applying", stat);
                return true;
            }
            if (step instanceof SpellModel.Cast cast) {
                return castApplies(config, cast.spec(), stat, pooled);
            }
            if (step instanceof SpellModel.Bundle bundle) {
                return switch (stat) {
                    case GlyphStats.SPEED, GlyphStats.SCALE -> true;
                    case GlyphStats.DAMAGE -> damages(config, bundle.payload());
                    default -> false;
                };
            }
            return true;
        };
    }

    private static boolean castApplies(SpellcraftConfig config, @Nullable String spec, String stat, boolean pooled) {
        if (spec == null) {
            WoldLogger.errorOnce("GlyphRelevance:spec:null", "Glyph relevance: an ability step has no specialization; every glyph stat is listed for it");
            return true;
        }
        Set<String> traits = SpellModel.traits(config, spec);
        boolean toggle = traits.contains("toggle");
        boolean heal = SpellAbilities.isHeal(spec);
        boolean intensify = traits.contains("intensify");
        boolean extend = traits.contains("extend");
        return switch (stat) {
            case GlyphStats.DAMAGE -> hasHits(spec);
            case GlyphStats.AREA -> traits.contains("area");
            case GlyphStats.SPEED -> traits.contains("projectile") || traits.contains("path");
            case GlyphStats.SCALE -> traits.contains("projectile") || traits.contains("path") || traits.contains("bolt");
            case GlyphStats.INTENSITY -> intensify;
            case GlyphStats.DURATION -> extend && !heal && (!toggle || pooled);
            case GlyphStats.DRAIN -> (intensify || extend) && toggle;
            case GlyphStats.HEAL_MANA -> heal;
            default -> true;
        };
    }

    private static void ownersOf(List<SpellModel.Step> steps, int token, List<SpellModel.Step> owners) {
        for (SpellModel.Step step : steps) {
            if (step instanceof SpellModel.Cast cast && holds(cast.glyphs(), token)) {
                owners.add(cast);
            }
            if (step instanceof SpellModel.Bundle bundle) {
                if (bundle.glyph().token() == token || holds(bundle.glyphs(), token)) {
                    owners.add(bundle);
                }
                ownersOf(bundle.payload(), token, owners);
            }
        }
    }

    private static boolean holds(List<PlacedGlyph> glyphs, int token) {
        for (PlacedGlyph glyph : glyphs) {
            if (glyph.token() == token) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHits(String spec) {
        List<HitSources.Source> sources = HitSources.sources(spec);
        if (sources == null) {
            WoldLogger.errorOnce("GlyphRelevance:hits:" + spec, "Glyph relevance: spell hits has no entry for {}; damage is listed as applying", spec);
            return true;
        }
        return !sources.isEmpty();
    }

    private static boolean damages(SpellcraftConfig config, List<SpellModel.Step> steps) {
        for (SpellModel.Step step : steps) {
            if (step instanceof SpellModel.Cast cast && castApplies(config, cast.spec(), GlyphStats.DAMAGE, false)) {
                return true;
            }
            if (step instanceof SpellModel.Bundle bundle && damages(config, bundle.payload())) {
                return true;
            }
        }
        return false;
    }

    private static boolean costsMana(SpellcraftConfig config, List<SpellModel.Step> steps) {
        for (SpellModel.Step step : steps) {
            if (step instanceof SpellModel.Cast cast && cast.spec() != null && SpellModel.traits(config, cast.spec()).contains("mana")) {
                return true;
            }
            if (step instanceof SpellModel.Bundle bundle && costsMana(config, bundle.payload())) {
                return true;
            }
        }
        return false;
    }
}
