package xyz.iwolfking.woldsvaults.milestones.trials;

import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

/**
 * The rank-up trial table, one row per rank that can be climbed to. Every number here comes
 * straight from the design spreadsheet's "Rank Up Trials" sheet; nothing is derived at runtime so
 * a sheet change is a one-line edit here.
 *
 * <p>Intra-band rank-ups are {@link Kind#VESSEL} trials: a single-phase greed vessel with a fixed
 * health pool that has to be emptied inside five minutes. Band jumps are {@link Kind#HYPER}
 * trials: a toned-down hyper objective that ends itself once the required number of boss cycles
 * is done.</p>
 *
 * <p>The vessel column on the sheet reads "Damage/Speed Scaling" and carries entries of the form
 * {@code 3x +20% spd}. That is read here as a damage multiplier plus a separate movement-speed
 * bonus, so rows written as a bare multiplier ({@code 1.5x}) get no speed bonus at all. The design
 * doc calls the sheet value a STARTING multiplier that "ramps up over the fight"; the ramp is the
 * vessel's own built-in one ({@code TheVesselEntity} already scales damage by 1.2 per minute for
 * the first five and speed by +5% per minute), so these values are applied as flat multipliers on
 * top of it rather than as a second, invented ramp.</p>
 */
public final class GreedTrial {
    public enum Kind {
        VESSEL, HYPER
    }

    /** Vault difficulty a hyper trial is built at, matching the sheet's "Base Difficulty" column. */
    public enum Difficulty {
        HARD("hard"), IMPOSSIBLE("impossible"), FRAGGED("fragged");

        private final String modifierName;

        Difficulty(String modifierName) {
            this.modifierName = modifierName;
        }

        /** the_vault modifier id path that pins a vault to this difficulty. */
        public String getModifierName() {
            return this.modifierName;
        }
    }

    private static final GreedTrial[] BY_RANK = new GreedTrial[MilestoneRankLadder.LEGEND_RANK + 1];

    static {
        vessel(MilestoneRankLadder.SCAVENGER_2, 100_000L, 1.0D, 0.0D);
        vessel(MilestoneRankLadder.SCAVENGER_3, 150_000L, 1.2D, 0.0D);
        hyper(MilestoneRankLadder.LOOTER_1, Difficulty.HARD, 10, 25, 1, 5.0D, 1.25D);
        vessel(MilestoneRankLadder.LOOTER_2, 500_000L, 1.5D, 0.0D);
        vessel(MilestoneRankLadder.LOOTER_3, 850_000L, 2.0D, 0.0D);
        hyper(MilestoneRankLadder.HUNTER_1, Difficulty.IMPOSSIBLE, 15, 25, 2, 10.0D, 1.3D);
        vessel(MilestoneRankLadder.HUNTER_2, 1_500_000L, 3.0D, 0.20D);
        vessel(MilestoneRankLadder.HUNTER_3, 3_000_000L, 3.0D, 0.45D);
        hyper(MilestoneRankLadder.MASTER_1, Difficulty.IMPOSSIBLE, 20, 25, 3, 20.0D, 1.4D);
        vessel(MilestoneRankLadder.MASTER_2, 10_000_000L, 5.0D, 0.50D);
        vessel(MilestoneRankLadder.MASTER_3, 30_000_000L, 10.0D, 0.50D);
        hyper(MilestoneRankLadder.CHAMPION_1, Difficulty.FRAGGED, 25, 25, 3, 30.0D, 1.65D);
        vessel(MilestoneRankLadder.CHAMPION_2, 150_000_000L, 15.0D, 0.75D);
        vessel(MilestoneRankLadder.CHAMPION_3, 500_000_000L, 25.0D, 1.00D);
        hyper(MilestoneRankLadder.LEGEND, Difficulty.FRAGGED, 25, 50, 6, 50.0D, 1.85D);
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

    private GreedTrial(int targetRank, Kind kind, int minutes, long vesselHealth, double vesselDamageScale,
                       double vesselSpeedBonus, Difficulty difficulty, int modifierCount, int requiredCycles,
                       double bossStrength, double cycleScaling) {
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
    }

    private static void vessel(int rank, long health, double damageScale, double speedBonus) {
        BY_RANK[rank] = new GreedTrial(rank, Kind.VESSEL, 5, health, damageScale, speedBonus,
                null, 0, 0, 0.0D, 0.0D);
    }

    private static void hyper(int rank, Difficulty difficulty, int modifierCount, int minutes,
                              int requiredCycles, double bossStrength, double cycleScaling) {
        BY_RANK[rank] = new GreedTrial(rank, Kind.HYPER, minutes, 0L, 0.0D, 0.0D,
                difficulty, modifierCount, requiredCycles, bossStrength, cycleScaling);
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

    /**
     * The sheet's "N modifiers +N" column. Both halves are the same number on every row, so one
     * value carries it: the trial vault is built with this many rolled modifiers.
     */
    public int getModifierCount() {
        return this.modifierCount;
    }

    public int getRequiredCycles() {
        return this.requiredCycles;
    }

    /**
     * Base hyperboss health and damage escalation as a fraction, matching
     * {@code HyperObjectiveConfig#getBossHealthPercent}'s unit: 5.0 is the sheet's 500% H/D.
     */
    public double getBossStrength() {
        return this.bossStrength;
    }

    /** Per-cycle compounding factor, the trial's stand-in for {@code hyperStatFactor}. */
    public double getCycleScaling() {
        return this.cycleScaling;
    }

    public int getCoinReward() {
        return MilestoneRankLadder.getTrialCoinReward(this.targetRank);
    }
}
