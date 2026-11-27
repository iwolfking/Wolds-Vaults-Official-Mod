package xyz.iwolfking.woldsvaults.spellcraft.spell;

import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.ability.SpellAbilities;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStats;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphViewLine;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel.Bundle;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel.Cast;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel.Pause;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel.Step;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

final class SpellParser {
    final SpellcraftConfig config;
    final GlyphView view;
    final Function<String, String> specOf;
    final ReadMode mode;
    final Map<Integer, GlyphState> forced;
    final Map<GlyphType, Integer> saved;
    final List<Step> root = new ArrayList<>();
    final List<String> errors = new ArrayList<>();
    final Set<String> groups = new LinkedHashSet<>();
    final Set<String> castGroups = new LinkedHashSet<>();
    final Map<GlyphType, Integer> uses = new EnumMap<>(GlyphType.class);
    final Map<GlyphType, Integer> activeUses = new EnumMap<>(GlyphType.class);
    final Map<Integer, PlacedGlyph> byToken = new TreeMap<>();
    List<Step> sink = this.root;
    Object target;
    List<PlacedGlyph> targetGlyphs;
    Set<String> targetTraits = Set.of();
    String targetSpec;
    int depthCount;
    int nesting;
    boolean shotgunOnBundle;
    boolean awaitingStep;
    GlyphState inactiveBundle;
    final GlyphStacking.Pool spellPool = new GlyphStacking.Pool();
    final List<Bundle> openBundles = new ArrayList<>();
    final List<PlacedGlyph> spellWide = new ArrayList<>();
    final Set<Integer> spellWideNew = new LinkedHashSet<>();

    private SpellParser(SpellcraftConfig config, GlyphView view, Function<String, String> specOf, ReadMode mode, Map<Integer, GlyphState> forced,
                        Map<GlyphType, Integer> saved) {
        this.config = config;
        this.view = view;
        this.specOf = specOf;
        this.mode = mode;
        this.forced = forced;
        this.saved = saved;
    }

    static SpellModel build(SpellcraftConfig config, GlyphView view, int depth, List<String> tokens, Function<String, String> specOf,
                            ReadMode mode, Map<Integer, GlyphState> forced, Map<GlyphType, Integer> saved) {
        SpellParser parser = new SpellParser(config, view, specOf, mode, forced, saved);
        if (tokens.isEmpty()) {
            parser.errors.add("the spell is empty");
        } else if (GlyphType.byId(tokens.get(0)) != null) {
            parser.errors.add("a spell starts with an ability");
        }
        for (int i = 0; i < tokens.size(); i++) {
            parser.accept(i, tokens.get(i));
        }
        parser.finishBundles();
        SpellWideBinding.bind(parser);
        int limit = Math.min(depth, config.rules.maxGlyphsPerSpell);
        if (parser.depthCount > limit) {
            parser.errors.add("this item holds at most " + limit + " glyphs per spell");
        }
        long cap = config.rules.maxCastsPerTrigger * 4L;
        int potential = countCasts(parser.root, 1L, cap, true);
        if (potential > config.rules.maxCastsPerTrigger) {
            parser.errors.add("one press would make " + potential + " casts; at most " + config.rules.maxCastsPerTrigger + " are allowed");
        }
        int casts = countCasts(parser.root, 1L, cap, false);
        Map<Step, GlyphStacking.Pool> stepPools = new IdentityHashMap<>();
        collectPools(config, parser.root, stepPools);
        float manaFactor = Math.max(config.rules.manaFactorFloor, parser.spellPool.mana()) * healManaFactor(parser.root, stepPools);
        return new SpellModel(parser, SpellArcana.arcana(config, parser), casts, manaFactor, stepPools);
    }

    /**
     * Intensify on a heal costs spell mana instead of duration: the product of every live heal step's heal mana total,
     * applied after the mana floor so billing and every display agree.
     */
    private static float healManaFactor(List<Step> steps, Map<Step, GlyphStacking.Pool> stepPools) {
        float factor = 1.0F;
        for (Step step : steps) {
            if (step instanceof Cast cast && cast.live() && SpellAbilities.isHeal(cast.spec())) {
                GlyphStacking.Pool pool = stepPools.get(cast);
                if (pool != null) {
                    factor *= pool.total(GlyphStats.HEAL_MANA);
                }
            }
        }
        return factor;
    }

    private static void collectPools(SpellcraftConfig config, List<Step> steps, Map<Step, GlyphStacking.Pool> pools) {
        for (Step step : steps) {
            if (step instanceof Cast cast) {
                pools.put(cast, PlacedGlyph.pool(config::glyph, cast.glyphs()));
            } else if (step instanceof Bundle bundle) {
                pools.put(bundle, PlacedGlyph.pool(config::glyph, bundle.glyphs()));
                collectPools(config, bundle.payload(), pools);
            }
        }
    }

