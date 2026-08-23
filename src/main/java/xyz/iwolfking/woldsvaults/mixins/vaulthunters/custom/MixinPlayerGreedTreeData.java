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

    /** Records the reputation and rank a force-set is about to overwrite, for the TAIL hooks below. */
    @Inject(method = "setGreedTier", at = @At("HEAD"))
    private void captureReputationBeforeTierChange(ServerPlayer player, int tier, CallbackInfo ci) {
        this.woldsvaults$reputationBeforeTierChange = this.getGreedReputation(player);
        this.woldsvaults$tierBeforeTierChange = this.getGreedTier(player);
    }

    /**
     * Raises reputation to the new rank's band floor after a force-set, keeping anything already above
     * it. The write goes through the data class's own setter, so the client sync fires.
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

    /** Restocks Mr. Greedy's shop whenever the rank actually rises, whatever moved it. */
    @Inject(method = "setGreedTier", at = @At("TAIL"))
    private void restockShopOnRankUp(ServerPlayer player, int tier, CallbackInfo ci) {
        if (tier > this.woldsvaults$tierBeforeTierChange && player.server != null) {
            PlayerGreedTraderData.get(player.server).rerollOffers(player);
        }
    }

    /** Blocks the two quest writes reachable without a network message: the greed command and lazy population. */
    @Inject(method = "completeQuest", at = @At("HEAD"), cancellable = true)
    private void cancelQuestCompletion(ServerPlayer player, int slotIndex, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "populateQuestsIfNeeded", at = @At("HEAD"), cancellable = true)
    private void cancelQuestPopulation(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }
}
