package xyz.iwolfking.woldsvaults.api.util;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedTheme;

/**
 * Turns the prestige tree's raw greed-tier gate into the rank wording the rest of the greed UI
 * uses. The int stored in {@code requiredGreedTier} is the 1..16 rank index, so the only thing the
 * prestige screens need is the matching rank name; both call sites are string rewrites on
 * the_vault's own text so that the vanilla colouring and layout are left alone.
 */
public final class PrestigeRankDisplay {
    public static final String LEGACY_WIDGET_PREFIX = "Requires Greed Tier ";
    public static final String LEGACY_DIALOG_LABEL = "\nRequired Greed Tier: ";
    public static final String DIALOG_LABEL = "\n";

    private PrestigeRankDisplay() {
    }

    public static String rankName(int rank) {
        return GreedTheme.rankName(rank).getString();
    }

    /**
     * Rewrites the node tooltip's requirement line to the bare rank name ("Hunter 2"). Returns the
     * input untouched for every other tooltip line, so the injection can stay ordinal-free.
     */
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

    /**
     * Rewrites the dialog's per-tier requirement value, which the_vault renders as a bare number.
     */
    public static String rewriteDialogRequirement(String value) {
        try {
            return rankName(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            WoldsVaults.LOGGER.error("Prestige dialog requirement value '{}' was not a rank index; leaving it as-is.", value);
            return value;
        }
    }
}