    private static int countCasts(List<Step> steps, long multiplier, long cap, boolean potential) {
        long total = 0;
        for (Step step : steps) {
            if (step instanceof Cast cast) {
                if (!potential && !cast.live()) {
                    continue;
                }
                long copies = multiplier * (1 + (potential ? cast.potential(GlyphType.SHOTGUN) : cast.count(GlyphType.SHOTGUN)));
                int echo = potential ? cast.potential(GlyphType.ECHO) : cast.count(GlyphType.ECHO);
                total += echo > 0 ? copies * 2 : copies;
            } else if (step instanceof Bundle bundle) {
                if (!potential && !bundle.glyph().active()) {
                    continue;
                }
                int shotgun = potential ? bundle.potential(GlyphType.SHOTGUN) : bundle.count(GlyphType.SHOTGUN);
                total += countCasts(bundle.payload(), multiplier * (1 + shotgun), cap, potential);
            }
            if (total > cap) {
                return (int) cap;
            }
        }
        return (int) total;
    }

    void accept(int index, String token) {
        GlyphType type = GlyphType.byId(token);
        if (type == null) {
            acceptAbility(token);
            return;
        }
        SpellcraftConfig.GlyphDefinition definition = this.config.glyph(type);
        int placed = this.uses.merge(type, 1, Integer::sum);
        if (!this.config.glyph(type).isUnique()) {
            this.depthCount++;
        }
        GlyphViewLine line = this.view.get(type);
        boolean isNew = placed > this.saved.getOrDefault(type, 0);
        GlyphState state = GlyphState.ACTIVE;
        if (line == null) {
            state = GlyphState.INACTIVE_SOURCE;
            refuse(isNew, type.displayName() + " is not among this item's glyphs");
        } else if (placed > line.limit()) {
            state = GlyphState.INACTIVE_USES;
            refuse(isNew, "the best " + type.displayName() + " allows " + line.limit() + " per spell");
        }
        float potency = line == null ? 0.0F : line.potency();
        float quality = line == null ? 0.0F : line.quality();
        int cost = line == null ? 0 : line.cost();
        if (definition.isDelay()) {
            PlacedGlyph glyph = finish(new PlacedGlyph(type, potency, quality, cost, state, false, index));
            this.sink.add(new Pause(glyph));
            applySpellEffects(definition, glyph);
            this.target = null;
            this.targetGlyphs = null;
            this.awaitingStep = true;
            return;
        }
        if (definition.isBundle()) {
            openBundle(finish(new PlacedGlyph(type, potency, quality, cost, state, false, index)), definition);
            return;
        }
        if (definition.spellWide()) {
            this.spellWide.add(new PlacedGlyph(type, potency, quality, cost, state, false, index));
            if (isNew) {
                this.spellWideNew.add(index);
            }
            return;
        }
        if (this.target == null) {
            this.errors.add(this.awaitingStep ? type.displayName() + " follows a Delay; put it before the Delay, or after the next ability"
                    : type.displayName() + " has no ability or orb before it to modify");
            record(new PlacedGlyph(type, potency, quality, cost, state == GlyphState.ACTIVE ? GlyphState.DORMANT : state, false, index));
            return;
        }
        for (PlacedGlyph existing : this.targetGlyphs) {
            if (excludes(definition, existing.type()) || excludes(this.config.glyph(existing.type()), type)) {
                this.errors.add(type.displayName() + " can't modify the same step as " + existing.type().displayName());
            }
        }
        if (!type.fits(this.config, this.targetTraits, this.targetSpec)) {
            refuse(isNew, type.displayName() + " doesn't fit " + describeTarget());
            if (state == GlyphState.ACTIVE) {
                state = GlyphState.DORMANT;
            }
        }
        PlacedGlyph glyph = finish(new PlacedGlyph(type, potency, quality, cost, state, false, index));
        this.targetGlyphs.add(glyph);
        applySpellEffects(definition, glyph);
        if (glyph.active() && type == GlyphType.SHOTGUN && this.target instanceof Bundle bundle && bundle.glyph().active()) {
            this.shotgunOnBundle = true;
        }
    }

    void refuse(boolean isNew, String message) {
        if (this.mode == ReadMode.STRICT_NEW && isNew) {
            this.errors.add(message);
        }
    }

