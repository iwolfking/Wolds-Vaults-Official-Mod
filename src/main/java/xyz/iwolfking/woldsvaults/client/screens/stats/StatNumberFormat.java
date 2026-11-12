package xyz.iwolfking.woldsvaults.client.screens.stats;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compact stat numbers for the H screen: a shown value below 1000 is left alone, larger ones keep three significant
 * digits with a K, M, B or T suffix and no trailing zeros ({@code 1061234} becomes {@code 1.06M},
 * {@code 1450%} becomes {@code 1.45K%}). Any text around the number is kept.
 */
public final class StatNumberFormat {
    private static final Pattern NUMBER = Pattern.compile("^(\\D*?)(-?\\d[\\d,]*(?:\\.\\d+)?)(.*)$", Pattern.DOTALL);
    private static final String[] UNITS = {"K", "M", "B", "T"};

    private StatNumberFormat() {
    }

    public static String compact(String shown) {
        Matcher matcher = NUMBER.matcher(shown);
        if (!matcher.matches()) {
            return shown;
        }
        double value;
        try {
            value = Double.parseDouble(matcher.group(2).replace(",", ""));
        } catch (NumberFormatException e) {
            return shown;
        }
        if (Math.abs(value) < 1000.0D || Double.isNaN(value) || Double.isInfinite(value)) {
            return shown;
        }
        int unit = -1;
        while (Math.abs(value) >= 1000.0D && unit < UNITS.length - 1) {
            value /= 1000.0D;
            unit++;
        }
        String digits = threeDigits(value);
        if (Math.abs(Double.parseDouble(digits)) >= 1000.0D && unit < UNITS.length - 1) {
            value /= 1000.0D;
            unit++;
            digits = threeDigits(value);
        }
        if (digits.contains(".")) {
            digits = digits.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return matcher.group(1) + digits + UNITS[unit] + matcher.group(3);
    }

    private static String threeDigits(double value) {
        double magnitude = Math.abs(value);
        return String.format(Locale.ROOT, magnitude < 10.0D ? "%.2f" : magnitude < 100.0D ? "%.1f" : "%.0f", value);
    }

    /** The shown value with thousands separators, for the exact-value tooltip line. */
    public static String grouped(String shown) {
        Matcher matcher = NUMBER.matcher(shown);
        if (!matcher.matches() || matcher.group(2).contains(",")) {
            return shown;
        }
        String number = matcher.group(2);
        boolean negative = number.startsWith("-");
        String unsigned = negative ? number.substring(1) : number;
        int dot = unsigned.indexOf('.');
        String whole = dot < 0 ? unsigned : unsigned.substring(0, dot);
        String fraction = dot < 0 ? "" : unsigned.substring(dot);
        StringBuilder out = new StringBuilder(whole);
        for (int i = whole.length() - 3; i > 0; i -= 3) {
            out.insert(i, ',');
        }
        return matcher.group(1) + (negative ? "-" : "") + out + fraction + matcher.group(3);
    }
}
