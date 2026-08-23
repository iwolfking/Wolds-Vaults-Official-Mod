package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.config.greed.GreedTraderConfig;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.init.ModConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.GreedChallengeOffers;

import java.util.HashSet;
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
     * Offers every unlocked challenge at once instead of one rolled slot. Accepted and completed slots
     * survive; abandoned ones, and those whose crystal is no longer unlocked, are dropped.
     */
    @Inject(method = "populateChallenges", at = @At("HEAD"), cancellable = true)
    private void offerEveryUnlockedChallenge(int greedTier, CallbackInfo ci) {
        ci.cancel();
        this.challengesPopulated = true;
        GreedTraderConfig config = ModConfigs.GREED_TRADER;
        if (config == null) {
            WoldsVaults.LOGGER.warn("Greed trader config is not loaded; offering no greed challenges");
            return;
        }
        this.challengeSlots.removeIf(GreedChallengeSlot::isSkipped);
        this.challengeSlots.removeIf(slot -> slot.isAvailable()
                && !GreedChallengeOffers.isUnlocked(slot.getChallengeId(), greedTier));
        Set<String> offered = new HashSet<>();
        for (GreedChallengeSlot slot : this.challengeSlots) {
            if (slot.getChallengeId() != null) {
                offered.add(slot.getChallengeId());
            }
        }
        for (GreedChallengeEntry entry : config.getChallenges()) {
            String crystalId = entry.getChallengeCrystalId();
            if (crystalId == null || offered.contains(crystalId)) {
                continue;
            }
            if (this.completedChallengeIds.contains(crystalId)) {
                continue;
            }
            if (!GreedChallengeOffers.isUnlocked(entry, greedTier)) {
                continue;
            }
            this.challengeSlots.add(new GreedChallengeSlot(crystalId));
        }
    }

    /** Logs any caller of {@code addGreedReputation} outside the milestone claim path; the grant still runs. */
    @Inject(method = "addGreedReputation", at = @At("HEAD"))
    private void traceUnexpectedReputationGrants(int amount, CallbackInfo ci) {
        if (amount == 0) {
            return;
        }
        String source = StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(name -> !name.equals("iskallia.vault.greed.GreedTree")
                        && !name.equals("iskallia.vault.world.data.PlayerGreedTreeData"))
                .findFirst().orElse("an unidentified caller"));
        if (source.startsWith("xyz.iwolfking.woldsvaults.milestones.Milestones")) {
            return;
        }
        WoldsVaults.LOGGER.warn("Greed reputation {} was granted by {}, outside the milestone claim path; "
                + "reputation is only meant to move when a milestone is collected at Mr. Greedy", amount, source);
    }

    /** Refuses challenge abandoning outright. */
    @Inject(method = "abandonChallenge", at = @At("HEAD"), cancellable = true)
    private void refuseAbandon(int slotIndex, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * @author iwolfking
     * @reason The cycle reset must not wipe completed slots or repopulate an empty list; it fires only
     * once every configured challenge has been completed.
     */
    @Overwrite
    public void checkChallengesCycleReset() {
        if (this.challengeSlots.isEmpty()) {
            return;
        }
        for (GreedChallengeSlot slot : this.challengeSlots) {
            if (slot.isAvailable() || slot.isAttempted()) {
                return;
            }
        }
        this.challengeHistory.removeIf(GreedChallengeSlot::isSkipped);
        if (ModConfigs.GREED_TRADER == null
                || this.completedChallengeIds.size() < ModConfigs.GREED_TRADER.getChallenges().size()) {
            return;
        }
        this.challengeHistory.clear();
        this.challengeSlots.clear();
        this.completedChallengeIds.clear();
        this.challengesPopulated = false;
        this.populateChallenges(this.greedTier);
    }
}