    PlacedGlyph finish(PlacedGlyph glyph) {
        PlacedGlyph result = force(glyph);
        if (result.active() && this.inactiveBundle != null) {
            result = new PlacedGlyph(result.type(), result.potency(), result.quality(), result.cost(), this.inactiveBundle, true, result.token());
        }

        record(result);
        return result;
    }

    PlacedGlyph force(PlacedGlyph glyph) {
        if (glyph.active()) {
            GlyphState forcedState = this.forced.get(glyph.token());
            if (forcedState != null && forcedState != GlyphState.ACTIVE) {
                return new PlacedGlyph(glyph.type(), glyph.potency(), glyph.quality(), glyph.cost(), forcedState, false, glyph.token());
            }
        }
        return glyph;
    }

    void record(PlacedGlyph glyph) {
        this.byToken.put(glyph.token(), glyph);
        if (glyph.active()) {
            this.activeUses.merge(glyph.type(), 1, Integer::sum);
        }
    }

    static boolean excludes(SpellcraftConfig.GlyphDefinition definition, GlyphType other) {
        for (String excluded : definition.excludes) {
            if (GlyphType.byId(excluded) == other) {
                return true;
            }
        }
        return false;
    }

    void acceptAbility(String group) {
        String spec = this.specOf.apply(group);
        if (spec == null) {
            this.errors.add(group + " is not an ability");
            return;
        }
        if (SpellAbilities.get(this.config, spec) == null) {
            this.errors.add(SpellAbilities.displayName(spec) + " can't be cast by a spell");
        }
        if (!this.groups.add(group)) {
            this.errors.add(AbilityLookup.groupName(group) + " appears twice in the spell");
        }
        if (this.inactiveBundle == null) {
            this.castGroups.add(group);
        }
        Set<String> traits = SpellModel.traits(this.config, spec);
        if (traits.contains("hold")) {
            this.errors.add(AbilityLookup.groupName(group) + " is a hold ability and can't be part of a spell");
        }
        if (traits.contains("toggle") && this.groups.size() > 1) {
            this.errors.add(AbilityLookup.groupName(group) + " is a toggle and can only start a spell");
        }
        if (this.config.isDisabled(spec)) {
            this.errors.add(AbilityLookup.groupName(group) + " is disabled for spellcraft");
        }
        if (!this.openBundles.isEmpty() && !traits.contains("payload")) {
            this.errors.add(SpellAbilities.displayName(spec) + " can't ride in a Bundled Spell payload");
        }
        this.awaitingStep = false;
        List<PlacedGlyph> glyphs = new ArrayList<>();
        Cast cast = new Cast(group, spec, glyphs, this.inactiveBundle == null);
        this.sink.add(cast);
        this.target = cast;
        this.targetGlyphs = glyphs;
        this.targetTraits = traits;
        this.targetSpec = spec;
    }

    void openBundle(PlacedGlyph glyph, SpellcraftConfig.GlyphDefinition definition) {
        this.nesting++;
        if (this.nesting > this.config.rules.maxBundleNesting) {
            this.errors.add("Bundled Spells nest at most " + this.config.rules.maxBundleNesting + " deep");
        }
        if (this.groups.isEmpty()) {
            this.errors.add("a Bundled Spell needs the trigger ability before it");
        }
        this.awaitingStep = false;
        List<PlacedGlyph> glyphs = new ArrayList<>();
        Bundle bundle = new Bundle(glyph, glyphs, new ArrayList<>());
        this.sink.add(bundle);
        this.sink = bundle.payload();
        this.openBundles.add(bundle);
        this.target = bundle;
        this.targetGlyphs = glyphs;
        this.targetTraits = Set.of(GlyphType.BUNDLE_TRAIT);
        this.targetSpec = null;
        applySpellEffects(definition, glyph);
        if (!glyph.active() && this.inactiveBundle == null) {
            this.inactiveBundle = glyph.state();
        }
    }

    void finishBundles() {
        if (this.awaitingStep) {
            this.errors.add("a Delay pauses before the next ability, so it needs an ability after it");
        }
        for (Bundle bundle : this.openBundles) {
            if (bundle.payload().stream().noneMatch(step -> step instanceof Cast || step instanceof Bundle)) {
                this.errors.add("a Bundled Spell carries no ability");
            }
        }
    }

    void applySpellEffects(SpellcraftConfig.GlyphDefinition definition, PlacedGlyph glyph) {
        if (glyph.active()) {
            this.spellPool.add(definition, definition.spell, glyph.potency());
        }
    }

    String describeTarget() {
        if (this.target instanceof Cast cast) {
            return cast.spec() == null ? AbilityLookup.groupName(cast.group()) : SpellAbilities.displayName(cast.spec());
        }
        return "a Bundled Spell orb";
    }
}
