package xyz.iwolfking.woldsvaults.milestones;

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

    /**
     * Replaces this milestone's numbers with the ones a server sent, keeping its structure - id,
     * category, counter, challenge crystal - which is code and identical on both sides.
     *
     * <p>Only {@link MilestoneRegistry#applyServerTables} calls this, and only on the client, so
     * that the screens read the host's balance instead of whatever {@code greed_milestones.json}
     * the player happens to have on disk. The arrays are swapped whole rather than written through,
     * so a reader mid-swap sees one complete table or the other; on an integrated server the values
     * being installed came from these very fields, which makes the write an identity.</p>
     *
     * <p>A payload whose tier count does not line up is refused outright rather than half-applied:
     * a definition with more thresholds than reputation values would throw out of the claim path
     * later, far from the packet that caused it.</p>
     */
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

    /**
     * Number of fully completed tiers for a raw counter value. 0 means no tier reached,
     * {@link #getTierCount()} means the milestone is finished.
     */
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

    /**
     * Whether this milestone's counter is a tick count rather than a tally of things. The two that
     * are exist because the only per-player clock the engine has is the listener tick, so their
     * thresholds are raw ticks and every readout of them has to be formatted as elapsed time.
     */
    public boolean isDuration() {
        return DURATION_MILESTONES.contains(this.id);
    }
}
