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
     * Offers every challenge the player's rank has unlocked at once instead of a single rolled
     * slot, so the trader's challenge tab is a menu the player picks from rather than a one-shot
     * offer they have to abandon to change.
     *
     * <p>The reimplementation is additive: it never clears the slot list the way base does, so
     * challenges already accepted keep their {@code ATTEMPTED} state - and with it the crystal
     * uuid the vault portal matches on - across a repopulate. Available slots whose crystal is no
     * longer unlocked are dropped, which is what keeps a force-set rank downwards honest, and
     * abandoned slots are dropped because abandoning is no longer a thing. Completed slots stay in
     * the list so the tab keeps showing them ticked off.</p>
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

    /**
     * Names anything that still pays greed reputation outside the milestone claim path.
     *
     * <p>{@code GreedTree.addGreedReputation} is the single chokepoint every grant in the game
     * funnels through, base and addon alike. After the rework the only legitimate callers are a
     * milestone being collected at Mr. Greedy and the Greedy Ticket item; a challenge crystal, a
     * quest, an assassin kill and a tier crossing all used to arrive here and none of them should
     * any more. This does not block the grant - a silent refusal would be worse to debug than a
     * wrong number - it logs the offending caller so the path can be closed at its source.</p>
     */
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
        if (source.startsWith("xyz.iwolfking.woldsvaults.milestones.Milestones")
                || source.startsWith("xyz.iwolfking.woldsvaults.items.GreedyTicketItem")) {
            return;
        }
        WoldsVaults.LOGGER.warn("Greed reputation {} was granted by {}, outside the milestone claim path; "
                + "reputation is only meant to move when a milestone is collected at Mr. Greedy", amount, source);
    }

    /**
     * Abandoning is retired. Every unlocked challenge is on the tab permanently, so the only thing
     * abandoning could still do is delete an offer the player is meant to keep; a player who wants
     * a different challenge simply accepts a different one, and a player who lost their crystal
     * rebuys it.
     */
    @Inject(method = "abandonChallenge", at = @At("HEAD"), cancellable = true)
    private void refuseAbandon(int slotIndex, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * @author iwolfking
     * @reason The whole unlocked challenge set is offered at once, so the cycle reset is only ever
     * about starting the set over: it must not wipe the completed slots the tab renders as ticked
     * off, and it must not repopulate an empty list every time the last available challenge is
     * taken. It now fires only once every configured challenge has been completed.
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
