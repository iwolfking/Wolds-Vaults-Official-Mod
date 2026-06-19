package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.client.data.ClientGreedTreeData;
import iskallia.vault.client.gui.screen.GreedTraderScreen;
import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedNodeHelper;
import iskallia.vault.init.ModConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = GreedTraderScreen.Challenges.class, remap = false)
public class MixinGreedTraderScreen$Challenges {

    @Shadow
    private int selectedChallenge;

    @Inject(method = "getChallengeReputationReward", at = @At("TAIL"), cancellable = true)
    private void alterChallengeRepReward(CallbackInfoReturnable<Integer> cir, @Local(name = "greedTier") int greedTier) {
        List<GreedChallengeSlot> challengeSlots = ClientGreedTreeData.getChallengeSlots();
        if(selectedChallenge == -1) {
            return;
        }

        GreedChallengeSlot selectedSlot = challengeSlots.get(this.selectedChallenge);
        int baseReward = (int) ((greedTier * 25) * (1.0 + (greedTier * 0.05)));
        if (selectedSlot != null && selectedSlot.getChallengeId() != null) {
            GreedChallengeEntry entry = ModConfigs.GREED_TRADER.getChallengeEntryById(selectedSlot.getChallengeId());
            if (entry != null) {
                int minGreedTier = entry.getMinTier();
                int maxRealTier = Math.min(ClientGreedTreeData.getGreedTier(), 12);
                int tierDifferential = Math.abs(minGreedTier - maxRealTier);

                if (tierDifferential > 0) {
                    double penaltyPerTier = 0.1;
                    double penalty = Math.max(0.5, 1.0 - (tierDifferential * penaltyPerTier));

                    cir.setReturnValue(GreedNodeHelper.applyGreedReputationMultiplier(ClientGreedTreeData.getTree(), (int) (baseReward * penalty)));
                    return;
                }
            }
        }

        cir.setReturnValue(GreedNodeHelper.applyGreedReputationMultiplier(ClientGreedTreeData.getTree(), baseReward));
    }
}
