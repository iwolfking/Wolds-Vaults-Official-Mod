package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.init.ModConfigs;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The unlock rule for Mr. Greedy's challenge crystals. Mr. Greedy offers every challenge the
 * player's rank has unlocked at once, so the same predicate has to answer for the offer list, for
 * the purchase the client sends back, and for pruning offers when a rank is force-set downwards -
 * this is the one place it lives.
 *
 * <p>A crystal is unlocked when the greed tier clears both gates: the rank tagged on the crystal's
 * milestone, and the config's own {@code minTier}/{@code maxTier} window. The rank gate is the
 * rework's; the config gate is kept on top of it so that {@code ultra_hard}, the one offered
 * crystal with no milestone and therefore rank 0, still opens on its configured tier.</p>
 */
public final class GreedChallengeOffers {
    private GreedChallengeOffers() {
    }

    /**
     * Whether the given rank may be offered and sold this challenge entry.
     */
    public static boolean isUnlocked(GreedChallengeEntry entry, int rank) {
        if (entry == null) {
            return false;
        }
        if (!entry.isEligibleForTier(rank)) {
            return false;
        }
        return MilestoneRegistry.getChallengeRequiredRank(entry.getChallengeCrystalId()) <= rank;
    }

    /**
     * Same test, resolved from a crystal id against the live greed trader config.
     */
    public static boolean isUnlocked(String challengeCrystalId, int rank) {
        if (challengeCrystalId == null) {
            return false;
        }
        if (ModConfigs.GREED_TRADER == null) {
            WoldsVaults.LOGGER.warn("Greed trader config is not loaded; treating challenge '{}' as locked", challengeCrystalId);
            return false;
        }
        return isUnlocked(ModConfigs.GREED_TRADER.getChallengeEntryById(challengeCrystalId), rank);
    }

    /**
     * Reputation a challenge crystal is worth to the given player state: the reputation of the
     * next tier of its milestone that has not been collected yet, which for the one-tier challenge
     * milestones is simply what the milestone pays. Crystals with no milestone are worth nothing.
     *
     * @param claimedTiers   tiers of that milestone whose reputation the player already collected
     * @param completedTiers tiers of that milestone the player has finished
     */
    public static int getReputation(String challengeCrystalId, int claimedTiers, int completedTiers) {
        MilestoneDefinition definition = MilestoneRegistry.getByChallengeCrystal(challengeCrystalId);
        if (definition == null) {
            return 0;
        }
        int completed = Math.max(0, completedTiers);
        int claimed = Math.min(Math.max(0, claimedTiers), completed);
        return definition.getReputation(Math.min(claimed, definition.getTierCount() - 1));
    }
}
