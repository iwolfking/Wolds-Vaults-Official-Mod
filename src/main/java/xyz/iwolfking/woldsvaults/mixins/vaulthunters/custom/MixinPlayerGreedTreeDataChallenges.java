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
 * Keeps accepted greed challenges alive across a rank change.
 *
 * <p>Both of base's rank writes wipe the challenge slot list before flagging it for repopulation.
 * That was survivable while a player could only ever hold one challenge, but Mr. Greedy now offers
 * the whole unlocked set at once and a player can be carrying several crystals: the slot is what
 * carries the crystal's uuid, and both {@code VaultPortalBlock} and {@code VaultCrystalItem} refuse
 * a challenge crystal whose slot is missing or not {@code ATTEMPTED}, so wiping the list turns
 * every crystal in the player's inventory into a brick.</p>
 *
 * <p>Each wipe is neutralised by handing base a throwaway list to clear. The
 * {@code setChallengesPopulated(false)} that follows is left alone, so the slots are still
 * refreshed on the next visit to the trader - the rework's repopulate is additive and prunes
 * anything the new rank no longer unlocks, which is what a rank change actually needs to happen.</p>
 *
 * <p>Filed as its own mixin rather than added to {@code MixinPlayerGreedTreeData} so that the
 * challenge-offer rework and the reputation/rank work on the same class stay separable.</p>
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
