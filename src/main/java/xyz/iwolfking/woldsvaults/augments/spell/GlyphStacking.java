package xyz.iwolfking.woldsvaults.augments.spell;

import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * How glyph effects on one stat combine (redesign/spellcraft/GLYPH_STACKING_2026-11-10.md). The upsides of common
 * glyphs add into one pool per stat; downsides and costs multiply once per copy; special and unique glyphs multiply.
 * A stat is {@code (1 + pool) x perCopy x special}. On a stat where lower is better (mana, cooldown, drain, heal mana)
 * an upside is a rate instead: each one adds {@code 1 / factor - 1} to the pool and the stat is
 * {@code perCopy x special / (1 + pool)}, so one copy keeps its written factor and no pool of reductions reaches zero.
 * Spell mana is further divided by the spell's efficiency total.
 */
public final class GlyphStacking {
    public static final List<String> STEP_STATS = List.of("damage", "area", "speed", "scale", "intensity", "duration", "drain", "heal_mana");
    public static final List<String> SPELL_STATS = List.of("mana", "cooldown", "haste", "efficiency");
    public static final List<String> CLASSES = List.of("common", "special", "unique", "utility");
    public static final List<String> ROLES = List.of("upside", "downside", "cost");
    private static final Set<String> LOWER_IS_BETTER = Set.of("mana", "cooldown", "drain", "heal_mana");

    private GlyphStacking() {
    }

    public static boolean lowerIsBetter(String stat) {
        return LOWER_IS_BETTER.contains(stat);
    }

    /** Whether a factor moves the stat in the player's favour. */
    public static boolean favourable(String stat, float factor) {
        return lowerIsBetter(stat) ? factor < 1.0F : factor > 1.0F;
    }

    /** Whether this effect of a glyph of this class goes into the additive pool rather than multiplying. */
    public static boolean pooled(FocusAugmentsConfig.Glyph glyph, FocusAugmentsConfig.Effect effect) {
        return "common".equals(glyph.glyphClass) && "upside".equals(effect.role);
    }

    /** One stat's parts: the summed pool of common upsides, the per-copy product, and the special product. */
    public static final class Stat {
        private final String name;
        private float pool;
        private float perCopy = 1.0F;
        private float special = 1.0F;

        private Stat(String name) {
            this.name = name;
        }

        public String name() {
            return this.name;
        }

        public float pool() {
            return this.pool;
        }

        public float perCopy() {
            return this.perCopy;
        }

        public float special() {
            return this.special;
        }

        public float total() {
            if (lowerIsBetter(this.name)) {
                return this.perCopy * this.special / (1.0F + this.pool);
            }
            return (1.0F + this.pool) * this.perCopy * this.special;
        }
    }

    /** The pooled stats of one step (its step effects) or one spell (its spell effects). */
    public static final class Pool {
        private final Map<String, Stat> stats = new LinkedHashMap<>();

        /** Adds every effect of one placed glyph copy at its rolled potency. */
        public void add(FocusAugmentsConfig.Glyph glyph, List<FocusAugmentsConfig.Effect> effects, float potency) {
            for (FocusAugmentsConfig.Effect effect : effects) {
                float factor = effect.factor(potency);
                Stat stat = this.stats.computeIfAbsent(effect.stat, Stat::new);
                if (pooled(glyph, effect)) {
                    stat.pool += lowerIsBetter(effect.stat) ? 1.0F / factor - 1.0F : factor - 1.0F;
                } else if ("special".equals(glyph.glyphClass) || "unique".equals(glyph.glyphClass)) {
                    stat.special *= factor;
                } else {
                    stat.perCopy *= factor;
                }
            }
        }

        public float total(String stat) {
            Stat entry = this.stats.get(stat);
            return entry == null ? 1.0F : entry.total();
        }

        @Nullable
        public Stat stat(String stat) {
            return this.stats.get(stat);
        }

        /** Every stat a glyph touched, in the order they were first touched. */
        public Map<String, Stat> stats() {
            return Collections.unmodifiableMap(this.stats);
        }

        /** Spell mana: the mana stat divided by the efficiency total. */
        public float mana() {
            return total("mana") / total("efficiency");
        }
    }
}
