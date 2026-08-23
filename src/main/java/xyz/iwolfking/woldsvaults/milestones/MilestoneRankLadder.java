package xyz.iwolfking.woldsvaults.milestones;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedRanksConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one definition of the greed rank ladder. Its shape - the reputation threshold of every rank,
 * how many ranks a band holds, what each band is called and which band jumps carry a god alignment
 * gate - is pack data in {@code config/the_vault/gods/greed_ranks.json}, installed here by
 * {@code ModConfigs.register}. Nothing else in the addon re-derives any of it.
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
     * Installs a ladder read from config. A file that does not describe a ladder of exactly
     * {@link #LEGEND_RANK} ranks, whose thresholds do not strictly ascend, or that names too few
     * bands to cover the ladder, is refused with an error naming the problem and the shipped
     * defaults are used instead: a half-applied ladder would silently move every rank gate,
     * medallion name and trial requirement in the game, and a threshold that dips would put a rank
     * behind a smaller number than the rank below it, which the reputation bar reads as a full bar
     * that never ranks up.
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

    /**
     * Installs the ladder a server sent, so the client's reputation bar, rank badges, trial gates
     * and medallion lookups all measure against the host's ladder rather than the
     * {@code greed_ranks.json} on the player's own disk.
     *
     * <p>The ladder is small and wholly numeric, so the server's copy simply replaces the local one
     * rather than being kept alongside it: every getter here is on a hot render path and would
     * otherwise have to branch. The local ladder is stashed on the first install and put back by
     * {@link #restoreLocal}, which the client mirror calls on disconnect. On an integrated server
     * the values being installed were read out of these very fields a moment earlier, so the write
     * is an identity there.</p>
     *
     * <p>A payload that does not describe a ladder of the right shape is refused by the same check
     * a config file goes through, leaving the client on its own ladder - which is the closest thing
     * to right that is available when the two sides disagree about the ladder's length.</p>
     */
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

    /**
     * Puts the locally configured ladder back, undoing {@link #applyServerLadder}. Idempotent, and
     * a no-op when no server ladder was ever installed.
     */
    public static void restoreLocal() {
        GreedRanksConfig local = localLadder;
        if (local == null) {
            return;
        }
        localLadder = null;
        apply(local);
    }

    /**
     * The live ladder as a config object. Used to stash the local ladder before a server's replaces
     * it, and to put the server's ladder on the wire without a second serialisation format.
     */
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

    /**
     * Reports band names that differ from the shipped ones without refusing the ladder.
     *
     * <p>A greed medallion's registry id is {@code greed_medallion_<band>_<tier>}, built from these
     * names while items are registered - which happens long before this config is read. Renaming a
     * band therefore cannot rename the items: the ids stay on the shipped names, and every screen
     * that resolves a medallion by band name from the live ladder stops finding one. The ladder is
     * still applied, because the thresholds and gates in the same file are usable and refusing them
     * over a cosmetic rename would be the larger surprise.</p>
     */
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

    /**
     * Reputation required to hold the given 1-based rank. Rank 1 (Scavenger 1) is 0 rep;
     * every rank past Legend costs a further {@value #LEGEND_PLUS_STEP}.
     */
    public static int getThreshold(int rank) {
        if (rank <= FIRST_RANK) {
            return 0;
        }
        if (rank <= LEGEND_RANK) {
            return thresholds[rank - 1];
        }
        return thresholds[LEGEND_RANK - 1] + (rank - LEGEND_RANK) * LEGEND_PLUS_STEP;
    }

    /**
     * Reputation awarded by a challenge-crystal milestone tagged with the given 1-based rank:
     * 15% of the threshold of the rank-up immediately after the tagged rank, floored.
     */
    public static int getChallengeReputation(int taggedRank) {
        return (int) Math.floor(0.15D * getThreshold(taggedRank + 1));
    }

    /**
     * God alignment level the player must hold with at least one god before the rank-up trial for
     * the given rank can be taken. Zero for every rank without a gate, including every rank past
     * Legend - Legend+ is a reputation-only climb.
     */
    public static int getGodLevelGate(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return 0;
        }
        return godLevelGates[rank];
    }

    /**
     * True when climbing to the given rank crosses into a new band (Looter 1, Hunter 1, Master 1,
     * Champion 1, Legend). Band jumps are the hyper trials; every other rank-up is a vessel trial.
     */
    public static boolean isBandJump(int rank) {
        if (rank <= FIRST_RANK || rank > LEGEND_RANK) {
            return false;
        }
        return (rank - FIRST_RANK) % bandSize == 0;
    }

    /**
     * Band a rank sits in: {@code scavenger} through {@code champion}, then {@code legend}, and
     * empty for a rank off the ladder. Medallion registry paths, medallion display names, band
     * colours and the rank badges on the reputation bar all read this instead of keeping a copy.
     */
    public static String getBandName(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return "";
        }
        int band = (rank - FIRST_RANK) / bandSize;
        return band < bandNames.length ? bandNames[band] : bandNames[bandNames.length - 1];
    }

    /**
     * Position of a rank inside its band, 1-based. Legend returns 0 because it is a band of one
     * and is named without a step - {@code legend}, not {@code legend_1}.
     */
    public static int getTierInBand(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return 0;
        }
        int position = (rank - FIRST_RANK) % bandSize;
        return position == 0 && rank == LEGEND_RANK ? 0 : position + 1;
    }

    /**
     * Greed coins paid by a successful rank-up trial: 50 per rank climbed past the first, so the
     * Scavenger 2 trial pays 50 and the Legend trial pays 750.
     */
    public static int getTrialCoinReward(int rank) {
        return 50 * Math.max(0, rank - 1);
    }
}
