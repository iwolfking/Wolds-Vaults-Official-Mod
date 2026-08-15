package xyz.iwolfking.woldsvaults.gods;

/**
 * The god alignment progression curve: cumulative XP thresholds, god point grants and
 * minor-transfer-slot capacity. Pure arithmetic, no state; safe on both logical sides.
 */
public final class GodLevels {
    public static final int MAX_DEFINED_LEVEL = 10;
    public static final long XP_PER_LEVEL_PAST_MAX = 50000L;
    public static final int ULTIMATE_UNLOCK_LEVEL = 5;

    private static final long[] CUMULATIVE_XP = {
            20000L, 50000L, 80000L, 120000L, 160000L, 220000L, 280000L, 350000L, 400000L, 475000L
    };

    private static final int POINTS_AT_LEVEL_ONE = 2;
    private static final int POINTS_PER_LEVEL_TO_MAX = 3;
    private static final int POINTS_PER_LEVEL_PAST_MAX = 2;

    private static final int[] MTS_UNLOCK_LEVELS = {2, 4, 7};

    private GodLevels() {
    }

    /**
     * Total accumulated XP required to be at {@code level}. Level 0 costs nothing; levels past
     * {@link #MAX_DEFINED_LEVEL} each cost a further {@link #XP_PER_LEVEL_PAST_MAX}.
     */
    public static long xpForLevel(int level) {
        if (level <= 0) {
            return 0L;
        }
        if (level <= MAX_DEFINED_LEVEL) {
            return CUMULATIVE_XP[level - 1];
        }
        return CUMULATIVE_XP[MAX_DEFINED_LEVEL - 1] + (long) (level - MAX_DEFINED_LEVEL) * XP_PER_LEVEL_PAST_MAX;
    }

    /**
     * The highest level reachable with {@code xp} accumulated. Unbounded above by design — the
     * curve is linear past {@link #MAX_DEFINED_LEVEL}.
     */
    public static int levelForXp(long xp) {
        if (xp < CUMULATIVE_XP[0]) {
            return 0;
        }
        int level = 0;
        while (level < MAX_DEFINED_LEVEL && xp >= CUMULATIVE_XP[level]) {
            level++;
        }
        if (level < MAX_DEFINED_LEVEL) {
            return level;
        }
        long past = xp - CUMULATIVE_XP[MAX_DEFINED_LEVEL - 1];
        return MAX_DEFINED_LEVEL + (int) (past / XP_PER_LEVEL_PAST_MAX);
    }

    /**
     * Total god points granted by reaching {@code level}: +2 at level 1, +3 for each of levels
     * 2 through 10, +2 for every level beyond that.
     */
    public static int totalPointsForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        int points = POINTS_AT_LEVEL_ONE;
        points += POINTS_PER_LEVEL_TO_MAX * Math.min(level - 1, MAX_DEFINED_LEVEL - 1);
        if (level > MAX_DEFINED_LEVEL) {
            points += POINTS_PER_LEVEL_PAST_MAX * (level - MAX_DEFINED_LEVEL);
        }
        return points;
    }

    /**
     * Minor-transfer slots unlocked by {@code level}: none by default, then one each at god
     * levels 2, 4 and 7.
     */
    public static int minorTransferSlots(int level) {
        int slots = 0;
        for (int unlock : MTS_UNLOCK_LEVELS) {
            if (level >= unlock) {
                slots++;
            }
        }
        return slots;
    }

    public static boolean hasUltimate(int level) {
        return level >= ULTIMATE_UNLOCK_LEVEL;
    }
}
