package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.world.data.PlayerGreedTreeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps accepted greed challenges alive across a rank change: both of base's rank writes are handed a
 * throwaway slot list to clear. The {@code setChallengesPopulated(false)} that follows is left alone.
 */
@Mixin(value = PlayerGreedTreeData.class, remap = false)
public class MixinPlayerGreedTreeDataChallenges {
    @Redirect(method = "incrementGreedTier",
            at = @At(value = "INVOKE", target = "Liskallia/vault/greed/GreedTree;getChallengeSlots()Ljava/util/List;"))
    private List<GreedChallengeSlot> keepChallengesOnRankUp(GreedTree greedTree) {
        return new ArrayList<>();
    }

    @Redirect(method = "setGreedTier",
            at = @At(value = "INVOKE", target = "Liskallia/vault/greed/GreedTree;getChallengeSlots()Ljava/util/List;"))
    private List<GreedChallengeSlot> keepChallengesOnRankSet(GreedTree greedTree) {
        return new ArrayList<>();
    }
}
