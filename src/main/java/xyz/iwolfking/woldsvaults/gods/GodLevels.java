package xyz.iwolfking.woldsvaults.gods;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.gods.GodLevelsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The god alignment progression curve: XP thresholds, god point grants (from levels and from raw
 * reputation), the charting threshold and minor-transfer-slot capacity.
 * Pure arithmetic; every number is read per call from the generated {@code gods/levels/god_levels} table. Until
 * that table is installed every level is 0 and nothing is granted.
 */
public final class GodLevels {
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    private GodLevels() {
    }

    /** Total accumulated XP to be at {@code level}; each level past the last defined one costs the same. */
    public static long xpForLevel(int level) {
        if (level <= 0) {
            return 0L;
        }
        GodLevelsConfig config = config();
        if (config == null) {
            return Long.MAX_VALUE;
        }
        long[] cumulative = config.cumulativeXp;
        int max = config.maxDefinedLevel;
        if (level <= max) {
            return cumulative[level - 1];
        }
        return cumulative[max - 1] + (long) (level - max) * config.xpPerLevelPastMax;
    }

    /** The highest level reachable with {@code xp} accumulated; unbounded above. */
    public static int levelForXp(long xp) {
        GodLevelsConfig config = config();
        if (config == null) {
            return 0;
        }
        long[] cumulative = config.cumulativeXp;
        int max = config.maxDefinedLevel;
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
        return max + (int) (past / config.xpPerLevelPastMax);
    }

    /** Total god points granted by reaching {@code level}, at the per-level rates the table sets. */
    public static int totalPointsForLevel(int level) {
        GodLevelsConfig config = config();
        if (level <= 0 || config == null) {
            return 0;
        }
        int max = config.maxDefinedLevel;
        int points = config.pointsAtLevelOne;
        points += config.pointsPerLevelToMax * Math.min(level - 1, max - 1);
        if (level > max) {
            points += config.pointsPerLevelPastMax * (level - max);
        }
        return points;
    }

    /** Minor-transfer slots unlocked by {@code level}: one per configured unlock level at or below it. */
    public static int minorTransferSlots(int level) {
        int slots = 0;
        for (int unlock : minorTransferSlotLevels()) {
            if (level >= unlock) {
                slots++;
            }
        }
        return slots;
    }

    public static int maxMinorTransferSlots() {
        return minorTransferSlotLevels().length;
    }

    /** The god level that opens minor-transfer slot {@code slot} (zero-based), or -1 if never granted. */
    public static int minorTransferSlotUnlockLevel(int slot) {
        int[] levels = minorTransferSlotLevels();
        return slot >= 0 && slot < levels.length ? levels[slot] : -1;
    }

    public static boolean hasUltimate(int level) {
        return level >= ultimateUnlockLevel();
    }

    public static int ultimateUnlockLevel() {
        GodLevelsConfig config = config();
        return config == null ? Integer.MAX_VALUE : config.ultimateUnlockLevel;
    }

    /**
     * The XP-derived level capped by completed cauldron sacrifices. Gate 0 is the Initiation and opens no
     * level; gate N opens level N, so standing at level N takes N+1 completed gates. XP still accumulates
     * past a closed gate.
     */
    public static int gatedLevel(long xp, int sacrificesCompleted) {
        int xpLevel = levelForXp(xp);
        if (sacrificesCompleted >= xyz.iwolfking.woldsvaults.gods.sacrifice.GodSacrifices.GATE_COUNT) {
            return xpLevel;
        }
        return Math.min(xpLevel, Math.max(sacrificesCompleted - 1, 0));
    }

    /** The completed-gate count needed to stand at {@code level}: none at 0, else the Initiation plus one per level. */
    public static int sacrificesForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        return Math.min(level + 1, xyz.iwolfking.woldsvaults.gods.sacrifice.GodSacrifices.GATE_COUNT);
    }

    /** The reputation with a god at which their constellation is charted and its first god point granted. */
    public static int chartingReputation() {
        GodLevelsConfig config = config();
        return config == null ? Integer.MAX_VALUE : config.chartingReputation;
    }

    public static int reputationPerGodPoint() {
        GodLevelsConfig config = config();
        return config == null ? Integer.MAX_VALUE : config.reputationPerGodPoint;
    }

    /** God points granted by raw reputation alone, one per {@link #reputationPerGodPoint()}; uncapped. */
    public static int reputationPoints(int reputation) {
        if (reputation <= 0) {
            return 0;
        }
        return reputation / reputationPerGodPoint();
    }

    /** The experience one god altar pays before the repeat multiplier and any prestige scaling. */
    public static double baseAltarXp() {
        GodLevelsConfig config = config();
        return config == null ? 0.0D : config.baseAltarXp;
    }

    private static int[] minorTransferSlotLevels() {
        GodLevelsConfig config = config();
        return config == null ? new int[0] : config.minorTransferSlotLevels;
    }

    @Nullable
    private static GodLevelsConfig config() {
        GodLevelsConfig config = ModConfigs.GOD_LEVELS;
        if (config == null && WARNED.compareAndSet(false, true)) {
            WoldsVaults.LOGGER.error("God levels were read before the gods/levels/god_levels table was installed; "
                    + "every god level reads as 0 until it is");
        }
        return config;
    }
}
