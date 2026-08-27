package xyz.iwolfking.woldsvaults.milestones;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedRanksConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The greed rank ladder: every rank's reputation threshold, the bands, and which ranks carry a god
 * alignment gate. Values come from {@code config/the_vault/gods/greed_ranks.json}.
 */
public class MilestoneRankLadder {
    public static final int FIRST_RANK = 1;
    public static final int LEGEND_RANK = 16;
    public static final int LEGEND_PLUS_STEP = 250;

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

    private static volatile int[] thresholds;
    private static volatile int[] godLevelGates;
    private static volatile String[] bandNames;
    private static volatile int bandSize;

    private static volatile GreedRanksConfig localLadder;

    static {
        apply(GreedRanksConfig.defaults());
    }

    private MilestoneRankLadder() {
    }

    /**
     * Installs a ladder read from config. Falls back to the shipped defaults, with an error, unless
     * the ladder has exactly {@link #LEGEND_RANK} ranks, strictly ascending thresholds and enough
     * band names to cover it.
     */
    public static void load(GreedRanksConfig config) {
        localLadder = null;
        if (isUsable(config)) {
            reportRenamedBands(config.getBandNames());
            apply(config);
            return;
        }
        apply(GreedRanksConfig.defaults());
    }

    /** Installs the ladder a server sent, stashing the local one; a bad payload is refused. */
    public static void applyServerLadder(GreedRanksConfig sent) {
        if (!isUsable(sent)) {
            WoldsVaults.LOGGER.error("The server's greed rank ladder was refused; the client keeps its own, so rank thresholds on screen may not match the server's");
            return;
        }
        if (localLadder == null) {
            localLadder = snapshot();
        }
        apply(sent);
    }

    /** Puts the local ladder back. Idempotent, and a no-op when no server ladder was installed. */
    public static void restoreLocal() {
        GreedRanksConfig local = localLadder;
        if (local == null) {
            return;
        }
        localLadder = null;
        apply(local);
    }

    /** The live ladder as a config object. */
    public static GreedRanksConfig snapshot() {
        List<Integer> rankThresholds = new ArrayList<>(LEGEND_RANK);
        for (int rank = FIRST_RANK; rank <= LEGEND_RANK; rank++) {
            rankThresholds.add(getThreshold(rank));
        }
        Map<String, Integer> gates = new LinkedHashMap<>();
        for (int rank = FIRST_RANK; rank <= LEGEND_RANK; rank++) {
            if (godLevelGates[rank] > 0) {
                gates.put(Integer.toString(rank), godLevelGates[rank]);
            }
        }
        return GreedRanksConfig.of(bandSize, List.of(bandNames), rankThresholds, gates);
    }

    /** Logs renamed bands; medallion registry ids are built from the shipped names and cannot follow. */
    private static void reportRenamedBands(List<String> names) {
        List<String> shipped = GreedRanksConfig.defaults().getBandNames();
        for (int index = 0; index < Math.min(names.size(), shipped.size()); index++) {
            if (!shipped.get(index).equals(names.get(index))) {
                WoldsVaults.LOGGER.error("greed_ranks.json renames band {} from '{}' to '{}'; greed medallion registry ids are built from the shipped names and cannot follow, so that band's medallions will not resolve",
                        index, shipped.get(index), names.get(index));
            }
        }
    }

