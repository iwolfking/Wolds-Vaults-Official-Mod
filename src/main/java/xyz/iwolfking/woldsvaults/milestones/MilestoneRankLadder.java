package xyz.iwolfking.woldsvaults.milestones;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedRanksConfig;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The greed rank ladder. Its shape is structure and fixed here, since greed medallion registry ids are built from
 * the band names before any config loads; its numbers (thresholds, god level gates, the step past Legend, trial
 * coins) come from the generated {@code greed/ranks/greed_ranks} table. Until that table is installed no rank past
 * the first can be reached.
 */
public class MilestoneRankLadder {
    public static final int FIRST_RANK = 1;
    public static final int LEGEND_RANK = 16;
    public static final int BAND_SIZE = 3;
    public static final List<String> BAND_NAMES = List.of("scavenger", "looter", "hunter", "master", "champion", "legend");

    public static final int SCAVENGER_1 = 1;
    public static final int SCAVENGER_2 = 2;
    public static final int SCAVENGER_3 = 3;
    public static final int LOOTER_1 = 4;
    public static final int LOOTER_2 = 5;
    public static final int LOOTER_3 = 6;
    public static final int HUNTER_1 = 7;
    public static final int HUNTER_2 = 8;
    public static final int HUNTER_3 = 9;
    public static final int MASTER_1 = 10;
    public static final int MASTER_2 = 11;
    public static final int MASTER_3 = 12;
    public static final int CHAMPION_1 = 13;
    public static final int CHAMPION_2 = 14;
    public static final int CHAMPION_3 = 15;
    public static final int LEGEND = 16;

    private static final AtomicBoolean WARNED = new AtomicBoolean();

    private static volatile int[] thresholds;
    private static volatile int[] godLevelGates = new int[LEGEND_RANK + 1];
    private static volatile int legendPlusStep;
    private static volatile int trialCoinsPerRank;

    private MilestoneRankLadder() {
    }

    /** Installs the generated ladder numbers; the installer has already validated them. */
    public static void load(GreedRanksConfig config) {
        int[] loaded = new int[config.thresholds.size()];
        for (int index = 0; index < loaded.length; index++) {
            loaded[index] = config.thresholds.get(index);
        }
        int[] gates = new int[LEGEND_RANK + 1];
        for (Map.Entry<String, Integer> gate : config.godLevelGates.entrySet()) {
            gates[Integer.parseInt(gate.getKey().trim())] = gate.getValue();
        }
        legendPlusStep = config.legendPlusStep;
        trialCoinsPerRank = config.trialCoinsPerRank;
        godLevelGates = gates;
        thresholds = loaded;
    }

    /** Reputation a rank-up to this 1-based rank costs; rank 1 is 0, and each rank past Legend adds the configured step. */
    public static int getThreshold(int rank) {
        if (rank <= FIRST_RANK) {
            return 0;
        }
        int[] loaded = loadedThresholds();
        if (loaded == null) {
            return Integer.MAX_VALUE;
        }
        if (rank <= LEGEND_RANK) {
            return loaded[rank - 1];
        }
        return loaded[LEGEND_RANK - 1] + (rank - LEGEND_RANK) * legendPlusStep;
    }

    /** Reputation spent climbing from the first rank up to this one; the first rank itself costs nothing. */
    public static int getCumulativeCost(int rank) {
        long total = 0L;
        for (int climbed = FIRST_RANK + 1; climbed <= rank; climbed++) {
            total += getThreshold(climbed);
        }
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    /** God level needed with one god before this rank's trial; 0 when the rank has no gate. */
    public static int getGodLevelGate(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return 0;
        }
        return godLevelGates[rank];
    }

    /** True when climbing to the given rank crosses into a new band. */
    public static boolean isBandJump(int rank) {
        if (rank <= FIRST_RANK || rank > LEGEND_RANK) {
            return false;
        }
        return (rank - FIRST_RANK) % BAND_SIZE == 0;
    }

    /** Band a rank sits in, {@code scavenger} to {@code legend}; empty for a rank off the ladder. */
    public static String getBandName(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return "";
        }
        int band = (rank - FIRST_RANK) / BAND_SIZE;
        return band < BAND_NAMES.size() ? BAND_NAMES.get(band) : BAND_NAMES.get(BAND_NAMES.size() - 1);
    }

    /** Position of a rank inside its band, 1-based; Legend returns 0, being a band of one. */
    public static int getTierInBand(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return 0;
        }
        int position = (rank - FIRST_RANK) % BAND_SIZE;
        return position == 0 && rank == LEGEND_RANK ? 0 : position + 1;
    }

    /** Greed coins paid by a successful rank-up trial: the configured amount per rank climbed past the first. */
    public static int getTrialCoinReward(int rank) {
        return trialCoinsPerRank * Math.max(0, rank - 1);
    }

    @Nullable
    private static int[] loadedThresholds() {
        int[] loaded = thresholds;
        if (loaded == null && WARNED.compareAndSet(false, true)) {
            WoldsVaults.LOGGER.error("The greed rank ladder was read before the greed/ranks/greed_ranks table was installed; "
                    + "no rank past the first can be reached until it is");
        }
        return loaded;
    }
}
