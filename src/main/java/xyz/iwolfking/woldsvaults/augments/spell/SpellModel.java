package xyz.iwolfking.woldsvaults.augments.spell;

import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * An augmented spell read against its item and the table: the timeline of steps with each glyph's rolled potency,
 * the arcana it costs, its spell-wide mana, cooldown and haste factors, how many ability casts one press makes, and
 * every rule it breaks. The same model drives casting (server), the focus screen and tooltips (client).
 * <p>
 * Grammar: the first token is the trigger ability; a modifier glyph changes the nearest ability or orb before it;
 * Delay pauses; Bundled Spell throws an orb, modified by the glyphs right after it, whose payload is everything from the
 * next ability on; any other ability token is a chained step.
 */
public final class SpellModel {
    private final List<Step> steps;
    private final List<String> errors;
    private final Set<String> groups;
    private final Map<AugmentType, Integer> uses;
    private final float arcana;
    private final int glyphCount;
    private final int casts;
    private final int nesting;
    private final float manaFactor;
    private final float cooldownFactor;
    private final float hasteMultiplier;
    private final List<FocusAugmentsConfig.Combo> combos;

    /** One placed glyph with its line's roll. A dormant glyph no longer fits its target and is skipped at cast. */
    public record Glyph(AugmentType type, float potency, float quality, int cost, boolean dormant) {
    }

    public sealed interface Step permits Cast, Pause, Bundle {
    }

    /** An ability step: the ability group, its current specialization id, and the glyphs on it. */
    public record Cast(String group, @Nullable String spec, List<Glyph> glyphs) implements Step {
        public int count(AugmentType type) {
            return (int) this.glyphs.stream().filter(glyph -> glyph.type() == type && !glyph.dormant()).count();
        }
    }

    public record Pause(Glyph glyph) implements Step {
    }

    /** A Bundled Spell: its own glyph, the glyphs that modify the orb, and its payload. */
    public record Bundle(Glyph glyph, List<Glyph> glyphs, List<Step> payload) implements Step {
        public int count(AugmentType type) {
            return (int) this.glyphs.stream().filter(glyph -> glyph.type() == type && !glyph.dormant()).count();
        }
    }

