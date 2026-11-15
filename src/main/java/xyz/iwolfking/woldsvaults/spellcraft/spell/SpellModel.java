package xyz.iwolfking.woldsvaults.spellcraft.spell;

import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStats;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A spell read against its item's glyph view and the table: the timeline of steps with each glyph's line and
 * state, the arcana it costs, its spell-wide mana, cooldown and haste factors, each step's stacked glyph stats, how
 * many ability casts one press makes, and every rule it breaks. The same model drives casting (server), the focus
 * screen and tooltips (client).
 * <p> Grammar: the first token is the trigger ability; a modifier glyph changes the nearest ability or orb before it;
 * Delay pauses; Bundled Spell throws an orb, modified by the glyphs right after it, whose payload is everything from
 * the next ability on; any other ability token is a chained step. A spell-wide glyph (the table's {@code scope})
 * ignores where it sits: its step effects reach every live ability step and active orb of the spell it fits, payload
 * steps included, and its spell effects, uses and arcana count once.
 * <p> Glyph states: a glyph with no line in the view, a copy beyond the view line's per-spell limit, one that doesn't
 * fit its step (a spell-wide glyph: no step of the spell) and one greyed by the item's budget pass all do nothing and
 * cost nothing. An inactive Bundled Spell takes its orb glyphs and its whole payload with it, but not the spell-wide
 * glyphs placed after it; an inactive Delay doesn't pause. Only structural problems are errors when casting; while
 * editing, glyphs placed new beyond what the view backs are errors too.
 */
public final class SpellModel {
    private final List<Step> steps;
    private final List<String> errors;
    private final Set<String> groups;
    private final Set<String> castGroups;
    private final Map<GlyphType, Integer> uses;
    private final Map<Integer, PlacedGlyph> glyphs;
    private final float arcana;
    private final float escalation;
    private final int casts;
    private final float manaFactor;
    private final float cooldownBase;
    private final float glyphHaste;
    private final Map<Step, GlyphStacking.Pool> stepPools;

    public sealed interface Step permits Cast, Pause, Bundle {
    }

    /**
     * An ability step: the ability group, its current specialization id, the glyphs that modify it (those placed on it
     * and every spell-wide glyph that fits it), and whether it casts.
     */
    public record Cast(String group, @Nullable String spec, List<PlacedGlyph> glyphs, boolean live) implements Step {
        public int count(GlyphType type) {
            return PlacedGlyph.countActive(this.glyphs, type);
        }

        int potential(GlyphType type) {
            return PlacedGlyph.countPotential(this.glyphs, type);
        }
    }

    public record Pause(PlacedGlyph glyph) implements Step {
    }

    /** A Bundled Spell: its own glyph, the glyphs that modify the orb (spell-wide ones included), and its payload. */
    public record Bundle(PlacedGlyph glyph, List<PlacedGlyph> glyphs, List<Step> payload) implements Step {
        public int count(GlyphType type) {
            return PlacedGlyph.countActive(this.glyphs, type);
        }

        int potential(GlyphType type) {
            return PlacedGlyph.countPotential(this.glyphs, type);
        }
    }

    SpellModel(SpellParser parser, SpellArcana.Arcana arcana, int casts, float manaFactor, Map<Step, GlyphStacking.Pool> stepPools) {
        this.steps = Collections.unmodifiableList(parser.root);
        this.errors = Collections.unmodifiableList(parser.errors);
        this.groups = Collections.unmodifiableSet(parser.groups);
        this.castGroups = Collections.unmodifiableSet(parser.castGroups);
        this.uses = Collections.unmodifiableMap(parser.uses);
        this.glyphs = Collections.unmodifiableMap(parser.byToken);
        this.arcana = arcana.total();
        this.escalation = arcana.escalation();
        this.casts = casts;
        this.manaFactor = manaFactor;
        GlyphStacking.Stat cooldown = parser.spellPool.stat(GlyphStats.COOLDOWN);
        this.cooldownBase = cooldown == null ? 1.0F : cooldown.favourable();
        float penalty = cooldown == null ? 1.0F : cooldown.total() / cooldown.favourable();
        this.glyphHaste = parser.spellPool.total(GlyphStats.HASTE) / penalty;
        this.stepPools = stepPools;
    }

    public List<Step> steps() {
        return this.steps;
    }

    public List<String> errors() {
        return this.errors;
    }

    public boolean isValid() {
        return this.errors.isEmpty();
    }

    /** Every ability group the spell holds; each one is claimed by this spell's bar slot and gates its cast. */
    public Set<String> groups() {
        return this.groups;
    }

    /** The groups that actually cast: those outside the payload of an inactive Bundled Spell. */
    public Set<String> castGroups() {
        return this.castGroups;
    }

    /** Placements of each glyph type, whatever their state. */
    public Map<GlyphType, Integer> uses() {
        return this.uses;
    }

    /** The glyph at a token index, or null when that token is an ability. */
    @Nullable
    public PlacedGlyph glyphAt(int token) {
        return this.glyphs.get(token);
    }

    /** Every placed glyph in token order. */
    public List<PlacedGlyph> glyphs() {
        return List.copyOf(this.glyphs.values());
    }

    public float arcana() {
        return this.arcana;
    }

    /** The escalation factor applied to the spell's common glyph and ability step costs. */
    public float escalation() {
        return this.escalation;
    }

    public int casts() {
        return this.casts;
    }

    /** The slot's mana multiplier, floored at the table's minimum. */
    public float manaFactor() {
        return this.manaFactor;
    }

    /** The base-cooldown cut from favourable cooldown glyphs (the unique cooldown glyphs); applied before haste. */
    public float cooldownBase() {
        return this.cooldownBase;
    }

    /**
     * The spell's glyph haste: Quicken over every penalty (cooldown costs and haste downsides such as Discount). It
     * multiplies the player's capped haste factor, and the result is capped again (see {@code SpellHaste.spellFactor}).
     */
    public float glyphHaste() {
        return this.glyphHaste;
    }

    /**
     * The stacked step stats of an ability step or Bundled Spell orb of this spell (its active glyphs only), or an
     * empty pool for a Delay or a step from another model.
     */
    public GlyphStacking.Pool stepPool(Step step) {
        GlyphStacking.Pool pool = this.stepPools.get(step);
        return pool != null ? pool : new GlyphStacking.Pool();
    }

    public boolean hasCharge() {
        return anyGlyph(this.steps, GlyphType.CHARGE);
    }

    private static boolean anyGlyph(List<Step> steps, GlyphType type) {
        for (Step step : steps) {
            if (step instanceof Cast cast && cast.count(type) > 0) {
                return true;
            }
            if (step instanceof Bundle bundle && (bundle.count(type) > 0 || anyGlyph(bundle.payload(), type))) {
                return true;
            }
        }
        return false;
    }

    public static SpellModel build(SpellcraftConfig config, GlyphView view, int depth, List<String> tokens, Function<String, String> specOf,
                                   ReadMode mode) {
        return build(config, view, depth, tokens, specOf, mode, Map.of(), Map.of());
    }

    /**
     * Reads a spell. {@code specOf} maps an ability group to the player's current specialization id (null when the
     * group is unknown). {@code forced} sets the state of glyphs that would otherwise be active, by token index (the
     * budget pass greys glyphs this way). {@code saved} holds, per type, how many copies the stored spell already
     * placed; {@link ReadMode#STRICT_NEW} only refuses copies beyond those.
     */
    public static SpellModel build(SpellcraftConfig config, GlyphView view, int depth, List<String> tokens, Function<String, String> specOf,
                                   ReadMode mode, Map<Integer, GlyphState> forced, Map<GlyphType, Integer> saved) {
        return SpellParser.build(config, view, depth, tokens, specOf, mode, forced, saved);
    }

    /** The traits of a specialization in lower case, from the table. */
    public static Set<String> traits(SpellcraftConfig config, String spec) {
        List<String> list = config.abilityTraits.get(spec);
        if (list == null) {
            return Set.of();
        }
        Set<String> traits = new LinkedHashSet<>();
        list.forEach(trait -> traits.add(trait.toLowerCase(Locale.ROOT)));
        return traits;
    }
}
