package xyz.iwolfking.woldsvaults.client.spellcraft.gui;

import xyz.iwolfking.woldsvaults.client.spellcraft.stats.SlotCostStats;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.StepStats;

import java.util.Locale;

public final class StatText {
    private StatText() {
    }

    /** A number in at most four characters plus a suffix such as k or M. */
    public static String compact(double value) {
        String[] units = {"", "k", "M", "B", "T"};
        double scaled = value;
        int unit = 0;
        while (unit < units.length - 1 && Math.abs(round(scaled)) >= 1000.0) {
            scaled /= 1000.0;
            unit++;
        }
        return trimmed(round(scaled)) + units[unit];
    }

    private static double round(double value) {
        double magnitude = Math.abs(value);
        return magnitude < 100.0 ? Math.round(value * 10.0) / 10.0 : Math.round(value);
    }

    private static String trimmed(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    public static String hit(StepStats.Hit hit) {
        return hit.isRange() ? SpellcraftNames.tr("fmt.range", compact(hit.min()), compact(hit.max())) : compact(hit.max());
    }

    public static String trim(float value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        text = text.replaceAll("0+$", "");
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }

    public static String seconds(float ticks) {
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0F);
    }

    public static String numbers(String key, SlotCostStats.Numbers stats) {
        String cooldown = SpellcraftNames.tr("fmt.seconds", seconds(stats.cooldownAfter()));
        return stats.hasMana() ? SpellcraftNames.tr(key, Math.round(stats.manaAfter()), cooldown) : cooldown;
    }
}
