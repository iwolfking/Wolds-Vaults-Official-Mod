package xyz.iwolfking.woldsvaults.augments.spell;

import xyz.iwolfking.woldsvaults.augments.AugmentAbilities;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.pool.ViewLine;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * An augmented spell read against its item's glyph view and the table: the timeline of steps with each glyph's line
 * and state, the arcana it costs, its spell-wide mana, cooldown and haste factors, each step's stacked glyph stats, how
 * many ability casts one press makes, and every rule it breaks. The same model drives casting (server), the focus
 * screen and tooltips (client).
 * <p>
 * Grammar: the first token is the trigger ability; a modifier glyph changes the nearest ability or orb before it;
 * Delay pauses; Bundled Spell throws an orb, modified by the glyphs right after it, whose payload is everything from the
 * next ability on; any other ability token is a chained step.
 * <p>
 * Glyph states: a glyph with no line in the view, a copy beyond the view line's per-spell limit, one that doesn't fit
 * its step and one greyed by the item's budget pass all do nothing and cost nothing. An inactive Bundled Spell takes its
 * orb glyphs and its whole payload with it; an inactive Delay doesn't pause. Only structural problems are errors when
 * casting; while editing, glyphs placed new beyond what the view backs are errors too.
 */
public final class SpellModel {
    private final List<Step> steps;
    private final List<String> errors;
    private final Set<String> groups;
    private final Set<String> castGroups;
    private final Map<AugmentType, Integer> uses;
    private final Map<AugmentType, Integer> activeUses;
    private final Map<Integer, Glyph> glyphs;
    private final float arcana;
    private final float escalation;
    private final int glyphCount;
    private final int casts;
    private final int nesting;
    private final float manaFactor;
    private final float cooldownFactor;
    private final float hasteMultiplier;
    private final GlyphStacking.Pool spellPool;
    private final Map<Step, GlyphStacking.Pool> stepPools;
    private final List<FocusAugmentsConfig.Combo> combos;

    /** How strictly a spell is read. */
    public enum Mode {
        /** Casting and display: only structural problems are errors; glyphs the view can't back go inactive. */
        LENIENT,
        /** Every glyph must be backed by the view, within its limit, and fit its step. */
        STRICT,
        /**
         * Editing: only copies the saved spell already held may lack a line, exceed the limit or not fit their step
         * (they stay, inactive); new copies must come from the view and fit.
         */
        STRICT_NEW
    }

    /** Why a glyph does or doesn't act. */
    public enum State {
        ACTIVE,
        DORMANT,
        INACTIVE_SOURCE,
        INACTIVE_USES,
        INACTIVE_BUDGET
    }

    /**
     * One placed glyph with its view line's potency, quality and cost, its state, and its token index in the spell.
     * {@code dependent}: it would act, but the Bundled Spell it rides on is inactive ({@code state} is that glyph's).
     */
    public record Glyph(AugmentType type, float potency, float quality, int cost, State state, boolean dependent, int token) {
        public boolean active() {
            return this.state == State.ACTIVE;
        }

        boolean potential() {
            return this.state != State.DORMANT;
        }
    }

    public sealed interface Step permits Cast, Pause, Bundle {
    }

    /** An ability step: the ability group, its current specialization id, the glyphs on it, and whether it casts. */
    public record Cast(String group, @Nullable String spec, List<Glyph> glyphs, boolean live) implements Step {
        public int count(AugmentType type) {
            return (int) this.glyphs.stream().filter(glyph -> glyph.type() == type && glyph.active()).count();
        }

        int potential(AugmentType type) {
            return (int) this.glyphs.stream().filter(glyph -> glyph.type() == type && glyph.potential()).count();
        }
    }

    public record Pause(Glyph glyph) implements Step {
    }

    /** A Bundled Spell: its own glyph, the glyphs that modify the orb, and its payload. */
    public record Bundle(Glyph glyph, List<Glyph> glyphs, List<Step> payload) implements Step {
        public int count(AugmentType type) {
            return (int) this.glyphs.stream().filter(glyph -> glyph.type() == type && glyph.active()).count();
        }

        int potential(AugmentType type) {
            return (int) this.glyphs.stream().filter(glyph -> glyph.type() == type && glyph.potential()).count();
        }
    }

