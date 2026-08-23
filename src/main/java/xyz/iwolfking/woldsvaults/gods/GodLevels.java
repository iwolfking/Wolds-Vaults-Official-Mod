package xyz.iwolfking.woldsvaults.gods;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.gods.GodLevelsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The god alignment progression curve: XP thresholds, god point grants and minor-transfer-slot capacity.
 * Pure arithmetic; every number is read per call from {@code config/the_vault/gods/god_levels.json}.
 */
public final class GodLevels {
    private static final GodLevelsConfig FALLBACK = GodLevelsConfig.defaults();
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    private GodLevels() {
    }

    /** Total accumulated XP to be at {@code level}; each level past the last defined one costs the same. */
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

    /** The highest level reachable with {@code xp} accumulated; unbounded above. */
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

    /** Total god points granted by reaching {@code level}, at the per-level rates {@code god_levels.json} sets. */
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

    /** Minor-transfer slots unlocked by {@code level}: one per configured unlock level at or below it. */
    public static int minorTransferSlots(int level) {
        int slots = 0;
        for (int unlock : config().getMinorTransferSlotLevels()) {
            if (level >= unlock) {
                slots++;
            }
        }
        return slots;
    }

    public static int maxMinorTransferSlots() {
        return config().getMinorTransferSlotLevels().length;
    }

    /** The god level that opens minor-transfer slot {@code slot} (zero-based), or -1 if never granted. */
    public static int minorTransferSlotUnlockLevel(int slot) {
        int[] levels = config().getMinorTransferSlotLevels();
        return slot >= 0 && slot < levels.length ? levels[slot] : -1;
    }

    public static boolean hasUltimate(int level) {
        return level >= ultimateUnlockLevel();
    }

    public static int ultimateUnlockLevel() {
        return config().getUltimateUnlockLevel();
    }

    /** The XP-derived level capped by completed cauldron sacrifices, one gate each; XP still accumulates. */
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
