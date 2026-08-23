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
    private static volatile boolean audited;

    private GreedChallengeOffers() {
    }

    /**
     * Re-arms the gate audit so a config reload is checked again. Called from
     * {@link MilestoneRegistry#load}, which is where the rank tags are rebuilt.
     */
    public static void resetAudit() {
        audited = false;
    }

    /**
     * Reports every crystal whose config {@code minTier} sits above its milestone's rank tag.
     *
     * <p>The two gates are ANDed, so the higher one wins: a crystal tagged for Looter 1 but
     * configured with {@code minTier} 5 is quietly unbuyable until Looter 2, while the reputation
     * its milestone pays - which is derived from the tag - stays priced for Looter 1. The mismatch
     * has no symptom in game beyond a crystal that never appears, so it is called out here rather
     * than left to be rediscovered by hand.</p>
     *
     * <p>Runs once per registry load, on the first unlock question asked after it, because
     * {@code ModConfigs.GREED_TRADER} belongs to the base mod and is not guaranteed to have been
     * read by the time the addon installs its own configs. Crystals with no milestone are skipped:
     * they carry no tag, and {@code minTier} is deliberately their only gate.</p>
     */
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

    /**
     * Whether the given rank may be offered and sold this challenge entry.
     */
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