    private SpellModel(Builder builder, Arcana arcana, int casts, float manaFactor, Map<Step, GlyphStacking.Pool> stepPools) {
        this.steps = Collections.unmodifiableList(builder.root);
        this.errors = Collections.unmodifiableList(builder.errors);
        this.groups = Collections.unmodifiableSet(builder.groups);
        this.castGroups = Collections.unmodifiableSet(builder.castGroups);
        this.uses = Collections.unmodifiableMap(builder.uses);
        this.activeUses = Collections.unmodifiableMap(builder.activeUses);
        this.glyphs = Collections.unmodifiableMap(builder.byToken);
        this.arcana = arcana.total();
        this.escalation = arcana.escalation();
        this.glyphCount = builder.depthCount;
        this.casts = casts;
        this.nesting = builder.maxNesting;
        this.manaFactor = manaFactor;
        this.cooldownFactor = builder.spellPool.total("cooldown");
        this.hasteMultiplier = builder.spellPool.total("haste");
        this.spellPool = builder.spellPool;
        this.stepPools = stepPools;
        this.combos = Collections.unmodifiableList(builder.appliedCombos);
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
    public Map<AugmentType, Integer> uses() {
        return this.uses;
    }

    /** Placements of each glyph type that are active. */
    public Map<AugmentType, Integer> activeUses() {
        return this.activeUses;
    }

    /** The glyph at a token index, or null when that token is an ability. */
    @Nullable
    public Glyph glyphAt(int token) {
        return this.glyphs.get(token);
    }

    /** Every placed glyph in token order. */
    public List<Glyph> glyphs() {
        return List.copyOf(this.glyphs.values());
    }

    /** How many placed glyphs are not active. */
    public int inactiveCount() {
        return (int) this.glyphs.values().stream().filter(glyph -> !glyph.active()).count();
    }

    public float arcana() {
        return this.arcana;
    }

    /** The escalation factor applied to the spell's common glyph and ability step costs. */
    public float escalation() {
        return this.escalation;
    }

    /** Placed glyphs that count toward the item's depth (unique glyphs don't). */
    public int glyphCount() {
        return this.glyphCount;
    }

    public int casts() {
        return this.casts;
    }

    public int nesting() {
        return this.nesting;
    }

    /** The slot's mana multiplier, floored at the table's minimum. */
    public float manaFactor() {
        return this.manaFactor;
    }

    public float cooldownFactor() {
        return this.cooldownFactor;
    }

    /** The multiplier on the player's haste factor for this spell (Quicken up, Discount down); capped by spell haste. */
    public float hasteMultiplier() {
        return this.hasteMultiplier;
    }

    public List<FocusAugmentsConfig.Combo> combos() {
        return this.combos;
    }

    /** The spell-wide glyph stats (mana, cooldown, haste, efficiency) as stacked, before the mana floor. */
    public GlyphStacking.Pool spellPool() {
        return this.spellPool;
    }

    /**
     * The stacked step stats of an ability step or Bundled Spell orb of this spell (its active glyphs only), or an empty
     * pool for a Delay or a step from another model.
     */
    public GlyphStacking.Pool stepPool(Step step) {
        GlyphStacking.Pool pool = this.stepPools.get(step);
        return pool != null ? pool : new GlyphStacking.Pool();
    }

    public boolean hasCharge() {
        return anyGlyph(this.steps, AugmentType.CHARGE);
    }

    private static boolean anyGlyph(List<Step> steps, AugmentType type) {
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

    public static SpellModel build(FocusAugmentsConfig config, GlyphView view, int depth, List<String> tokens, Function<String, String> specOf,
                                   Mode mode) {
        return build(config, view, depth, tokens, specOf, mode, Map.of(), Map.of());
    }

    /**
     * Reads a spell. {@code specOf} maps an ability group to the player's current specialization id (null when the
     * group is unknown). {@code forced} sets the state of glyphs that would otherwise be active, by token index (the
     * budget pass greys glyphs this way). {@code saved} holds, per type, how many copies the stored spell already
     * placed; {@link Mode#STRICT_NEW} only refuses copies beyond those.
     */
    public static SpellModel build(FocusAugmentsConfig config, GlyphView view, int depth, List<String> tokens, Function<String, String> specOf,
                                   Mode mode, Map<Integer, State> forced, Map<AugmentType, Integer> saved) {
        Builder builder = new Builder(config, view, specOf, mode, forced, saved);
        if (tokens.isEmpty()) {
            builder.errors.add("the spell is empty");
        } else if (AugmentType.byId(tokens.get(0)) != null) {
            builder.errors.add("a spell starts with an ability");
        }
        for (int i = 0; i < tokens.size(); i++) {
            builder.accept(i, tokens.get(i));
        }
        builder.finishBundles();
        int limit = Math.min(depth, config.rules.maxGlyphsPerSpell);
        if (builder.depthCount > limit) {
            builder.errors.add("this item holds at most " + limit + " glyphs per spell");
        }
        long cap = config.rules.maxCastsPerTrigger * 4L;
        int potential = countCasts(builder.root, 1L, cap, true);
        if (potential > config.rules.maxCastsPerTrigger) {
            builder.errors.add("one press would make " + potential + " casts; at most " + config.rules.maxCastsPerTrigger + " are allowed");
        }
        int casts = countCasts(builder.root, 1L, cap, false);
        float manaFactor = Math.max(config.rules.manaFactorFloor, builder.spellPool.mana());
        Map<Step, GlyphStacking.Pool> stepPools = new IdentityHashMap<>();
        collectPools(config, builder.root, stepPools);
        return new SpellModel(builder, arcana(config, builder), casts, manaFactor, stepPools);
    }

    private static void collectPools(FocusAugmentsConfig config, List<Step> steps, Map<Step, GlyphStacking.Pool> pools) {
        for (Step step : steps) {
            if (step instanceof Cast cast) {
                pools.put(cast, CastModifiers.pool(config::glyph, cast.glyphs()));
            } else if (step instanceof Bundle bundle) {
                pools.put(bundle, CastModifiers.pool(config::glyph, bundle.glyphs()));
                collectPools(config, bundle.payload(), pools);
            }
        }
    }

    /** How many times one spell may place an item's own line: the line's uses, capped by its cap or the glyph's. */
    public static int perSpellLimit(FocusAugmentsConfig config, SpellcraftItemData.Line line) {
        return GlyphView.ownLimit(config, line);
    }

    public static boolean isUnique(FocusAugmentsConfig config, AugmentType type) {
        return "unique".equals(config.glyph(type).glyphClass);
    }

    /** The traits of a specialization in lower case, from the table. */
    public static Set<String> traits(FocusAugmentsConfig config, String spec) {
        List<String> list = config.abilityTraits.get(spec);
        if (list == null) {
            return Set.of();
        }
        Set<String> traits = new LinkedHashSet<>();
        list.forEach(trait -> traits.add(trait.toLowerCase(Locale.ROOT)));
        return traits;
    }

    private record Arcana(float total, float escalation) {
    }

    /**
     * Live arcana over active glyphs: common glyphs pay their view line's cost and escalate; unique glyphs are free
     * while every active glyph in the spell is unique, and otherwise cost the table's {@code uniqueMixCost} each, never
     * escalating and adding no escalation step; combos count active glyphs.
     */
    private static Arcana arcana(FocusAugmentsConfig config, Builder builder) {
        List<Glyph> active = builder.byToken.values().stream().filter(Glyph::active).toList();
        boolean allUnique = active.stream().allMatch(glyph -> isUnique(config, glyph.type()));
        float common = 0.0F;
        float unique = 0.0F;
        int escalating = 0;
        for (Glyph glyph : active) {
            if (isUnique(config, glyph.type())) {
                unique += allUnique ? 0.0F : config.rules.uniqueMixCost;
            } else {
                common += glyph.cost();
                if (!config.glyph(glyph.type()).noEscalation) {
                    escalating++;
                }
            }
        }
        int steps = Math.max(0, builder.castGroups.size() - 1);
        int n = escalating + steps;
        float escalation = 1.0F + config.rules.escalationPerGlyph * Math.max(0, n - 1);
        float total = (common + config.rules.abilityStepCost * steps) * escalation + unique;
        for (FocusAugmentsConfig.Combo combo : config.combos) {
            AugmentType a = AugmentType.byId(combo.a);
            AugmentType b = AugmentType.byId(combo.b);
            boolean hit;
            if (isPair(a, b, AugmentType.SHOTGUN, AugmentType.BUNDLED_SPELL)) {
                hit = builder.shotgunOnBundle;
            } else {
                hit = builder.activeUses.containsKey(a) && builder.activeUses.containsKey(b);
            }
            if (hit) {
                total += combo.cost;
                builder.appliedCombos.add(combo);
            }
        }
        return new Arcana(total, escalation);
    }

    private static boolean isPair(AugmentType a, AugmentType b, AugmentType x, AugmentType y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    private static int countCasts(List<Step> steps, long multiplier, long cap, boolean potential) {
        long total = 0;
        for (Step step : steps) {
            if (step instanceof Cast cast) {
                if (!potential && !cast.live()) {
                    continue;
                }
                long copies = multiplier * (1 + (potential ? cast.potential(AugmentType.SHOTGUN) : cast.count(AugmentType.SHOTGUN)));
                int echo = potential ? cast.potential(AugmentType.ECHO) : cast.count(AugmentType.ECHO);
                total += echo > 0 ? copies * 2 : copies;
            } else if (step instanceof Bundle bundle) {
                if (!potential && !bundle.glyph().active()) {
                    continue;
                }
                int shotgun = potential ? bundle.potential(AugmentType.SHOTGUN) : bundle.count(AugmentType.SHOTGUN);
                total += countCasts(bundle.payload(), multiplier * (1 + shotgun), cap, potential);
            }
            if (total > cap) {
                return (int) cap;
            }
        }
        return (int) total;
    }

    private static final class Builder {
        final FocusAugmentsConfig config;
        final GlyphView view;
        final Function<String, String> specOf;
        final Mode mode;
        final Map<Integer, State> forced;
        final Map<AugmentType, Integer> saved;
        final List<Step> root = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final Set<String> groups = new LinkedHashSet<>();
        final Set<String> castGroups = new LinkedHashSet<>();
        final Map<AugmentType, Integer> uses = new EnumMap<>(AugmentType.class);
        final Map<AugmentType, Integer> activeUses = new EnumMap<>(AugmentType.class);
        final Map<Integer, Glyph> byToken = new TreeMap<>();
        final List<FocusAugmentsConfig.Combo> appliedCombos = new ArrayList<>();
        List<Step> sink = this.root;
        Object target;
        List<Glyph> targetGlyphs;
        Set<String> targetTraits = Set.of();
        String targetSpec;
        int depthCount;
        int nesting;
        int maxNesting;
        boolean shotgunOnBundle;
        boolean awaitingStep;
        State inactiveBundle;
        final GlyphStacking.Pool spellPool = new GlyphStacking.Pool();
        final List<Bundle> openBundles = new ArrayList<>();

        Builder(FocusAugmentsConfig config, GlyphView view, Function<String, String> specOf, Mode mode, Map<Integer, State> forced,
                Map<AugmentType, Integer> saved) {
            this.config = config;
            this.view = view;
            this.specOf = specOf;
            this.mode = mode;
            this.forced = forced;
            this.saved = saved;
        }

        void accept(int index, String token) {
            AugmentType type = AugmentType.byId(token);
            if (type == null) {
                acceptAbility(token);
                return;
            }
            FocusAugmentsConfig.Glyph spec = this.config.glyph(type);
            int placed = this.uses.merge(type, 1, Integer::sum);
            if (!isUnique(this.config, type)) {
                this.depthCount++;
            }
            ViewLine line = this.view.get(type);
            boolean isNew = placed > this.saved.getOrDefault(type, 0);
            State state = State.ACTIVE;
            if (line == null) {
                state = State.INACTIVE_SOURCE;
                refuse(isNew, type.displayName() + " is not among this item's glyphs");
            } else if (placed > line.limit()) {
                state = State.INACTIVE_USES;
                refuse(isNew, "the best " + type.displayName() + " allows " + line.limit() + " per spell");
            }
            float potency = line == null ? 0.0F : line.potency();
            float quality = line == null ? 0.0F : line.quality();
            int cost = line == null ? 0 : line.cost();
            if ("delay".equals(spec.kind)) {
                Glyph glyph = finish(new Glyph(type, potency, quality, cost, state, false, index));
                this.sink.add(new Pause(glyph));
                applySpellEffects(spec, glyph);
                this.target = null;
                this.targetGlyphs = null;
                this.awaitingStep = true;
                return;
            }
            if ("bundle".equals(spec.kind)) {
                openBundle(finish(new Glyph(type, potency, quality, cost, state, false, index)), spec);
                return;
            }
            if (this.target == null) {
                this.errors.add(this.awaitingStep ? type.displayName() + " follows a Delay; put it before the Delay, or after the next ability"
                        : type.displayName() + " has no ability or orb before it to modify");
                record(new Glyph(type, potency, quality, cost, state == State.ACTIVE ? State.DORMANT : state, false, index));
                return;
            }
            for (Glyph existing : this.targetGlyphs) {
                if (excludes(spec, existing.type()) || excludes(this.config.glyph(existing.type()), type)) {
                    this.errors.add(type.displayName() + " can't modify the same step as " + existing.type().displayName());
                }
            }
            if (!type.fits(this.config, this.targetTraits, this.targetSpec)) {
                refuse(isNew, type.displayName() + " doesn't fit " + describeTarget());
                if (state == State.ACTIVE) {
                    state = State.DORMANT;
                }
            }
            Glyph glyph = finish(new Glyph(type, potency, quality, cost, state, false, index));
            this.targetGlyphs.add(glyph);
            applySpellEffects(spec, glyph);
            if (glyph.active() && type == AugmentType.SHOTGUN && this.target instanceof Bundle bundle && bundle.glyph().active()) {
                this.shotgunOnBundle = true;
            }
        }

        void refuse(boolean isNew, String message) {
            if (this.mode == Mode.STRICT || (this.mode == Mode.STRICT_NEW && isNew)) {
                this.errors.add(message);
            }
        }

        Glyph finish(Glyph glyph) {
            Glyph result = glyph;
            if (result.active()) {
                State forcedState = this.forced.get(result.token());
                if (forcedState != null && forcedState != State.ACTIVE) {
                    result = new Glyph(result.type(), result.potency(), result.quality(), result.cost(), forcedState, false, result.token());
                }
            }
            if (result.active() && this.inactiveBundle != null) {
                result = new Glyph(result.type(), result.potency(), result.quality(), result.cost(), this.inactiveBundle, true, result.token());
            }
            record(result);
            return result;
        }

        void record(Glyph glyph) {
            this.byToken.put(glyph.token(), glyph);
            if (glyph.active()) {
                this.activeUses.merge(glyph.type(), 1, Integer::sum);
            }
        }

        static boolean excludes(FocusAugmentsConfig.Glyph spec, AugmentType other) {
            for (String excluded : spec.excludes) {
                if (AugmentType.byId(excluded) == other) {
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
            if (AugmentAbilities.get(this.config, spec) == null) {
                this.errors.add(AugmentAbilities.displayName(spec) + " can't be cast by a spell");
            }
            if (!this.groups.add(group)) {
                this.errors.add(group.replace('_', ' ') + " appears twice in the spell");
            }
            if (this.inactiveBundle == null) {
                this.castGroups.add(group);
            }
            Set<String> traits = traits(this.config, spec);
            if (traits.contains("hold")) {
                this.errors.add(group.replace('_', ' ') + " is a hold ability and can't be part of a spell");
            }
            if (traits.contains("toggle") && this.groups.size() > 1) {
                this.errors.add(group.replace('_', ' ') + " is a toggle and can only start a spell");
            }
            if (this.config.isDisabled(spec)) {
                this.errors.add(group.replace('_', ' ') + " is disabled for spellcraft");
            }
            if (!this.openBundles.isEmpty() && !traits.contains("payload")) {
                this.errors.add(AugmentAbilities.displayName(spec) + " can't ride in a Bundled Spell payload");
            }
            this.awaitingStep = false;
            List<Glyph> glyphs = new ArrayList<>();
            Cast cast = new Cast(group, spec, glyphs, this.inactiveBundle == null);
            this.sink.add(cast);
            this.target = cast;
            this.targetGlyphs = glyphs;
            this.targetTraits = traits;
            this.targetSpec = spec;
        }

        void openBundle(Glyph glyph, FocusAugmentsConfig.Glyph spec) {
            this.nesting++;
            this.maxNesting = Math.max(this.maxNesting, this.nesting);
            if (this.nesting > this.config.rules.maxBundleNesting) {
                this.errors.add("Bundled Spells nest at most " + this.config.rules.maxBundleNesting + " deep");
            }
            if (this.groups.isEmpty()) {
                this.errors.add("a Bundled Spell needs the trigger ability before it");
            }
            this.awaitingStep = false;
            List<Glyph> glyphs = new ArrayList<>();
            Bundle bundle = new Bundle(glyph, glyphs, new ArrayList<>());
            this.sink.add(bundle);
            this.sink = bundle.payload();
            this.openBundles.add(bundle);
            this.target = bundle;
            this.targetGlyphs = glyphs;
            this.targetTraits = Set.of(AugmentType.BUNDLE_TRAIT);
            this.targetSpec = null;
            applySpellEffects(spec, glyph);
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

        void applySpellEffects(FocusAugmentsConfig.Glyph spec, Glyph glyph) {
            if (glyph.active()) {
                this.spellPool.add(spec, spec.spell, glyph.potency());
            }
        }

        String describeTarget() {
            if (this.target instanceof Cast cast) {
                return cast.spec() == null ? cast.group().replace('_', ' ') : AugmentAbilities.displayName(cast.spec());
            }
            return "a Bundled Spell orb";
        }
    }
}
