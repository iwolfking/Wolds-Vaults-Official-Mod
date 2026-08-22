package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import xyz.iwolfking.woldsvaults.config.PackAuthoredConfig;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The god alignment progression curve - {@code config/the_vault/gods/god_levels.json}: the
 * cumulative XP thresholds, the linear tail past the last defined level, the god points each
 * level grants, and the levels that unlock a minor-transfer slot or the god's ultimate.
 *
 * <p>Every scalar is boxed so that an absent key is distinguishable from a configured zero -
 * several of these are legitimately zero - and every reader falls back to the shipped value with
 * an error naming the key rather than silently flattening the curve. Because every field already
 * degrades that way there is nothing left for {@link #isValid()} to reject; what this config wants
 * from {@link PackAuthoredConfig} is the guarantee that a hand-edit which fails to parse is kept
 * rather than overwritten.
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

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    @Expose private long[] cumulativeXp;
    @Expose private Integer maxDefinedLevel;
    @Expose private Long xpPerLevelPastMax;
    @Expose private Integer ultimateUnlockLevel;
    @Expose private Integer pointsAtLevelOne;
    @Expose private Integer pointsPerLevelToMax;
    @Expose private Integer pointsPerLevelPastMax;
    @Expose private int[] minorTransferSlotLevels;

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
    }

    public long[] getCumulativeXp() {
        if (this.cumulativeXp == null || this.cumulativeXp.length == 0) {
            warn("cumulativeXp");
            return DEFAULT_CUMULATIVE_XP;
        }
        return this.cumulativeXp;
    }

    /**
     * The last level the XP table defines, never past the end of that table: the two are read
     * together on every lookup, so a table shortened without its scalar would otherwise index
     * out of bounds on the first level query.
     */
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

    private static void warn(String key) {
        if (WARNED.add(key)) {
            WoldsVaults.LOGGER.error("config/the_vault/gods/god_levels.json has no usable '{}'; "
                    + "falling back to the shipped god level curve for it", key);
        }
    }
}
