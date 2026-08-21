package xyz.iwolfking.woldsvaults.gods;

/**
 * The god alignment progression curve: cumulative XP thresholds, god point grants and
 * minor-transfer-slot capacity. Pure arithmetic, no state; safe on both logical sides.
 */
public final class GodLevels {
    public static final int MAX_DEFINED_LEVEL = 10;
    public static final long XP_PER_LEVEL_PAST_MAX = 150000L;
    public static final int ULTIMATE_UNLOCK_LEVEL = 5;

    private static final long[] CUMULATIVE_XP = {
            20000L, 60000L, 120000L, 200000L, 300000L, 400000L, 500000L, 650000L, 800000L, 1000000L
    };

    private static final int POINTS_AT_LEVEL_ONE = 3;
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
     * Total god points granted by reaching {@code level}: +3 per level through 10 (30 at the
     * standard cap), +2 for every level beyond that.
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

    /**
     * The effective god level: the XP-derived level capped by completed cauldron sacrifices. Each
     * sacrifice opens exactly one level gate; once every defined gate is done, XP alone rules.
     * XP keeps accumulating past an unopened gate - only the level readout waits.
     */
    public static int gatedLevel(long xp, int sacrificesCompleted) {
        int xpLevel = levelForXp(xp);
        if (sacrificesCompleted >= xyz.iwolfking.woldsvaults.gods.sacrifice.GodSacrifices.GATE_COUNT) {
            return xpLevel;
        }
        return Math.min(xpLevel, Math.max(sacrificesCompleted, 0));
    }
}
