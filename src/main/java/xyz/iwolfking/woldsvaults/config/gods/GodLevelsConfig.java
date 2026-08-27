package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import xyz.iwolfking.woldsvaults.config.PackAuthoredConfig;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The god alignment progression curve - {@code config/the_vault/gods/god_levels.json}: cumulative XP
 * thresholds, the linear tail past the last defined level, god points per level and per reputation, the
 * reputation that charts a constellation, the levels that unlock a minor-transfer slot or the ultimate,
 * and the base experience one god altar pays. An absent or unusable key falls back, with an error.
 */
public class GodLevelsConfig extends PackAuthoredConfig {
    private static final long[] DEFAULT_CUMULATIVE_XP = {
            20000L, 60000L, 120000L, 200000L, 300000L, 400000L, 500000L, 650000L, 800000L, 1000000L
    };
    private static final int DEFAULT_MAX_DEFINED_LEVEL = 10;
    private static final long DEFAULT_XP_PER_LEVEL_PAST_MAX = 150000L;
    private static final int DEFAULT_ULTIMATE_UNLOCK_LEVEL = 5;
    private static final int DEFAULT_POINTS_AT_LEVEL_ONE = 3;
    private static final int DEFAULT_POINTS_PER_LEVEL_TO_MAX = 3;
    private static final int DEFAULT_POINTS_PER_LEVEL_PAST_MAX = 2;
    private static final int[] DEFAULT_MINOR_TRANSFER_SLOT_LEVELS = {2, 4, 7};
    private static final int DEFAULT_CHARTING_REPUTATION = 10;
    private static final int DEFAULT_REPUTATION_PER_GOD_POINT = 10;
    private static final double DEFAULT_BASE_ALTAR_XP = 300.0D;

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    @Expose private long[] cumulativeXp;
    @Expose private Integer maxDefinedLevel;
    @Expose private Long xpPerLevelPastMax;
    @Expose private Integer ultimateUnlockLevel;
    @Expose private Integer pointsAtLevelOne;
    @Expose private Integer pointsPerLevelToMax;
    @Expose private Integer pointsPerLevelPastMax;
    @Expose private int[] minorTransferSlotLevels;
    @Expose private Integer chartingReputation;
    @Expose private Integer reputationPerGodPoint;
    @Expose private Double baseAltarXp;

    /** A config carrying only the shipped curve, for readers that run before the config pass. */
    public static GodLevelsConfig defaults() {
        GodLevelsConfig config = new GodLevelsConfig();
        config.reset();
        return config;
    }

    @Override
    public String getName() {
        return "gods" + File.separator + "god_levels";
    }

    @Override
    protected void reset() {
        this.cumulativeXp = Arrays.copyOf(DEFAULT_CUMULATIVE_XP, DEFAULT_CUMULATIVE_XP.length);
        this.maxDefinedLevel = DEFAULT_MAX_DEFINED_LEVEL;
        this.xpPerLevelPastMax = DEFAULT_XP_PER_LEVEL_PAST_MAX;
        this.ultimateUnlockLevel = DEFAULT_ULTIMATE_UNLOCK_LEVEL;
        this.pointsAtLevelOne = DEFAULT_POINTS_AT_LEVEL_ONE;
        this.pointsPerLevelToMax = DEFAULT_POINTS_PER_LEVEL_TO_MAX;
        this.pointsPerLevelPastMax = DEFAULT_POINTS_PER_LEVEL_PAST_MAX;
        this.minorTransferSlotLevels =
                Arrays.copyOf(DEFAULT_MINOR_TRANSFER_SLOT_LEVELS, DEFAULT_MINOR_TRANSFER_SLOT_LEVELS.length);
        this.chartingReputation = DEFAULT_CHARTING_REPUTATION;
        this.reputationPerGodPoint = DEFAULT_REPUTATION_PER_GOD_POINT;
        this.baseAltarXp = DEFAULT_BASE_ALTAR_XP;
    }

    public long[] getCumulativeXp() {
        if (this.cumulativeXp == null || this.cumulativeXp.length == 0) {
            warn("cumulativeXp");
            return DEFAULT_CUMULATIVE_XP;
        }
        return this.cumulativeXp;
    }

    /** The last level the XP table defines, clamped to the length of that table. */
    public int getMaxDefinedLevel() {
        int configured = this.maxDefinedLevel == null ? DEFAULT_MAX_DEFINED_LEVEL : this.maxDefinedLevel;
        if (this.maxDefinedLevel == null) {
            warn("maxDefinedLevel");
        }
        int table = this.getCumulativeXp().length;
        if (configured > table || configured < 1) {
            warn("maxDefinedLevel/cumulativeXp");
            return Math.max(1, table);
        }
        return configured;
    }

    public long getXpPerLevelPastMax() {
        if (this.xpPerLevelPastMax == null || this.xpPerLevelPastMax <= 0L) {
            warn("xpPerLevelPastMax");
            return DEFAULT_XP_PER_LEVEL_PAST_MAX;
        }
        return this.xpPerLevelPastMax;
    }

    public int getUltimateUnlockLevel() {
        if (this.ultimateUnlockLevel == null) {
            warn("ultimateUnlockLevel");
            return DEFAULT_ULTIMATE_UNLOCK_LEVEL;
        }
        return this.ultimateUnlockLevel;
    }

    public int getPointsAtLevelOne() {
        if (this.pointsAtLevelOne == null) {
            warn("pointsAtLevelOne");
            return DEFAULT_POINTS_AT_LEVEL_ONE;
        }
        return this.pointsAtLevelOne;
    }

    public int getPointsPerLevelToMax() {
        if (this.pointsPerLevelToMax == null) {
            warn("pointsPerLevelToMax");
            return DEFAULT_POINTS_PER_LEVEL_TO_MAX;
        }
        return this.pointsPerLevelToMax;
    }

    public int getPointsPerLevelPastMax() {
        if (this.pointsPerLevelPastMax == null) {
            warn("pointsPerLevelPastMax");
            return DEFAULT_POINTS_PER_LEVEL_PAST_MAX;
        }
        return this.pointsPerLevelPastMax;
    }

    public int[] getMinorTransferSlotLevels() {
        if (this.minorTransferSlotLevels == null) {
            warn("minorTransferSlotLevels");
            return DEFAULT_MINOR_TRANSFER_SLOT_LEVELS;
        }
        return this.minorTransferSlotLevels;
    }

    /** The reputation with a god at which their constellation is charted and its first god point granted. */
    public int getChartingReputation() {
        if (this.chartingReputation == null || this.chartingReputation < 0) {
            warn("chartingReputation");
            return DEFAULT_CHARTING_REPUTATION;
        }
        return this.chartingReputation;
    }

    /** How much reputation with a god buys one more god point in their tree; must be positive. */
    public int getReputationPerGodPoint() {
        if (this.reputationPerGodPoint == null || this.reputationPerGodPoint <= 0) {
            warn("reputationPerGodPoint");
            return DEFAULT_REPUTATION_PER_GOD_POINT;
        }
        return this.reputationPerGodPoint;
    }

    /** The experience one god altar pays before the repeat multiplier and any prestige scaling. */
    public double getBaseAltarXp() {
        if (this.baseAltarXp == null || this.baseAltarXp < 0.0D) {
            warn("baseAltarXp");
            return DEFAULT_BASE_ALTAR_XP;
        }
        return this.baseAltarXp;
    }

    private static void warn(String key) {
        if (WARNED.add(key)) {
            WoldsVaults.LOGGER.error("config/the_vault/gods/god_levels.json has no usable '{}'; "
                    + "falling back to the shipped god level curve for it", key);
        }
    }
}
