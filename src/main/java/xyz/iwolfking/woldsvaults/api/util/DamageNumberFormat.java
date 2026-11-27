package xyz.iwolfking.woldsvaults.api.util;

import java.util.Locale;

/** Short-scale damage notation: 100620483 becomes {@code 100.62m}. */
public final class DamageNumberFormat {
    private static final String[] SUFFIXES = {"", "k", "m", "b", "t", "qa", "qi", "sx", "sp", "oc", "no", "dc"};

    private DamageNumberFormat() {
    }

    /**
     * Two decimals and a short-scale suffix (k, m, b, t, qa, ...); values below one thousand keep one
     * decimal, values past the last suffix fall back to scientific notation.
     */
    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        double abs = Math.abs(value);
        if (abs < 1000.0) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        int tier = (int) Math.floor(Math.log10(abs) / 3.0);
        if (tier >= SUFFIXES.length) {
            return String.format(Locale.ROOT, "%.2e", value);
        }
        double scaled = value / Math.pow(1000.0, tier);
        if (Math.abs(scaled) >= 999.995 && tier + 1 < SUFFIXES.length) {
            tier++;
            scaled = value / Math.pow(1000.0, tier);
        }
        return String.format(Locale.ROOT, "%.2f%s", scaled, SUFFIXES[tier]);
    }

    public static String percent(double fraction) {
        return String.format(Locale.ROOT, "%.1f%%", 100.0 * fraction);
    }
}
