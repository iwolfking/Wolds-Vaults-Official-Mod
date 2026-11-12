package xyz.iwolfking.woldsvaults.datagen.gods;

import com.google.gson.JsonElement;
import iskallia.vault.util.calc.AttributeLimitHelper;
import xyz.iwolfking.vhapi.api.config.AttributeCapConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodNodeEffectsConfig;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import xyz.iwolfking.vhapi.init.ModConfigs;

/**
 * Datagen-only reader over one god's {@link GodNodeEffectDefaults} table plus the formatters the god node
 * descriptions use, so every number a description quotes is read from the table it describes. A missing
 * effect, index or parameter refuses the run.
 */
public final class GodNodeText {
    private static final double TICKS_PER_SECOND = 20.0D;
    private static final double TICKS_PER_MINUTE = 1200.0D;
    private static final String[] NUMBER_WORDS = {"zero", "one", "two", "three", "four", "five", "six", "seven",
            "eight", "nine", "ten", "eleven", "twelve"};

    private final String god;
    private final GodNodeEffectsConfig.EffectMap effects;

    private GodNodeText(String god, GodNodeEffectsConfig.EffectMap effects) {
        this.god = god;
        this.effects = effects;
    }

    /** The text reader for one god, by lower-case name; a god with no table refuses the run. */
    public static GodNodeText of(String god) {
        GodNodeEffectsConfig.EffectMap effects = GodNodeEffectDefaults.effects(god);
        if (effects.isEmpty()) {
            throw new IllegalStateException("No god node effect table for god '" + god + "'");
        }
        return new GodNodeText(god, effects);
    }

    /** The first entry of an effect's per-point table. */
    public double value(String id) {
        return this.value(id, 0);
    }

    public double value(String id, int index) {
        float[] values = this.entry(id).values();
        if (values == null || index < 0 || index >= values.length) {
            throw new IllegalStateException("God effect '" + id + "' of " + this.god + " has no value at index " + index);
        }
        return values[index];
    }

    /** The first entry an effect pays its second attribute: its {@code secondValues} table when it has one, else {@code values}. */
    public double secondValue(String id) {
        JsonElement table = this.entry(id).json().get("secondValues");
        if (table == null) {
            return this.value(id);
        }
        if (!table.isJsonArray() || table.getAsJsonArray().size() == 0) {
            throw new IllegalStateException("God effect '" + id + "' of " + this.god + " has a secondValues entry that is not a table");
        }
        return table.getAsJsonArray().get(0).getAsDouble();
    }

