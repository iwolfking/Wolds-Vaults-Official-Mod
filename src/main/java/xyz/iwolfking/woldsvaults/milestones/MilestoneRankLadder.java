package xyz.iwolfking.woldsvaults.milestones;

public class MilestoneRankLadder {
    public static final int FIRST_RANK = 1;
    public static final int LEGEND_RANK = 16;
    public static final int LEGEND_PLUS_STEP = 250;

    private static final int[] THRESHOLDS = {
            0, 75, 100, 125, 150, 175, 200, 250, 300, 400, 500, 550, 600, 800, 900, 1000
    };

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

    /**
     * God-alignment gate on a rank-up, indexed by the rank being climbed TO. Only the four band
     * jumps that open a new band carry one; every other entry is zero. The gate is satisfied by
     * reaching the level in any single god, not by a sum across gods.
     */
    private static final int[] GOD_LEVEL_GATES = new int[LEGEND_RANK + 1];

    static {
        GOD_LEVEL_GATES[HUNTER_1] = 2;
        GOD_LEVEL_GATES[MASTER_1] = 4;
        GOD_LEVEL_GATES[CHAMPION_1] = 6;
        GOD_LEVEL_GATES[LEGEND] = 8;
    }

    private MilestoneRankLadder() {
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
            return THRESHOLDS[rank - 1];
        }
        return THRESHOLDS[LEGEND_RANK - 1] + (rank - LEGEND_RANK) * LEGEND_PLUS_STEP;
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
     * Legend — Legend+ is a reputation-only climb.
     */
    public static int getGodLevelGate(int rank) {
        if (rank < FIRST_RANK || rank > LEGEND_RANK) {
            return 0;
        }
        return GOD_LEVEL_GATES[rank];
    }

    /**
     * True when climbing to the given rank crosses into a new band (Looter 1, Hunter 1, Master 1,
     * Champion 1, Legend). Band jumps are the hyper trials; every other rank-up is a vessel trial.
     */
    public static boolean isBandJump(int rank) {
        if (rank <= FIRST_RANK || rank > LEGEND_RANK) {
            return false;
        }
        return (rank - 1) % 3 == 0;
    }

    /**
     * Greed coins paid by a successful rank-up trial: 50 per rank climbed past the first, so the
     * Scavenger 2 trial pays 50 and the Legend trial pays 750.
     */
    public static int getTrialCoinReward(int rank) {
        return 50 * Math.max(0, rank - 1);
    }
}
