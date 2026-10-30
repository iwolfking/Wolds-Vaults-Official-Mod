package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedNodeHelper;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.ServerVaults;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ServerVaults.class, remap = false)
public class MixinServerVaults {
    @ModifyVariable(method = "checkChallengeCompletion", at = @At("STORE"), name = "reputationReward")
    private static int adjustGreedChallengeReward(int reputationReward, @Local(name = "activeSlot") GreedChallengeSlot activeSlot, @Local(name = "greedTree") GreedTree greedTree) {
        int greedTier = greedTree.getGreedTier();
        int baseReward = (int) ((greedTier * 25) * (1.0 + (greedTier * 0.05)));

        GreedChallengeEntry entry = ModConfigs.GREED_TRADER.getChallengeEntryById(activeSlot.getChallengeId());
        if (entry != null) {
            int minGreedTier = entry.getMinTier();
            int maxRealTier = Math.min(greedTier, 12);
            int tierDifferential = Math.abs(minGreedTier - maxRealTier);

            if (tierDifferential > 0) {
                double penaltyPerTier = 0.1;
                double penalty = Math.max(0.5, 1.0 - (tierDifferential * penaltyPerTier));

                return GreedNodeHelper.applyGreedReputationMultiplier(greedTree, (int) (baseReward * penalty));
            }
        }

        return GreedNodeHelper.applyGreedReputationMultiplier(greedTree, baseReward);
    }
}
