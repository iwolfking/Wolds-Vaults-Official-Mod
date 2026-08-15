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
}
