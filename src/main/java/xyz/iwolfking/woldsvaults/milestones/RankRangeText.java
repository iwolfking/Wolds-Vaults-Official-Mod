package xyz.iwolfking.woldsvaults.milestones;

import java.util.function.IntFunction;

/** Writes greed rank ranges for player-facing text: "Looter 1+", "Looter 1-2", "Looter 3 - Hunter 1", "Hunter 2". */
public final class RankRangeText {
    /** Marks a range with no upper rank. */
    public static final int OPEN = -1;

    private RankRangeText() {
    }

    /**
     * The range from {@code from} to {@code to}, both 1-based ranks named by {@code nameOf}; {@link #OPEN} as
     * {@code to} gives "Rank+", and two ranks of one band share the band name.
     */
    public static String of(int from, int to, IntFunction<String> nameOf) {
        String start = nameOf.apply(from);
        if (to == OPEN) {
            return start + "+";
        }
        if (to <= from) {
            return start;
        }
        String end = nameOf.apply(to);
        String band = start.substring(0, start.lastIndexOf(' ') + 1);
        if (!band.isEmpty() && end.startsWith(band)) {
            return start + "-" + end.substring(band.length());
        }
        return start + " - " + end;
    }
}