    private static boolean isUsable(GreedRanksConfig config) {
        List<Integer> configured = config.getThresholds();
        if (configured == null || configured.size() != LEGEND_RANK) {
            WoldsVaults.LOGGER.error("greed_ranks.json lists {} rank thresholds but the ladder is {} ranks long; using the shipped ladder instead",
                    configured == null ? 0 : configured.size(), LEGEND_RANK);
            return false;
        }
        for (int index = 1; index < configured.size(); index++) {
            if (configured.get(index) <= configured.get(index - 1)) {
                WoldsVaults.LOGGER.error("greed_ranks.json puts rank {} at {} reputation and rank {} at {}; rank thresholds must strictly ascend, so the shipped ladder is used instead",
                        index + 1, configured.get(index), index, configured.get(index - 1));
                return false;
            }
        }
        if (config.getBandSize() <= 0) {
            WoldsVaults.LOGGER.error("greed_ranks.json sets bandSize to {}, which cannot divide the ladder; using the shipped ladder instead",
                    config.getBandSize());
            return false;
        }
        List<String> names = config.getBandNames();
        int required = (LEGEND_RANK - FIRST_RANK) / config.getBandSize() + 1;
        if (names == null || names.size() < required) {
            WoldsVaults.LOGGER.error("greed_ranks.json names {} bands but a {} rank ladder in bands of {} needs {}; using the shipped ladder instead",
                    names == null ? 0 : names.size(), LEGEND_RANK, config.getBandSize(), required);
            return false;
        }
        return true;
    }

    private static void apply(GreedRanksConfig config) {
        List<Integer> configured = config.getThresholds();
        thresholds = new int[configured.size()];
        for (int index = 0; index < configured.size(); index++) {
            thresholds[index] = configured.get(index);
        }
        bandSize = config.getBandSize();
        bandNames = config.getBandNames().toArray(new String[0]);
        godLevelGates = new int[LEGEND_RANK + 1];
        Map<String, Integer> gates = config.getGodLevelGates();
        if (gates == null) {
            return;
        }
        for (Map.Entry<String, Integer> gate : gates.entrySet()) {
            int rank;
            try {
                rank = Integer.parseInt(gate.getKey().trim());
            } catch (NumberFormatException e) {
                WoldsVaults.LOGGER.error("greed_ranks.json god level gate key '{}' is not a rank number; that gate is ignored", gate.getKey());
                continue;
            }
            if (rank < FIRST_RANK || rank > LEGEND_RANK) {
                WoldsVaults.LOGGER.error("greed_ranks.json god level gate is keyed to rank {}, which is outside the ladder; that gate is ignored", rank);
                continue;
            }
            godLevelGates[rank] = gate.getValue() == null ? 0 : gate.getValue();
        }
    }

    /** Reputation a rank-up to this 1-based rank costs; rank 1 is 0, each rank past Legend adds {@value #LEGEND_PLUS_STEP}. */
    public static int getThreshold(int rank) {
        if (rank <= FIRST_RANK) {
            return 0;
        }
        if (rank <= LEGEND_RANK) {
            return thresholds[rank - 1];
        }
        return thresholds[LEGEND_RANK - 1] + (rank - LEGEND_RANK) * LEGEND_PLUS_STEP;
    }

    /** Reputation spent climbing from the first rank up to this one; the first rank itself costs nothing. */
    public static int getCumulativeCost(int rank) {
        int total = 0;
        for (int climbed = FIRST_RANK + 1; climbed <= rank; climbed++) {
            total += getThreshold(climbed);
        }
        return total;
    }

    /** Reputation a milestone tagged with this rank pays: 15% of the next rank's threshold, floored. */
    public static int getChallengeReputation(int taggedRank) {
        return (int) Math.floor(0.15D * getThreshold(taggedRank + 1));
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
        return (rank - FIRST_RANK) % bandSize == 0;
    }

    /** Band a rank sits in, {@code scavenger} to {@code legend}; empty for a rank off the ladder. */
    public static String getBandName(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return "";
        }
        int band = (rank - FIRST_RANK) / bandSize;
        return band < bandNames.length ? bandNames[band] : bandNames[bandNames.length - 1];
    }

    /** Position of a rank inside its band, 1-based; Legend returns 0, being a band of one. */
    public static int getTierInBand(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return 0;
        }
        int position = (rank - FIRST_RANK) % bandSize;
        return position == 0 && rank == LEGEND_RANK ? 0 : position + 1;
    }

    /** Greed coins paid by a successful rank-up trial: 50 per rank climbed past the first. */
    public static int getTrialCoinReward(int rank) {
        return 50 * Math.max(0, rank - 1);
    }
}
