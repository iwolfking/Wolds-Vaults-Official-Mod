package xyz.iwolfking.woldsvaults.milestones;

public class MilestoneDefinition {
    private final String id;
    private final MilestoneCategory category;
    private final MilestoneCounter counter;
    private final long[] thresholds;
    private final int[] reputation;
    private final String nameKey;
    private final String descriptionKey;
    private final String challengeCrystalId;
    private final int requiredRank;

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

    /**
     * The greed challenge crystal this milestone tracks, or null for every non-challenge milestone.
     */
    public String getChallengeCrystalId() {
        return this.challengeCrystalId;
    }

    /**
     * The 1-based greed rank tagged on this milestone in the design sheet, or 0 when untagged.
     * For challenge milestones this doubles as the rank at which the crystal becomes purchasable.
     */
    public int getRequiredRank() {
        return this.requiredRank;
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

    /**
     * Number of fully completed tiers for a raw counter value. 0 means no tier reached,
     * {@link #getTierCount()} means the milestone is finished.
     */
    public int getCompletedTiers(long value) {
        int tier = 0;
        while (tier < this.thresholds.length && value >= this.thresholds[tier]) {
            tier++;
        }
        return tier;
    }

    public boolean isComplete(long value) {
        return value >= this.getFinalThreshold();
    }
}
