package xyz.iwolfking.woldsvaults.milestones.trials;

import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.HashSet;
import java.util.Set;

/**
 * The rank-up trial table, one row per rank that can be climbed to: intra-band rank-ups are
 * {@link Kind#VESSEL} trials, band jumps are {@link Kind#HYPER} trials.
 */
public final class GreedTrial {
    public enum Kind {
        VESSEL, HYPER
    }

    /**
     * Vault difficulty a hyper trial is built at. Every entry maps to a {@code difficulty_lock}
     * modifier with {@code lockHigher = false}, so a row sets a floor and never drags a player
     * who chose a harder mode back down.
     */
    public enum Difficulty {
        NORMAL("normalized"), HARD("hard"), IMPOSSIBLE("impossible"), FRAGGED("fragged");

        private final String modifierName;

        Difficulty(String modifierName) {
            this.modifierName = modifierName;
        }

        /** The_vault modifier id path that pins a vault to this difficulty. */
        public String getModifierName() {
            return this.modifierName;
        }
    }

    /**
     * Modifiers neither of the two lowest hyper trials receives: every roll that voids vault
     * time, the electric mob storm, the safari spawner trio and the Wounded max-health drain.
     */
    private static final Set<String> SHARED_BANS = Set.of(
            "the_vault:wendarr_challenge",
            "the_vault:ticking_clock",
            "the_vault:voiding",
            "the_vault:haunting",
            "the_vault:electric",
            "the_vault:safari",
            "the_vault:wounded");

    /**
     * The first hyper trial additionally refuses the heaviest mob-stat rolls, the challenge-stack
     * ticker, the resistance strip and the void pool decorator.
     */
    private static final Set<String> LOOTER_BANS = union(SHARED_BANS,
            "the_vault:abusive_mobs",
            "the_vault:brutal_mobs",
            "the_vault:creeping_doom",
            "the_vault:idona_challenge",
            "the_vault:piercing",
            "the_vault:chunky_mobs4",
            "the_vault:void_pools");

    private static final GreedTrial[] BY_RANK = new GreedTrial[MilestoneRankLadder.LEGEND_RANK + 1];

