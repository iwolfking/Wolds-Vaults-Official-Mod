package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.config.greed.GreedTraderConfig;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.init.ModConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(value = GreedTree.class, remap = false)
public abstract class MixinGreedTree {
    @Shadow
    protected List<GreedChallengeSlot> challengeSlots;
    @Shadow
    protected int greedTier;
    @Shadow
    protected boolean challengesPopulated;

    @Shadow
    public abstract void populateChallenges(int greedTier);

    @Shadow
    protected Set<String> completedChallengeIds;

    @Shadow
    protected List<GreedChallengeSlot> challengeHistory;

    /**
     * @author iwolfking
     * @reason Completing and abandoning challenges can refresh an additional challenge to complete *before* GT13, completing all challenges grants access to do all challenges again
     */
    @Overwrite
    public void checkChallengesCycleReset() {
        if (!this.challengeSlots.isEmpty()) {
            for (GreedChallengeSlot slot : this.challengeSlots) {
                if (slot.isAvailable() || slot.isAttempted()) {
                    return;
                }
            }

            this.challengeSlots.clear();
            if (this.greedTier >= 1) {
                this.challengesPopulated = false;
                this.populateChallenges(this.greedTier);
            }

            challengeHistory.removeIf(GreedChallengeSlot::isSkipped);

            if(completedChallengeIds.size() == ModConfigs.GREED_TRADER.getChallenges().size()) {
                this.challengeHistory.clear();
                this.challengeSlots.clear();
                this.completedChallengeIds.clear();
                this.challengesPopulated = false;
                this.populateChallenges(this.greedTier);
            }

        }
    }

    @WrapOperation(method = "populateTierChallenge", at = @At(value = "INVOKE", target = "Liskallia/vault/config/greed/GreedTraderConfig;getEligibleChallenges(I)Ljava/util/List;"))
    public List<GreedChallengeEntry> removeCompletedChallengesFromPopulating(GreedTraderConfig instance, int greedTier, Operation<List<GreedChallengeEntry>> original) {
        List<GreedChallengeEntry> entries = original.call(instance, greedTier);
        entries.removeIf(challengeEntry -> completedChallengeIds.contains(challengeEntry.getChallengeCrystalId()));
        entries.removeIf(challengeEntry -> isRankLocked(challengeEntry, greedTier));
        return entries;
    }

    @WrapOperation(method = "populateCatchupChallenge", at = @At(value = "INVOKE", target = "Liskallia/vault/config/greed/GreedTraderConfig;getChallenges()Ljava/util/List;"))
    public List<GreedChallengeEntry> removeRankLockedChallengesFromCatchup(GreedTraderConfig instance, Operation<List<GreedChallengeEntry>> original) {
        List<GreedChallengeEntry> entries = new ArrayList<>(original.call(instance));
        entries.removeIf(challengeEntry -> isRankLocked(challengeEntry, this.greedTier));
        return entries;
    }

    /**
     * Re-gates challenge crystals on the rank tagged on their milestone instead of the config's
     * {@code minTier}. Applied at the offer path, so a locked crystal is never rolled into a slot
     * and therefore can never be accepted or rebought. The greed tier int is the rank, the same
     * interim convention the rest of the rework uses. Crystals with no milestone - only
     * {@code ultra_hard} - carry rank 0 and keep their config gate.
     */
    private static boolean isRankLocked(GreedChallengeEntry entry, int rank) {
        return MilestoneRegistry.getChallengeRequiredRank(entry.getChallengeCrystalId()) > rank;
    }
}
