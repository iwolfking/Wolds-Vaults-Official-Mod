package xyz.iwolfking.woldsvaults.augments.cast;

import xyz.iwolfking.woldsvaults.augments.AugmentAbilities;
import xyz.iwolfking.woldsvaults.augments.AugmentAbility;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A recipe read as a timeline. The trigger ability is always the first cast; every glyph after it is an augment
 * (modifying the closest ability or Bundled Spell before it), a Delay pause, a Bundled Spell whose payload is the
 * rest of the recipe, or another ability id, which is cast natively with its own cooldown. The same parse validates
 * a recipe when it is stored and again before every cast, against the table installed at that moment.
 */
public final class SpellPlan {
    private final List<Step> steps;
    private final List<String> errors;
    private final Map<AugmentType, Integer> tokens;
    private final int casts;

    private SpellPlan(List<Step> steps, List<String> errors, Map<AugmentType, Integer> tokens, int casts) {
        this.steps = steps;
        this.errors = errors;
        this.tokens = tokens;
        this.casts = casts;
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

    /** How many augment glyphs of each kind the recipe places, for the item's token budget. */
    public Map<AugmentType, Integer> tokens() {
        return this.tokens;
    }

    /** The most ability casts one trigger can produce: Shotgun copies, Echo and every Bundled Spell landing counted. */
    public int casts() {
        return this.casts;
    }

    public static SpellPlan parse(FocusAugmentsConfig config, String triggerId, List<String> glyphs) {
        Parser parser = new Parser(config);
        AugmentAbility trigger = AugmentAbilities.get(config, triggerId);
        if (trigger == null || !trigger.isCastable()) {
            parser.errors.add(triggerId + " can't trigger a recipe");
        } else if (config.isDisabled(triggerId)) {
            parser.errors.add(triggerId + " is disabled for augments");
        } else {
            parser.acceptAbility(trigger);
        }
        if (glyphs.size() > config.limits.maxRecipeLength) {
            parser.errors.add("a recipe holds at most " + config.limits.maxRecipeLength + " glyphs");
        }
        for (String glyph : glyphs) {
            parser.accept(glyph);
        }
        int casts = countCasts(parser.root, config, 1);
        if (casts > config.limits.maxCastsPerTrigger) {
            parser.errors.add("the recipe makes " + casts + " casts; at most " + config.limits.maxCastsPerTrigger + " are allowed");
        }
        return new SpellPlan(Collections.unmodifiableList(parser.root), Collections.unmodifiableList(parser.errors),
                Collections.unmodifiableMap(parser.tokens), casts);
    }

    private static int countCasts(List<Step> steps, FocusAugmentsConfig config, long multiplier) {
        long cap = Integer.MAX_VALUE / 2;
        long total = 0;
        for (Step step : steps) {
            if (step instanceof Cast cast) {
                long copies = Math.min(cap, multiplier << Math.min(cast.count(AugmentType.SHOTGUN), 20));
                total += cast.count(AugmentType.ECHO) > 0 ? copies * 2 : copies;
            } else if (step instanceof Bundle bundle) {
                long landings = Math.min(cap, multiplier << Math.min(bundle.count(AugmentType.SHOTGUN), 20));
                total += countCasts(bundle.payload(), config, landings);
            }
            if (total > cap) {
                return (int) cap;
            }
        }
        return (int) total;
    }

    private static final class Parser {
        final FocusAugmentsConfig config;
        final List<Step> root = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        final Map<AugmentType, Integer> tokens = new EnumMap<>(AugmentType.class);
        List<Step> sink = root;
        Step target;
        int delays;
        int echoes;
        int depth;

        Parser(FocusAugmentsConfig config) {
            this.config = config;
        }

        void accept(String glyph) {
            AugmentType type = AugmentType.byId(glyph);
            if (type != null) {
                this.tokens.merge(type, 1, Integer::sum);
                acceptAugment(type);
                return;
            }
            AugmentAbility ability = AugmentAbilities.get(this.config, glyph);
            if (ability == null) {
                this.errors.add(glyph + " is not an augment or a known ability");
            } else if (!ability.isCastable()) {
                this.errors.add(glyph + " is a hold ability; recipes can't cast it");
            } else if (this.config.isDisabled(glyph)) {
                this.errors.add(glyph + " is disabled for augments");
            } else {
                acceptAbility(ability);
            }
        }

        void acceptAbility(AugmentAbility ability) {
            Cast cast = new Cast(ability, new EnumMap<>(AugmentType.class));
            this.sink.add(cast);
            this.target = cast;
        }

        void acceptAugment(AugmentType type) {
            if (type == AugmentType.BUNDLED_SPELL) {
                if (++this.depth > this.config.limits.maxBundleDepth) {
                    this.errors.add("Bundled Spells nest at most " + this.config.limits.maxBundleDepth + " deep");
                    return;
                }
                Bundle bundle = new Bundle(new EnumMap<>(AugmentType.class), new ArrayList<>());
                this.sink.add(bundle);
                this.sink = bundle.payload();
                this.target = bundle;
                return;
            }
            if (this.target == null) {
                this.errors.add(type.displayName() + " has nothing before it to modify");
                return;
            }
            if (type == AugmentType.DELAY) {
                if (++this.delays > Math.min(this.config.limits.maxDelaysPerRecipe, this.config.maxPerCast(type))) {
                    this.errors.add("too many Delay glyphs");
                } else {
                    addPause(Math.max(1, Math.round(this.config.strength(type) * 20)));
                }
                return;
            }
            Map<AugmentType, Integer> augments;
            if (this.target instanceof Cast cast) {
                if (!type.canModify(cast.ability())) {
                    this.errors.add(type.displayName() + " can't modify " + AugmentAbilities.displayName(cast.ability().id()));
                    return;
                }
                augments = cast.augments();
            } else {
                if (!type.canModifyBundle()) {
                    this.errors.add(type.displayName() + " can't modify a Bundled Spell");
                    return;
                }
                augments = ((Bundle) this.target).augments();
            }
            int count = augments.merge(type, 1, Integer::sum);
            boolean tooMany = type == AugmentType.ECHO
                    ? ++this.echoes > Math.min(this.config.limits.maxEchoesPerRecipe, this.config.maxPerCast(type))
                    : count > this.config.maxPerCast(type);
            if (tooMany) {
                this.errors.add("too many " + type.displayName() + " glyphs");
            } else if (augments.containsKey(AugmentType.ORBIT) && augments.containsKey(AugmentType.HOMING)) {
                this.errors.add("Orbit and Homing can't modify the same thing");
            }
        }

        void addPause(int ticks) {
            if (!this.sink.isEmpty() && this.sink.get(this.sink.size() - 1) instanceof Pause pause) {
                this.sink.set(this.sink.size() - 1, new Pause(pause.ticks() + ticks));
            } else {
                this.sink.add(new Pause(ticks));
            }
        }
    }

    /** Whether a payload casts anything. */
    public static boolean containsCast(List<Step> steps) {
        for (Step step : steps) {
            if (step instanceof Cast || step instanceof Bundle bundle && containsCast(bundle.payload())) {
                return true;
            }
        }
        return false;
    }

    public sealed interface Step permits Cast, Pause, Bundle {
    }

    public record Cast(AugmentAbility ability, Map<AugmentType, Integer> augments) implements Step {
        public int count(AugmentType type) {
            return this.augments.getOrDefault(type, 0);
        }
    }

    public record Pause(int ticks) implements Step {
    }

    public record Bundle(Map<AugmentType, Integer> augments, List<Step> payload) implements Step {
        public int count(AugmentType type) {
            return this.augments.getOrDefault(type, 0);
        }
    }
}