    static {
        vessel(MilestoneRankLadder.SCAVENGER_2, 100_000L, 1.0D, 0.0D);
        vessel(MilestoneRankLadder.SCAVENGER_3, 150_000L, 1.2D, 0.0D);
        hyper(MilestoneRankLadder.LOOTER_1).difficulty(Difficulty.NORMAL).modifiers(10).minutes(40)
                .cycles(1).boss(0.0D, 1.25D).objectiveScale(0.5D).speedCap(1.3D)
                .ambientPeriod(4800).positiveBrutalRewards().phoenix(1).bans(LOOTER_BANS).register();
        vessel(MilestoneRankLadder.LOOTER_2, 500_000L, 1.5D, 0.0D);
        vessel(MilestoneRankLadder.LOOTER_3, 850_000L, 2.0D, 0.0D);
        hyper(MilestoneRankLadder.HUNTER_1).difficulty(Difficulty.IMPOSSIBLE).modifiers(15).minutes(40)
                .cycles(2).boss(3.0D, 1.3D).objectiveScale(0.75D)
                .positiveBrutalRewards().phoenix(1).bans(SHARED_BANS).register();
        vessel(MilestoneRankLadder.HUNTER_2, 1_500_000L, 3.0D, 0.20D);
        vessel(MilestoneRankLadder.HUNTER_3, 3_000_000L, 3.0D, 0.45D);
        hyper(MilestoneRankLadder.MASTER_1).difficulty(Difficulty.IMPOSSIBLE).modifiers(20).minutes(50)
                .cycles(3).boss(10.0D, 1.4D).register();
        vessel(MilestoneRankLadder.MASTER_2, 10_000_000L, 5.0D, 0.50D);
        vessel(MilestoneRankLadder.MASTER_3, 30_000_000L, 10.0D, 0.50D);
        hyper(MilestoneRankLadder.CHAMPION_1).difficulty(Difficulty.FRAGGED).modifiers(25).minutes(50)
                .cycles(3).boss(20.0D, 1.65D).register();
        vessel(MilestoneRankLadder.CHAMPION_2, 150_000_000L, 15.0D, 0.75D);
        vessel(MilestoneRankLadder.CHAMPION_3, 500_000_000L, 25.0D, 1.00D);
        hyper(MilestoneRankLadder.LEGEND).difficulty(Difficulty.FRAGGED).modifiers(25).minutes(60)
                .cycles(6).boss(50.0D, 1.85D).register();
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
    private final double speedCapFactor;
    private final int ambientPeriodTicks;
    private final boolean positiveBrutalRewards;
    private final int phoenixStacks;
    private final Set<String> bannedModifiers;

    private GreedTrial(Builder builder) {
        this.targetRank = builder.targetRank;
        this.kind = builder.kind;
        this.minutes = builder.minutes;
        this.vesselHealth = builder.vesselHealth;
        this.vesselDamageScale = builder.vesselDamageScale;
        this.vesselSpeedBonus = builder.vesselSpeedBonus;
        this.difficulty = builder.difficulty;
        this.modifierCount = builder.modifierCount;
        this.requiredCycles = builder.requiredCycles;
        this.bossStrength = builder.bossStrength;
        this.cycleScaling = builder.cycleScaling;
        this.objectiveScale = builder.objectiveScale;
        this.speedCapFactor = builder.speedCapFactor;
        this.ambientPeriodTicks = builder.ambientPeriodTicks;
        this.positiveBrutalRewards = builder.positiveBrutalRewards;
        this.phoenixStacks = builder.phoenixStacks;
        this.bannedModifiers = builder.bannedModifiers;
    }

    private static Set<String> union(Set<String> base, String... extra) {
        Set<String> merged = new HashSet<>(base);
        merged.addAll(Set.of(extra));
        return Set.copyOf(merged);
    }

    private static void vessel(int rank, long health, double damageScale, double speedBonus) {
        Builder builder = new Builder(rank, Kind.VESSEL);
        builder.minutes = 5;
        builder.vesselHealth = health;
        builder.vesselDamageScale = damageScale;
        builder.vesselSpeedBonus = speedBonus;
        builder.register();
    }

    private static Builder hyper(int rank) {
        return new Builder(rank, Kind.HYPER);
    }

    /**
     * One row of the sheet under construction. Anything left unset keeps the live
     * {@code hyper_objective.json} value, so a row only states where it differs.
     */
    private static final class Builder {
        private final int targetRank;
        private final Kind kind;
        private int minutes;
        private long vesselHealth;
        private double vesselDamageScale;
        private double vesselSpeedBonus;
        private Difficulty difficulty;
        private int modifierCount;
        private int requiredCycles;
        private double bossStrength;
        private double cycleScaling;
        private double objectiveScale = 1.0D;
        private double speedCapFactor;
        private int ambientPeriodTicks;
        private boolean positiveBrutalRewards;
        private int phoenixStacks;
        private Set<String> bannedModifiers = Set.of();

        private Builder(int targetRank, Kind kind) {
            this.targetRank = targetRank;
            this.kind = kind;
        }

        private Builder difficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        private Builder modifiers(int modifierCount) {
            this.modifierCount = modifierCount;
            return this;
        }

        private Builder minutes(int minutes) {
            this.minutes = minutes;
            return this;
        }

        private Builder cycles(int requiredCycles) {
            this.requiredCycles = requiredCycles;
            return this;
        }

        private Builder boss(double bossStrength, double cycleScaling) {
            this.bossStrength = bossStrength;
            this.cycleScaling = cycleScaling;
            return this;
        }

        private Builder objectiveScale(double objectiveScale) {
            this.objectiveScale = objectiveScale;
            return this;
        }

        private Builder speedCap(double speedCapFactor) {
            this.speedCapFactor = speedCapFactor;
            return this;
        }

        private Builder ambientPeriod(int ambientPeriodTicks) {
            this.ambientPeriodTicks = ambientPeriodTicks;
            return this;
        }

        private Builder positiveBrutalRewards() {
            this.positiveBrutalRewards = true;
            return this;
        }

        private Builder phoenix(int phoenixStacks) {
            this.phoenixStacks = phoenixStacks;
            return this;
        }

        private Builder bans(Set<String> bannedModifiers) {
            this.bannedModifiers = bannedModifiers;
            return this;
        }

        private void register() {
            BY_RANK[this.targetRank] = new GreedTrial(this);
        }
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

    /** Mob movement speed ceiling as a multiple of base; 0 keeps the configured hyper cap. */
    public double getSpeedCapFactor() {
        return this.speedCapFactor;
    }

    /** Ticks between ambient negative pulls; 0 keeps the configured hyper period. */
    public int getAmbientPeriodTicks() {
        return this.ambientPeriodTicks;
    }

    /**
     * Whether brutal pillar bosses pay their own bb_ pool, the positive rolls an ordinary brutal
     * vault gives, instead of a negative hyper roll drawn from the shared chaos budget.
     */
    public boolean hasPositiveBrutalRewards() {
        return this.positiveBrutalRewards;
    }

    /** Free Phoenix stacks the trial vault opens with; 0 adds none. */
    public int getPhoenixStacks() {
        return this.phoenixStacks;
    }

    /** Modifier ids this trial refuses, whatever pool they are rolled from. Never null. */
    public Set<String> getBannedModifiers() {
        return this.bannedModifiers;
    }

    public int getCoinReward() {
        return MilestoneRankLadder.getTrialCoinReward(this.targetRank);
    }
}
