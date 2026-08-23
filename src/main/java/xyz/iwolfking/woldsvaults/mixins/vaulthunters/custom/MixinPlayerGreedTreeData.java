package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.world.data.PlayerGreedTraderData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

@Mixin(value = PlayerGreedTreeData.class, remap = false)
public abstract class MixinPlayerGreedTreeData {
    @Shadow
    public abstract void setGreedReputation(ServerPlayer player, int reputation);

    @Shadow
    public abstract int getGreedReputation(Player player);

    @Shadow
    public abstract int getGreedTier(Player player);

    @Unique
    private boolean woldsvaults$alignInProgress;

    @Unique
    private int woldsvaults$reputationBeforeTierChange;

    @Unique
    private int woldsvaults$tierBeforeTierChange;

    /**
     * Records the reputation a force-set is about to destroy and the rank it is leaving. Base's
     * {@code setGreedTier} zeroes the reputation on its second line and has already overwritten the
     * tier by the time the TAIL hooks below run, so neither old value can be read there.
     */
    @Inject(method = "setGreedTier", at = @At("HEAD"))
    private void captureReputationBeforeTierChange(ServerPlayer player, int tier, CallbackInfo ci) {
        this.woldsvaults$reputationBeforeTierChange = this.getGreedReputation(player);
        this.woldsvaults$tierBeforeTierChange = this.getGreedTier(player);
    }

    /**
     * Keeps rank and reputation coherent whenever a rank is force-set.
     *
     * <p>The greed rework reads the reputation bar as a position inside the current rank's band:
     * {@code (reputation - threshold(rank)) / (threshold(rank + 1) - threshold(rank))}. Base's
     * {@code setGreedTier}, which is what {@code /the_vault greed set_tier} calls, moves the tier
     * and zeroes the reputation, so a player force-set to any rank above the first lands below
     * their own band floor and the bar reads empty until they earn back the whole band.</p>
     *
     * <p>The write raises the reputation to the new band's floor rather than assigning it, so
     * reputation earned above that floor survives a force-set instead of being thrown away. It is
     * still idempotent - a repeated force-set to the same rank finds the floor already met and
     * writes the same number back - and it goes through the data class's own setter so the client
     * sync fires. A force-set DOWN therefore leaves a player sitting above their new band, which is
     * the intended trade: reputation is earned, ranks are the thing the command is moving.</p>
     *
     * <p>The re-entry guard is defensive: nothing in base calls back into {@code setGreedTier} from
     * a reputation write today, but a future rank-up hook on the reputation setter would.</p>
     */
    @Inject(method = "setGreedTier", at = @At("TAIL"))
    private void alignReputationToRankFloor(ServerPlayer player, int tier, CallbackInfo ci) {
        if (this.woldsvaults$alignInProgress) {
            return;
        }
        this.woldsvaults$alignInProgress = true;
        try {
            this.setGreedReputation(player, Math.max(this.woldsvaults$reputationBeforeTierChange,
                    MilestoneRankLadder.getThreshold(tier)));
        } finally {
            this.woldsvaults$alignInProgress = false;
        }
    }

    /**
     * Restocks Mr. Greedy's shop on every rank-up, whatever moved the rank.
     *
     * <p>Base only rerolls from {@code incrementGreedTier}, which the rework never calls: a trial
     * win and {@code /the_vault greed set_tier} both go through {@code setGreedTier}, which reroll
     * nothing. The shop's tier pools are cumulative, so a player who ranked up without this kept
     * staring at their old tier's offers until they paid for a reroll. Guarded on the rank actually
     * having risen, so a force-set down or a repeat set to the same rank leaves the shop alone.</p>
     */
    @Inject(method = "setGreedTier", at = @At("TAIL"))
    private void restockShopOnRankUp(ServerPlayer player, int tier, CallbackInfo ci) {
        if (tier > this.woldsvaults$tierBeforeTierChange && player.server != null) {
            PlayerGreedTraderData.get(player.server).rerollOffers(player);
        }
    }

    /**
     * Blocks the two quest writes that are reachable without a network message: the greed command's
     * {@code complete_quest}, which paid reputation, and the lazy population that ran whenever the
     * trader's quest tab was opened. The quest system is retired, and the addon's old auto-refresh
     * inject on {@code completeQuest} went with it.
     */
    @Inject(method = "completeQuest", at = @At("HEAD"), cancellable = true)
    private void cancelQuestCompletion(ServerPlayer player, int slotIndex, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "populateQuestsIfNeeded", at = @At("HEAD"), cancellable = true)
    private void cancelQuestPopulation(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }
}