    private SpellModel(Builder builder, float arcana, int casts) {
        this.steps = Collections.unmodifiableList(builder.root);
        this.errors = Collections.unmodifiableList(builder.errors);
        this.groups = Collections.unmodifiableSet(builder.groups);
        this.uses = Collections.unmodifiableMap(builder.uses);
        this.arcana = arcana;
        this.glyphCount = builder.glyphCount;
        this.casts = casts;
        this.nesting = builder.maxNesting;
        this.manaFactor = builder.manaFactor;
        this.cooldownFactor = builder.cooldownFactor;
        this.hasteMultiplier = builder.hasteMultiplier;
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

    /** Every ability group the spell casts; each one is taken by this spell's bar slot. */
    public Set<String> groups() {
        return this.groups;
    }

    /** Placements of each glyph type, counted against the item's glyph uses. */
    public Map<AugmentType, Integer> uses() {
        return this.uses;
    }

    public float arcana() {
        return this.arcana;
    }

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

    /**
     * Reads a spell. {@code specOf} maps an ability group to the player's current specialization id (null when the
     * group is unknown). With {@code strict}, glyphs that don't fit their target are errors (editing); otherwise they go
     * dormant (casting after a specialization change).
     */
    public static SpellModel build(FocusAugmentsConfig config, List<SpellcraftItemData.Line> lines, int depth, List<String> tokens,
                                   Function<String, String> specOf, boolean strict) {
        Builder builder = new Builder(config, lines, specOf, strict);
        if (tokens.isEmpty()) {
            builder.errors.add("the spell is empty");
        } else if (AugmentType.byId(tokens.get(0)) != null) {
            builder.errors.add("a spell starts with an ability");
        }
        for (String token : tokens) {
            builder.accept(token);
        }
        builder.finishBundles();
        int limit = Math.min(depth, config.rules.maxGlyphsPerSpell);
        if (builder.glyphCount > limit) {
            builder.errors.add("this item holds at most " + limit + " glyphs per spell");
        }
        int casts = countCasts(builder.root, 1L, config.rules.maxCastsPerTrigger * 4L);
        if (casts > config.rules.maxCastsPerTrigger) {
            builder.errors.add("one press would make " + casts + " casts; at most " + config.rules.maxCastsPerTrigger + " are allowed");
        }
        builder.manaFactor = Math.max(config.rules.manaFactorFloor, builder.manaFactor);
        return new SpellModel(builder, arcana(config, builder), casts);
    }

    private static float arcana(FocusAugmentsConfig config, Builder builder) {
        float costs = builder.glyphCost + config.rules.abilityStepCost * Math.max(0, builder.groups.size() - 1);
        int n = builder.escalating + Math.max(0, builder.groups.size() - 1);
        float total = costs * (1.0F + config.rules.escalationPerGlyph * Math.max(0, n - 1));
        for (FocusAugmentsConfig.Combo combo : config.combos) {
            AugmentType a = AugmentType.byId(combo.a);
            AugmentType b = AugmentType.byId(combo.b);
            boolean hit;
            if (isPair(a, b, AugmentType.SHOTGUN, AugmentType.BUNDLED_SPELL)) {
                hit = builder.shotgunOnBundle;
            } else {
                hit = builder.uses.containsKey(a) && builder.uses.containsKey(b);
            }
            if (hit) {
                total += combo.cost;
                builder.appliedCombos.add(combo);
            }
        }
        return total;
    }

    private static boolean isPair(AugmentType a, AugmentType b, AugmentType x, AugmentType y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    private static int countCasts(List<Step> steps, long multiplier, long cap) {
        long total = 0;
        for (Step step : steps) {
            if (step instanceof Cast cast) {
                long copies = multiplier * (1 + cast.count(AugmentType.SHOTGUN));
                total += cast.count(AugmentType.ECHO) > 0 ? copies * 2 : copies;
            } else if (step instanceof Bundle bundle) {
                total += countCasts(bundle.payload(), multiplier * (1 + bundle.count(AugmentType.SHOTGUN)), cap);
            }
            if (total > cap) {
                return (int) cap;
            }
        }
        return (int) total;
    }

    private static final class Builder {
        final FocusAugmentsConfig config;
        final Map<AugmentType, SpellcraftItemData.Line> lines = new EnumMap<>(AugmentType.class);
        final Function<String, String> specOf;
        final boolean strict;
        final List<Step> root = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final Set<String> groups = new LinkedHashSet<>();
        final Map<AugmentType, Integer> uses = new EnumMap<>(AugmentType.class);
        final List<FocusAugmentsConfig.Combo> appliedCombos = new ArrayList<>();
        List<Step> sink = this.root;
        Object target;
        List<Glyph> targetGlyphs;
        Set<String> targetTraits = Set.of();
        int glyphCount;
        int escalating;
        float glyphCost;
        int nesting;
        int maxNesting;
        boolean shotgunOnBundle;
        boolean awaitingStep;
        float manaFactor = 1.0F;
        float cooldownFactor = 1.0F;
        float hasteMultiplier = 1.0F;
        final List<Bundle> openBundles = new ArrayList<>();

        Builder(FocusAugmentsConfig config, List<SpellcraftItemData.Line> lines, Function<String, String> specOf, boolean strict) {
            this.config = config;
            for (SpellcraftItemData.Line line : lines) {
                this.lines.put(line.type(), line);
            }
            this.specOf = specOf;
            this.strict = strict;
        }

        void accept(String token) {
            AugmentType type = AugmentType.byId(token);
            if (type == null) {
                acceptAbility(token);
                return;
            }
            SpellcraftItemData.Line line = this.lines.get(type);
            if (line == null) {
                this.errors.add(type.displayName() + " is not rolled on this item");
                return;
            }
            FocusAugmentsConfig.Glyph spec = this.config.glyph(type);
            int placed = this.uses.merge(type, 1, Integer::sum);
            this.glyphCount++;
            this.glyphCost += line.cost();
            if (!spec.noEscalation) {
                this.escalating++;
            }
            int limit = Math.min(spec.capPerSpell, line.uses());
            if (placed > limit) {
                this.errors.add(type.displayName() + " fits at most " + limit + " times in one spell on this item");
            }
            for (String excluded : spec.excludes) {
                AugmentType other = AugmentType.byId(excluded);
                if (other != null && this.uses.containsKey(other)) {
                    this.errors.add(type.displayName() + " can't share a spell with " + other.displayName());
                }
            }
            if ("delay".equals(spec.kind)) {
                Glyph glyph = new Glyph(type, line.potency(), line.quality(), line.cost(), false);
                this.sink.add(new Pause(glyph));
                applySpellEffects(spec, glyph);
                this.target = null;
                this.targetGlyphs = null;
                this.awaitingStep = true;
                return;
            }
            if ("bundle".equals(spec.kind)) {
                openBundle(new Glyph(type, line.potency(), line.quality(), line.cost(), false), spec);
                return;
            }
            if (this.target == null) {
                this.errors.add(this.awaitingStep ? type.displayName() + " follows a Delay; put it before the Delay, or after the next ability"
                        : type.displayName() + " has no ability or orb before it to modify");
                return;
            }
            boolean fits = type.fits(this.config, this.targetTraits);
            if (!fits && this.strict) {
                this.errors.add(type.displayName() + " doesn't fit " + describeTarget());
            }
            Glyph glyph = new Glyph(type, line.potency(), line.quality(), line.cost(), !fits);
            this.targetGlyphs.add(glyph);
            if (fits) {
                applySpellEffects(spec, glyph);
                if (type == AugmentType.SHOTGUN && this.target instanceof Bundle) {
                    this.shotgunOnBundle = true;
                }
            }
        }

        void acceptAbility(String group) {
            String spec = this.specOf.apply(group);
            if (spec == null) {
                this.errors.add(group + " is not an ability");
                return;
            }
            if (!this.groups.add(group)) {
                this.errors.add(group.replace('_', ' ') + " appears twice in the spell");
            }
            Set<String> traits = traits(spec);
            if (traits.contains("hold")) {
                this.errors.add(group.replace('_', ' ') + " is a hold ability and can't be part of a spell");
            }
            if (traits.contains("toggle") && this.groups.size() > 1) {
                this.errors.add(group.replace('_', ' ') + " is a toggle and can only start a spell");
            }
            if (this.config.isDisabled(spec)) {
                this.errors.add(group.replace('_', ' ') + " is disabled for spellcraft");
            }
            this.awaitingStep = false;
            List<Glyph> glyphs = new ArrayList<>();
            Cast cast = new Cast(group, spec, glyphs);
            this.sink.add(cast);
            this.target = cast;
            this.targetGlyphs = glyphs;
            this.targetTraits = traits;
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
            applySpellEffects(spec, glyph);
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
            for (FocusAugmentsConfig.Effect effect : spec.spell) {
                float factor = effect.factor(glyph.potency());
                switch (effect.stat) {
                    case "mana" -> this.manaFactor *= factor;
                    case "cooldown" -> this.cooldownFactor *= factor;
                    case "haste" -> this.hasteMultiplier *= factor;
                    default -> {
                    }
                }
            }
        }

        Set<String> traits(String spec) {
            List<String> list = this.config.abilityTraits.get(spec);
            if (list == null) {
                return Set.of();
            }
            Set<String> traits = new LinkedHashSet<>();
            list.forEach(trait -> traits.add(trait.toLowerCase(Locale.ROOT)));
            return traits;
        }

        String describeTarget() {
            if (this.target instanceof Cast cast) {
                return cast.group().replace('_', ' ');
            }
            return "a Bundled Spell orb";
        }
    }
}
