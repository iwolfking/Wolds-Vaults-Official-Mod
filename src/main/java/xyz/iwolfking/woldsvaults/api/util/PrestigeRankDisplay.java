package xyz.iwolfking.woldsvaults.api.util;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedTheme;

/** Turns the prestige tree's {@code requiredGreedTier}, a 1..16 rank index, into greed rank wording. */
public final class PrestigeRankDisplay {
    public static final String LEGACY_WIDGET_PREFIX = "Requires Greed Tier ";
    public static final String LEGACY_DIALOG_LABEL = "\nRequired Greed Tier: ";
    public static final String DIALOG_LABEL = "\n";

    private PrestigeRankDisplay() {
    }

    public static String rankName(int rank) {
        return GreedTheme.rankName(rank).getString();
    }

    /** Rewrites a node tooltip's requirement line to the bare rank name; any other line is returned untouched. */
    public static String rewriteWidgetRequirement(String line) {
        if (line == null || !line.startsWith(LEGACY_WIDGET_PREFIX)) {
            return line;
        }
        String tail = line.substring(LEGACY_WIDGET_PREFIX.length()).trim();
        try {
            return rankName(Integer.parseInt(tail));
        } catch (NumberFormatException e) {
            WoldsVaults.LOGGER.error("Prestige tooltip requirement '{}' did not end in a rank index; leaving it as-is.", line);
            return line;
        }
    }

    public static String rewriteDialogRequirement(String value) {
        try {
            return rankName(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            WoldsVaults.LOGGER.error("Prestige dialog requirement value '{}' was not a rank index; leaving it as-is.", value);
            return value;
        }
    }
}
