package xyz.iwolfking.woldsvaults.milestones.client;

import xyz.iwolfking.woldsvaults.milestones.MilestoneDefinition;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client mirror of the local player's milestone counters, claim state, pin and greed header
 * numbers. Data only — the milestones screen is a later wave and reads this.
 */
public class ClientMilestoneData {
    private static final Map<String, Long> VALUES = new HashMap<>();
    private static final Map<String, Integer> CLAIMED_TIERS = new HashMap<>();

    private static String pinned;
    private static int rank;
    private static int reputation;
    private static int nextRankThreshold;
    private static int unclaimedReputation;
    private static int shopRerollCost;

    private ClientMilestoneData() {
    }

    public static void replaceAll(Map<String, Long> values, Map<String, Integer> claimedTiers) {
        VALUES.clear();
        VALUES.putAll(values);
        CLAIMED_TIERS.clear();
        CLAIMED_TIERS.putAll(claimedTiers);
    }

    public static void apply(Map<String, Long> values, Map<String, Integer> claimedTiers) {
        VALUES.putAll(values);
        CLAIMED_TIERS.putAll(claimedTiers);
    }

    public static void clear() {
        VALUES.clear();
        CLAIMED_TIERS.clear();
        pinned = null;
        rank = 0;
        reputation = 0;
        nextRankThreshold = 0;
        unclaimedReputation = 0;
        shopRerollCost = 0;
    }

    public static long getValue(String milestoneId) {
        return VALUES.getOrDefault(milestoneId, 0L);
    }

    public static int getCompletedTiers(String milestoneId) {
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        return definition == null ? 0 : definition.getCompletedTiers(getValue(milestoneId));
    }

    public static int getClaimedTiers(String milestoneId) {
        return CLAIMED_TIERS.getOrDefault(milestoneId, 0);
    }

    /**
     * Reputation banked on a milestone but not yet collected at Mr. Greedy, derived client-side
     * from the synced counter and claim mark so the screen never needs its own round trip.
     */
    public static int getUnclaimedRep(String milestoneId) {
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        if (definition == null) {
            return 0;
        }
        int completed = definition.getCompletedTiers(getValue(milestoneId));
        int claimed = Math.min(getClaimedTiers(milestoneId), completed);
        int total = 0;
        for (int tier = claimed; tier < completed; tier++) {
            total += definition.getReputation(tier);
        }
        return total;
    }

    public static String getPinned() {
        return pinned;
    }

    public static void setPinned(String milestoneId) {
        pinned = milestoneId;
    }

    public static void setStatus(int newRank, int newReputation, int newNextRankThreshold,
                                 int newUnclaimedReputation, int newShopRerollCost) {
        rank = newRank;
        reputation = newReputation;
        nextRankThreshold = newNextRankThreshold;
        unclaimedReputation = newUnclaimedReputation;
        shopRerollCost = newShopRerollCost;
    }

    public static int getRank() {
        return rank;
    }

    public static int getReputation() {
        return reputation;
    }

    public static int getNextRankThreshold() {
        return nextRankThreshold;
    }

    public static int getUnclaimedReputation() {
        return unclaimedReputation;
    }

    /**
     * Reputation price of the next greed shop reroll. The greed shop has no timed reset — see
     * {@code MilestoneStatusMessage}.
     */
    public static int getShopRerollCost() {
        return shopRerollCost;
    }

    public static Map<String, Long> getAll() {
        return Collections.unmodifiableMap(VALUES);
    }

    public static Map<String, Integer> getAllClaimedTiers() {
        return Collections.unmodifiableMap(CLAIMED_TIERS);
    }
}
