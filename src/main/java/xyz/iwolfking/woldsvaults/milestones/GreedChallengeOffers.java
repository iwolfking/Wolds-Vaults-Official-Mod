package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.init.ModConfigs;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The unlock rule for Mr. Greedy's challenge crystals: the greed tier must clear both the rank
 * tagged on the crystal's milestone and the config's {@code minTier}/{@code maxTier} window.
 */
public final class GreedChallengeOffers {
    private static volatile boolean audited;

    private GreedChallengeOffers() {
    }

    /** Re-arms the gate audit so a config reload is checked again. */
    public static void resetAudit() {
        audited = false;
    }

    /** Logs crystals whose {@code minTier} sits above their milestone's rank tag, once per load. */
    private static void auditGates() {
        if (audited || ModConfigs.GREED_TRADER == null) {
            return;
        }
        audited = true;
        for (GreedChallengeEntry entry : ModConfigs.GREED_TRADER.getChallenges()) {
            if (entry == null) {
                continue;
            }
            int requiredRank = MilestoneRegistry.getChallengeRequiredRank(entry.getChallengeCrystalId());
            if (requiredRank > 0 && entry.getMinTier() > requiredRank) {
                WoldsVaults.LOGGER.warn("Challenge crystal '{}' is gated at minTier {} in greed_trader.json but its milestone is tagged rank {}; it stays locked until rank {}",
                        entry.getChallengeCrystalId(), entry.getMinTier(), requiredRank, entry.getMinTier());
            }
        }
    }

    public static boolean isUnlocked(GreedChallengeEntry entry, int rank) {
        auditGates();
        if (entry == null) {
            return false;
        }
        if (!entry.isEligibleForTier(rank)) {
            return false;
        }
        return MilestoneRegistry.getChallengeRequiredRank(entry.getChallengeCrystalId()) <= rank;
    }

    /** Same test, resolved from a crystal id against the live greed trader config. */
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

    /** Reputation of the next uncollected tier of a crystal's milestone, or 0 when it has none. */
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
