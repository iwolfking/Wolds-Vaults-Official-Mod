package xyz.iwolfking.woldsvaults.milestones.trials;

import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

/**
 * The rank-up trial table, one row per rank that can be climbed to: intra-band rank-ups are
 * {@link Kind#VESSEL} trials, band jumps are {@link Kind#HYPER} trials.
 */
public final class GreedTrial {
    public enum Kind {
        VESSEL, HYPER
    }

    /** Vault difficulty a hyper trial is built at. */
    public enum Difficulty {
        HARD("hard"), IMPOSSIBLE("impossible"), FRAGGED("fragged");

        private final String modifierName;

        Difficulty(String modifierName) {
            this.modifierName = modifierName;
        }

        /** The_vault modifier id path that pins a vault to this difficulty. */
        public String getModifierName() {
            return this.modifierName;
        }
    }

    private static final GreedTrial[] BY_RANK = new GreedTrial[MilestoneRankLadder.LEGEND_RANK + 1];

    static {
        vessel(MilestoneRankLadder.SCAVENGER_2, 100_000L, 1.0D, 0.0D);
        vessel(MilestoneRankLadder.SCAVENGER_3, 150_000L, 1.2D, 0.0D);
        hyper(MilestoneRankLadder.LOOTER_1, Difficulty.HARD, 10, 25, 1, 1.0D, 1.25D, 0.5D);
        vessel(MilestoneRankLadder.LOOTER_2, 500_000L, 1.5D, 0.0D);
        vessel(MilestoneRankLadder.LOOTER_3, 850_000L, 2.0D, 0.0D);
        hyper(MilestoneRankLadder.HUNTER_1, Difficulty.IMPOSSIBLE, 15, 30, 2, 3.0D, 1.3D, 0.75D);
        vessel(MilestoneRankLadder.HUNTER_2, 1_500_000L, 3.0D, 0.20D);
        vessel(MilestoneRankLadder.HUNTER_3, 3_000_000L, 3.0D, 0.45D);
        hyper(MilestoneRankLadder.MASTER_1, Difficulty.IMPOSSIBLE, 20, 40, 3, 10.0D, 1.4D, 1.0D);
        vessel(MilestoneRankLadder.MASTER_2, 10_000_000L, 5.0D, 0.50D);
        vessel(MilestoneRankLadder.MASTER_3, 30_000_000L, 10.0D, 0.50D);
        hyper(MilestoneRankLadder.CHAMPION_1, Difficulty.FRAGGED, 25, 40, 3, 20.0D, 1.65D, 1.0D);
        vessel(MilestoneRankLadder.CHAMPION_2, 150_000_000L, 15.0D, 0.75D);
        vessel(MilestoneRankLadder.CHAMPION_3, 500_000_000L, 25.0D, 1.00D);
        hyper(MilestoneRankLadder.LEGEND, Difficulty.FRAGGED, 25, 50, 6, 50.0D, 1.85D, 1.0D);
    }

    private final int targetRank;
    private final Kind kind;
    private final int minutes;
    private final long vesselHealth;
    private final double vesselDamageScale;
    private final double vesselSpeedBonus;
    private final Difficulty difficulty;
    private final int modifierCount;
    private final int requiredCycles;
    private final double bossStrength;
    private final double cycleScaling;
    private final double objectiveScale;

    private GreedTrial(int targetRank, Kind kind, int minutes, long vesselHealth, double vesselDamageScale,
                       double vesselSpeedBonus, Difficulty difficulty, int modifierCount, int requiredCycles,
                       double bossStrength, double cycleScaling, double objectiveScale) {
        this.targetRank = targetRank;
        this.kind = kind;
        this.minutes = minutes;
        this.vesselHealth = vesselHealth;
        this.vesselDamageScale = vesselDamageScale;
        this.vesselSpeedBonus = vesselSpeedBonus;
        this.difficulty = difficulty;
        this.modifierCount = modifierCount;
        this.requiredCycles = requiredCycles;
        this.bossStrength = bossStrength;
        this.cycleScaling = cycleScaling;
        this.objectiveScale = objectiveScale;
    }

    private static void vessel(int rank, long health, double damageScale, double speedBonus) {
        BY_RANK[rank] = new GreedTrial(rank, Kind.VESSEL, 5, health, damageScale, speedBonus,
                null, 0, 0, 0.0D, 0.0D, 1.0D);
    }

    private static void hyper(int rank, Difficulty difficulty, int modifierCount, int minutes,
                              int requiredCycles, double bossStrength, double cycleScaling,
                              double objectiveScale) {
        BY_RANK[rank] = new GreedTrial(rank, Kind.HYPER, minutes, 0L, 0.0D, 0.0D,
                difficulty, modifierCount, requiredCycles, bossStrength, cycleScaling, objectiveScale);
    }

    /** The trial guarding entry into the given rank, or null when that rank has none. */
    public static GreedTrial forRank(int rank) {
        if (rank < MilestoneRankLadder.FIRST_RANK || rank > MilestoneRankLadder.LEGEND_RANK) {
            return null;
        }
        return BY_RANK[rank];
    }

    public int getTargetRank() {
        return this.targetRank;
    }

    public Kind getKind() {
        return this.kind;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public int getTicks() {
        return this.minutes * 60 * 20;
    }

    public long getVesselHealth() {
        return this.vesselHealth;
    }

    public double getVesselDamageScale() {
        return this.vesselDamageScale;
    }

    public double getVesselSpeedBonus() {
        return this.vesselSpeedBonus;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    /** How many rolled modifiers the trial vault is built with, and gains per cycle. */
    public int getModifierCount() {
        return this.modifierCount;
    }

    public int getRequiredCycles() {
        return this.requiredCycles;
    }

    /** Base hyperboss health and damage escalation, in {@code bossHealthPercent} units: 5.0 is 500%. */
    public double getBossStrength() {
        return this.bossStrength;
    }

    /** Per-cycle compounding factor, in place of {@code hyperStatFactor}. */
    public double getCycleScaling() {
        return this.cycleScaling;
    }

    /**
     * Multiplier folded into the elixir target and the bingo/collector requirement scale, so an
     * early trial can run the same objectives at a fraction of a live hyper vault's demands.
     * 1.0 leaves them at their configured values; brutal pillars are never affected.
     */
    public double getObjectiveScale() {
        return this.objectiveScale;
    }

    public int getCoinReward() {
        return MilestoneRankLadder.getTrialCoinReward(this.targetRank);
    }
}
