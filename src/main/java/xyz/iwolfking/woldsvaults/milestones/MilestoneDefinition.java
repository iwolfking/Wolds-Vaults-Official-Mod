package xyz.iwolfking.woldsvaults.milestones;

import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;

import java.util.Comparator;
import java.util.Set;

public class MilestoneDefinition {
    private static final Set<String> DURATION_MILESTONES =
            Set.of(MilestoneIds.I_WILL_SURVIVE, MilestoneIds.I_LIVE_HERE_NOW);

    private final String id;
    private final MilestoneCategory category;
    private final MilestoneCounter counter;
    private volatile long[] thresholds;
    private volatile int[] reputation;
    private final String nameKey;
    private final String descriptionKey;
    private final String challengeCrystalId;
    private volatile int requiredRank;

    public MilestoneDefinition(String id, MilestoneCategory category, MilestoneCounter counter, long[] thresholds, int[] reputation) {
        this(id, category, counter, thresholds, reputation, null, 0);
    }

    public MilestoneDefinition(String id, MilestoneCategory category, MilestoneCounter counter, long[] thresholds, int[] reputation,
                               String challengeCrystalId, int requiredRank) {
        if (thresholds.length == 0) {
            throw new IllegalArgumentException("Milestone " + id + " has no tiers");
        }
        if (thresholds.length != reputation.length) {
            throw new IllegalArgumentException("Milestone " + id + " has " + thresholds.length + " tiers but " + reputation.length + " reputation values");
        }
        this.id = id;
        this.category = category;
        this.counter = counter;
        this.thresholds = thresholds;
        this.reputation = reputation;
        this.nameKey = "milestone.woldsvaults." + id;
        this.descriptionKey = "milestone.woldsvaults." + id + ".desc";
        this.challengeCrystalId = challengeCrystalId;
        this.requiredRank = requiredRank;
    }

    /** The greed challenge crystal this milestone tracks, or null for a non-challenge milestone. */
    public String getChallengeCrystalId() {
        return this.challengeCrystalId;
    }

    /** The 1-based greed rank tagged on this milestone, or 0 when untagged. */
    public int getRequiredRank() {
        return this.requiredRank;
    }

    /** Swaps in new numbers whole. Throws when the arrays are empty or of unequal length. */
    void applyNumbers(long[] newThresholds, int[] newReputation, int newRequiredRank) {
        if (newThresholds.length == 0 || newThresholds.length != newReputation.length) {
            throw new IllegalArgumentException("Milestone " + this.id + " was sent " + newThresholds.length
                    + " thresholds and " + newReputation.length + " reputation values");
        }
        this.thresholds = newThresholds;
        this.reputation = newReputation;
        this.requiredRank = newRequiredRank;
    }

    public String getId() {
        return this.id;
    }

    public MilestoneCategory getCategory() {
        return this.category;
    }

    public MilestoneCounter getCounter() {
        return this.counter;
    }

    public int getTierCount() {
        return this.thresholds.length;
    }

    public long getThreshold(int tier) {
        return this.thresholds[tier];
    }

    public int getReputation(int tier) {
        return this.reputation[tier];
    }

    public long getFinalThreshold() {
        return this.thresholds[this.thresholds.length - 1];
    }

    public String getNameKey() {
        return this.nameKey;
    }

    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    /** Fully completed tiers for a raw counter value; {@link #getTierCount()} means finished. */
    public int getCompletedTiers(long value) {
        long[] table = this.thresholds;
        int tier = 0;
        while (tier < table.length && value >= table[tier]) {
            tier++;
        }
        return tier;
    }

    public boolean isComplete(long value) {
        return value >= this.getFinalThreshold();
    }

    /** Whether this milestone's thresholds are raw ticks, to be read out as elapsed time. */
    public boolean isDuration() {
        return DURATION_MILESTONES.contains(this.id);
    }


    public static Comparator<MilestoneDefinition> compareByProgress() {
        return Comparator.comparing((MilestoneDefinition definition) -> {
            long value = ClientMilestoneData.getValue(definition.getId());
            int completed = definition.getCompletedTiers(value);
            boolean finished = completed >= definition.getTierCount();

            if (finished) {
                return 1F;
            }
            long floor = completed == 0 ? 0L : definition.getThreshold(completed - 1);
            long ceiling = definition.getThreshold(completed);
            long span = ceiling - floor;
            return span <= 0L ? 0.0F : (float) (value - floor) / (float) span;
        }).thenComparing(MilestoneDefinition::getId);
    }

}
