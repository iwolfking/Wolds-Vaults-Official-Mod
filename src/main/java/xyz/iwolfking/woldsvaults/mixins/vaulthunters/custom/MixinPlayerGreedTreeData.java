package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
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

    @Unique
    private boolean woldsvaults$alignInProgress;

    /**
     * Keeps rank and reputation coherent whenever a rank is force-set.
     *
     * <p>The greed rework reads the reputation bar as a position inside the current rank's band:
     * {@code (reputation - threshold(rank)) / (threshold(rank + 1) - threshold(rank))}. Base's
     * {@code setGreedTier}, which is what {@code /the_vault greed set_tier} calls, moves the tier
     * and zeroes the reputation, so a player force-set to any rank above the first lands below
     * their own band floor and the bar reads empty until they earn back the whole band. Landing
     * them exactly on the floor of the band is the only assignment that makes both numbers agree,
     * and it is what a legitimately earned rank-up leaves behind anyway.</p>
     *
     * <p>The reputation write is exact rather than additive so that repeated force-sets are
     * idempotent, and it goes through the data class's own setter so the client sync fires. The
     * re-entry guard is defensive: nothing in base calls back into {@code setGreedTier} from a
     * reputation write today, but a future rank-up hook on the reputation setter would.</p>
     */
    @Inject(method = "setGreedTier", at = @At("TAIL"))
    private void alignReputationToRankFloor(ServerPlayer player, int tier, CallbackInfo ci) {
        if (this.woldsvaults$alignInProgress) {
            return;
        }
        this.woldsvaults$alignInProgress = true;
        try {
            this.setGreedReputation(player, MilestoneRankLadder.getThreshold(tier));
        } finally {
            this.woldsvaults$alignInProgress = false;
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