    /** A numeric handler parameter of an effect. */
    public double param(String id, String key) {
        JsonElement element = this.entry(id).json().get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalStateException("God effect '" + id + "' of " + this.god + " has no numeric parameter '" + key + "'");
        }
        return element.getAsDouble();
    }

    /** Two parameters a description quotes as one number; refuses the run when they differ. */
    public double same(String id, String key, String otherKey) {
        double value = this.param(id, key);
        double other = this.param(id, otherKey);
        if (value != other) {
            throw new IllegalStateException("God effect '" + id + "' has " + key + " " + value + " and " + otherKey
                    + " " + other + ", but its description quotes them as one number");
        }
        return value;
    }

    private GodNodeEffectsConfig.Entry entry(String id) {
        GodNodeEffectsConfig.Entry entry = this.effects.get(id);
        if (entry == null) {
            throw new IllegalStateException("No god effect '" + id + "' in the " + this.god + " table");
        }
        return entry;
    }

    /**
     * The lucky hit chance cap Overcrit measures against, read the way the game reads it: the attribute limit
     * (VHAPI's attribute cap overrides when enabled) clamped to 1. Datagen never runs VHAPI's config load, so the
     * overrides are read here from the seeded configs first, as VHAPI reads them at game load.
     */
    public static double luckyHitCap() {
        ModConfigs.ATTRIBUTE_CAP_CONFIG = new AttributeCapConfig().readConfig();
        return Math.min(AttributeLimitHelper.getLuckyHitChanceLimit(null), 1.0F);
    }

    /** Up to two decimals, trailing zeros dropped: 1.250 reads {@code 1.25}, 2.0 reads {@code 2}. */
    public String num(double value) {
        return GodNodePreviews.number(value);
    }

    /** Up to {@code decimals} decimals, trailing zeros dropped. */
    public String num(double value, int decimals) {
        String text = this.fixed(value, decimals);
        if (text.indexOf('.') >= 0) {
            text = text.replaceAll("0+$", "");
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text.equals("-0") ? "0" : text;
    }

    /** Exactly {@code decimals} decimals: 1.1 reads {@code 1.10} at two. */
    public String fixed(double value, int decimals) {
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).toPlainString();
    }

    /** {@code digits} significant digits, trailing zeros dropped: 16.81 reads {@code 17} at two. */
    public String sig(double value, int digits) {
        BigDecimal rounded = new BigDecimal(value).round(new MathContext(digits, RoundingMode.HALF_UP));
        String text = rounded.stripTrailingZeros().toPlainString();
        return text.equals("-0") ? "0" : text;
    }

    /** A whole number with thousands separators: 15000 reads {@code 15,000}. */
    public String grouped(double value) {
        return String.format(Locale.ROOT, "%,d", Math.round(value));
    }

    /** A fraction as a percent: 0.125 reads {@code 12.5%}. */
    public String percent(double fraction) {
        return this.num(fraction * 100.0D) + "%";
    }

    /** A fraction as a percent with {@code decimals} decimals at most. */
    public String percent(double fraction, int decimals) {
        return this.num(fraction * 100.0D, decimals) + "%";
    }

    /** A fraction as a signed percent: 0.05 reads {@code +5%}, -0.75 reads {@code -75%}. */
    public String signedPercent(double fraction) {
        return (fraction < 0.0D ? "-" : "+") + this.percent(Math.abs(fraction));
    }

    public String signedPercent(double fraction, int decimals) {
        return (fraction < 0.0D ? "-" : "+") + this.percent(Math.abs(fraction), decimals);
    }

    /** A signed flat amount: 10 reads {@code +10}. */
    public String signedFlat(double value) {
        return (value < 0.0D ? "-" : "+") + this.num(Math.abs(value));
    }

    /** A multiplier written after the number: 1.5 reads {@code 1.5x}. */
    public String times(double multiplier) {
        return this.num(multiplier) + "x";
    }

    /** A multiplier written before the number: 1.05 reads {@code x1.05}. */
    public String x(double multiplier) {
        return "x" + this.num(multiplier);
    }

    public String seconds(double ticks) {
        return this.num(ticks / TICKS_PER_SECOND);
    }

    public String minutes(double ticks) {
        return this.num(ticks / TICKS_PER_MINUTE);
    }

    public double perSecond(double perTick) {
        return perTick * TICKS_PER_SECOND;
    }

    public double ticksPerMinute() {
        return TICKS_PER_MINUTE;
    }

    /** A whole-number multiplier in words: 2 reads {@code double}, 10 reads {@code ten times}. */
    public String timesWord(double multiplier) {
        if (isWhole(multiplier, 2)) {
            return "double";
        }
        if (isWhole(multiplier, 3)) {
            return "triple";
        }
        long whole = Math.round(multiplier);
        if (Math.abs(multiplier - whole) < 1.0E-6D && whole >= 4 && whole < NUMBER_WORDS.length) {
            return NUMBER_WORDS[(int) whole] + " times";
        }
        return this.num(multiplier) + " times";
    }

    /** A verb multiplying {@code object}: 2 reads {@code Doubles <object>}. */
    public String multipliesVerb(double multiplier, String object) {
        if (isWhole(multiplier, 2)) {
            return "Doubles " + object;
        }
        if (isWhole(multiplier, 3)) {
            return "Triples " + object;
        }
        return "Multiplies " + object + " by " + this.num(multiplier);
    }

    /** A past participle for a multiplier: 0.5 reads {@code halved}. */
    public String scaledWord(double multiplier) {
        if (isWhole(multiplier * 2.0D, 1)) {
            return "halved";
        }
        if (isWhole(multiplier, 2)) {
            return "doubled";
        }
        return "multiplied by " + this.num(multiplier);
    }

    /** A fraction in words: 0.5 reads {@code half}, 1/3 reads {@code a third}. */
    public String fractionWord(double fraction) {
        if (isWhole(fraction * 2.0D, 1)) {
            return "half";
        }
        if (isWhole(fraction * 3.0D, 1)) {
            return "a third";
        }
        if (isWhole(fraction * 4.0D, 1)) {
            return "a quarter";
        }
        return this.times(fraction);
    }

    /** A fill level: 0.5 reads {@code half}, anything else as a percent. */
    public String fillWord(double fraction) {
        return isWhole(fraction * 2.0D, 1) ? "half" : this.percent(fraction);
    }

    public String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static boolean isWhole(double value, long whole) {
        return Math.abs(value - whole) < 1.0E-6D;
    }
}
