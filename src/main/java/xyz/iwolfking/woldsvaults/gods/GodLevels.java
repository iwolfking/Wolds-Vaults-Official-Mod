package xyz.iwolfking.woldsvaults.gods;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.gods.GodLevelsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The god alignment progression curve: cumulative XP thresholds, god point grants and
 * minor-transfer-slot capacity. Pure arithmetic, no state; safe on both logical sides.
 *
 * <p>Every number comes from {@code config/the_vault/gods/god_levels.json} and is read per call
 * rather than held in a constant, because this class is reachable from code that loads before
 * the config pass runs.
 */
public final class GodLevels {
    private static final GodLevelsConfig FALLBACK = GodLevelsConfig.defaults();
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    private GodLevels() {
    }

    /**
     * Total accumulated XP required to be at {@code level}. Level 0 costs nothing; levels past
     * the last defined level each cost a further {@code xpPerLevelPastMax}.
     */
    public static long xpForLevel(int level) {
        if (level <= 0) {
            return 0L;
        }
        GodLevelsConfig config = config();
        long[] cumulative = config.getCumulativeXp();
        int max = config.getMaxDefinedLevel();
        if (level <= max) {
            return cumulative[level - 1];
        }
        return cumulative[max - 1] + (long) (level - max) * config.getXpPerLevelPastMax();
    }

    /**
     * The highest level reachable with {@code xp} accumulated. Unbounded above by design — the
     * curve is linear past the last defined level.
     */
    public static int levelForXp(long xp) {
        GodLevelsConfig config = config();
        long[] cumulative = config.getCumulativeXp();
        int max = config.getMaxDefinedLevel();
        if (xp < cumulative[0]) {
            return 0;
        }
        int level = 0;
        while (level < max && xp >= cumulative[level]) {
            level++;
        }
        if (level < max) {
            return level;
        }
        long past = xp - cumulative[max - 1];
        return max + (int) (past / config.getXpPerLevelPastMax());
    }

    /**
     * Total god points granted by reaching {@code level}: +3 per level through 10 (30 at the
     * standard cap), +2 for every level beyond that.
     */
    public static int totalPointsForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        GodLevelsConfig config = config();
        int max = config.getMaxDefinedLevel();
        int points = config.getPointsAtLevelOne();
        points += config.getPointsPerLevelToMax() * Math.min(level - 1, max - 1);
        if (level > max) {
            points += config.getPointsPerLevelPastMax() * (level - max);
        }
        return points;
    }

    /**
     * Minor-transfer slots unlocked by {@code level}: none by default, then one each at god
     * levels 2, 4 and 7.
     */
    public static int minorTransferSlots(int level) {
        int slots = 0;
        for (int unlock : config().getMinorTransferSlotLevels()) {
            if (level >= unlock) {
                slots++;
            }
        }
        return slots;
    }

    public static boolean hasUltimate(int level) {
        return level >= config().getUltimateUnlockLevel();
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

    private static GodLevelsConfig config() {
        GodLevelsConfig config = ModConfigs.GOD_LEVELS;
        if (config != null) {
            return config;
        }
        if (WARNED.compareAndSet(false, true)) {
            WoldsVaults.LOGGER.error("God level curve was read before config/the_vault/gods/god_levels.json "
                    + "was loaded; falling back to the shipped curve");
        }
        return FALLBACK;
    }
}
