package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.network.message.ServerboundGreedChallengeActionMessage;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.GreedChallengeOffers;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;

import java.util.List;

/**
 * Gates and narrates what Mr. Greedy hands over. Taking a challenge re-tests the crystal against the
 * player's rank, because the offer list is only rebuilt when the rank moves. Rebuying is deliberately
 * not rank-tested: the slot was accepted at a rank that allowed it, and a later demotion must not
 * strand a crystal the player still owes a run. Every refusal now reaches the player, since the base
 * handlers return in silence and the screen draws a live button either way.
 */
@Mixin(value = ServerboundGreedChallengeActionMessage.class, remap = false)
public class MixinServerboundGreedChallengeActionMessage {
    @Inject(method = "handleAccept", at = @At("HEAD"), cancellable = true)
    private static void gateAcceptOnRank(ServerPlayer player, GreedTree tree, PlayerGreedTreeData treeData,
                                         int slotIndex, CallbackInfo ci) {
        GreedChallengeSlot slot = woldsvaults$slot(tree, slotIndex);
        if (slot == null) {
            return;
        }
        if (!slot.isAvailable()) {
            woldsvaults$refuse(player, "You have already taken that challenge.");
            ci.cancel();
            return;
        }
        String crystalId = slot.getChallengeId();
        if (!GreedChallengeOffers.isUnlocked(crystalId, tree.getGreedTier())) {
            int required = woldsvaults$requiredRank(crystalId);
            woldsvaults$refuse(player, required > 0
                    ? "That challenge unlocks at greed rank " + required + "."
                    : "That challenge is not unlocked at your greed rank.");
            WoldsVaults.LOGGER.warn("Refused greed challenge accept of '{}' from {}: not unlocked at greed rank {}",
                    crystalId, player.getGameProfile().getName(), tree.getGreedTier());
            ci.cancel();
            return;
        }
        if (woldsvaults$hasNoCrystal(player, crystalId, "accept")) {
            ci.cancel();
        }
    }

    @Inject(method = "handleRebuy", at = @At("HEAD"), cancellable = true)
    private static void guardRebuy(ServerPlayer player, GreedTree tree, PlayerGreedTreeData treeData,
                                   int slotIndex, CallbackInfo ci) {
        GreedChallengeSlot slot = woldsvaults$slot(tree, slotIndex);
        if (slot == null || !slot.isAttempted()) {
            return;
        }
        if (woldsvaults$hasNoCrystal(player, slot.getChallengeId(), "rebuy")) {
            ci.cancel();
        }
    }

    private static GreedChallengeSlot woldsvaults$slot(GreedTree tree, int slotIndex) {
        List<GreedChallengeSlot> slots = tree.getChallengeSlots();
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return null;
        }
        return slots.get(slotIndex);
    }

    /**
     * Whether the crystal a slot names is missing from the challenge crystal config. Base mutates the
     * slot, and on a rebuy spends the coins, before it discovers this, so the sale is stopped up front.
     */
    private static boolean woldsvaults$hasNoCrystal(ServerPlayer player, String crystalId, String action) {
        if (ModConfigs.CHALLENGE_CRYSTALS != null && crystalId != null
                && ModConfigs.CHALLENGE_CRYSTALS.getChallenge(crystalId).isPresent()) {
            return false;
        }
        woldsvaults$refuse(player, "That challenge crystal is unavailable; nothing was taken.");
        WoldsVaults.LOGGER.error("Refused greed challenge {} of '{}' from {}: challenge_crystal.json has no such "
                + "challenge, so no crystal could be built", action, crystalId, player.getGameProfile().getName());
        return true;
    }

    private static int woldsvaults$requiredRank(String crystalId) {
        int required = MilestoneRegistry.getChallengeRequiredRank(crystalId);
        if (ModConfigs.GREED_TRADER != null) {
            GreedChallengeEntry entry = ModConfigs.GREED_TRADER.getChallengeEntryById(crystalId);
            if (entry != null) {
                required = Math.max(required, entry.getMinTier());
            }
        }
        return required;
    }

    private static void woldsvaults$refuse(ServerPlayer player, String reason) {
        player.displayClientMessage(new TextComponent(reason).withStyle(ChatFormatting.RED), false);
    }
}
