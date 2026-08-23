package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.network.message.ServerboundGreedChallengeActionMessage;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.GreedChallengeOffers;

import java.util.List;

/** Re-tests the crystal a slot names against the player's rank when a challenge is bought. */
@Mixin(value = ServerboundGreedChallengeActionMessage.class, remap = false)
public class MixinServerboundGreedChallengeActionMessage {
    @Inject(method = "handleAccept", at = @At("HEAD"), cancellable = true)
    private static void gateAcceptOnRank(ServerPlayer player, GreedTree tree, PlayerGreedTreeData treeData,
                                         int slotIndex, CallbackInfo ci) {
        if (woldsvaults$isLocked(player, tree, slotIndex, "accept")) {
            ci.cancel();
        }
    }

    @Inject(method = "handleRebuy", at = @At("HEAD"), cancellable = true)
    private static void gateRebuyOnRank(ServerPlayer player, GreedTree tree, PlayerGreedTreeData treeData,
                                        int slotIndex, CallbackInfo ci) {
        if (woldsvaults$isLocked(player, tree, slotIndex, "rebuy")) {
            ci.cancel();
        }
    }

    private static boolean woldsvaults$isLocked(ServerPlayer player, GreedTree tree, int slotIndex, String action) {
        List<GreedChallengeSlot> slots = tree.getChallengeSlots();
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return false;
        }
        String crystalId = slots.get(slotIndex).getChallengeId();
        if (GreedChallengeOffers.isUnlocked(crystalId, tree.getGreedTier())) {
            return false;
        }
        WoldsVaults.LOGGER.warn("Refused greed challenge {} of '{}' from {}: not unlocked at greed rank {}",
                action, crystalId, player.getGameProfile().getName(), tree.getGreedTier());
        return true;
    }
}
