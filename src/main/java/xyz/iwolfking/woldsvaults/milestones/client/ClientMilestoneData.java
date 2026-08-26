package xyz.iwolfking.woldsvaults.milestones.client;

import xyz.iwolfking.woldsvaults.milestones.MilestoneDefinition;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;

import java.util.*;

/** Client mirror of the local player's milestone counters, claim state, pin and greed header. */
public class ClientMilestoneData {
    private static final Map<String, Long> VALUES = new HashMap<>();
    private static final Map<String, Integer> CLAIMED_TIERS = new HashMap<>();
    private static final Set<String> pinned = new HashSet<>();

    private static int rank;
    private static int reputation;
    private static int nextRankThreshold;
    private static int unclaimedReputation;
    private static int shopRerollCost;
    private static int trialKind;
    private static int trialGodGate;
    private static int bestGodLevel;

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

    /** Drops everything the last server sent, restoring the local config's balance tables. */
    public static void clear() {
        MilestoneRankLadder.restoreLocal();
        MilestoneRegistry.restoreLocal();
        VALUES.clear();
        CLAIMED_TIERS.clear();
        pinned.clear();
        rank = 0;
        reputation = 0;
        nextRankThreshold = 0;
        unclaimedReputation = 0;
        shopRerollCost = 0;
        trialKind = 0;
        trialGodGate = 0;
        bestGodLevel = 0;
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

    /** Reputation banked on a milestone but not yet collected at Mr. Greedy. */
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

    public static Set<String> getPinned() {
        return pinned;
    }

    public static void pin(String milestoneId) {
        pinned.add(milestoneId);
    }

    public static void unpin(String milestoneId) {
        pinned.remove(milestoneId);
    }

    public static void setPinned(Set<String> pins) {
        pinned.clear();
        pinned.addAll(pins);
    }

    public static void setStatus(int newRank, int newReputation, int newNextRankThreshold,
                                 int newUnclaimedReputation, int newShopRerollCost) {
        rank = newRank;
        reputation = newReputation;
        nextRankThreshold = newNextRankThreshold;
        unclaimedReputation = newUnclaimedReputation;
        shopRerollCost = newShopRerollCost;
    }

    /** Mirrors the trial half of the status packet; {@code kind} is a Kind ordinal plus one, or 0. */
    public static void setTrialStatus(int kind, int godGate, int bestGod) {
        trialKind = kind;
        trialGodGate = godGate;
        bestGodLevel = bestGod;
    }

    public static boolean hasTrial() {
        return trialKind > 0;
    }

    /** True when the trial for the next rank is a hyper vault. */
    public static boolean isTrialHyper() {
        return trialKind == 2;
    }

    public static int getTrialGodGate() {
        return trialGodGate;
    }

    public static int getBestGodLevel() {
        return bestGodLevel;
    }

    /** Whether a trial exists, the reputation bar is full and any god gate is met. */
    public static boolean isTrialReady() {
        return hasTrial() && reputation >= nextRankThreshold && bestGodLevel >= trialGodGate;
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

    /** Greedy-ticket price of the next greed shop reroll. */
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
