package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.customisation.CustomisationDiscoveryHelper;
import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.milestones.Milestones;

import java.util.UUID;

@Mixin(value = ServerVaults.class, remap = false)
public class MixinServerVaults {
    /**
     * Replaces the greed challenge completion payout. Finishing a challenge crystal no longer
     * grants reputation directly: it completes the crystal's milestone, whose reputation the player
     * collects at Mr. Greedy like every other milestone. Everything else the base path did is kept
     * verbatim - the slot is still marked complete so {@code completedChallengeIds} keeps growing,
     * the tree is still saved and synced, customisation discoveries still run, and the completion
     * is still announced - only the reputation grant and its chat suffix are gone.
     */
    @Inject(method = "checkChallengeCompletion", at = @At("HEAD"), cancellable = true)
    private static void completeChallengeWithoutReputation(Vault vault, CallbackInfo ci) {
        ci.cancel();
        if (!vault.has(Vault.OWNER) || !vault.has(Vault.STATS)) {
            return;
        }
        UUID ownerId = vault.get(Vault.OWNER);
        UUID vaultId = vault.get(Vault.ID);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        PlayerGreedTreeData treeData = PlayerGreedTreeData.get(server);
        GreedTree greedTree = treeData.getGreedTree(ownerId);
        if (greedTree == null) {
            return;
        }
        GreedChallengeSlot activeSlot = greedTree.getActiveChallengeSlot();
        if (activeSlot == null || !vaultId.equals(activeSlot.getChallengeVaultId())) {
            return;
        }
        StatsCollector stats = vault.get(Vault.STATS);
        StatCollector ownerStats = stats.get(ownerId);
        if (ownerStats == null || ownerStats.getCompletion() != Completion.COMPLETED) {
            return;
        }
        String challengeId = activeSlot.getChallengeId();
        greedTree.completeChallenge(greedTree.getActiveChallengeSlotIndex());
        treeData.setDirty();
        String challengeName = "Unknown Challenge";
        if (ModConfigs.GREED_TRADER != null) {
            GreedChallengeEntry challengeEntry = ModConfigs.GREED_TRADER.getChallengeEntryById(challengeId);
            if (challengeEntry != null && challengeEntry.getDisplayName() != null) {
                challengeName = challengeEntry.getDisplayName();
            }
        }
        ServerPlayer player = server.getPlayerList().getPlayer(ownerId);
        if (player == null) {
            return;
        }
        treeData.sync(player);
        CustomisationDiscoveryHelper.checkDiscoveries(player);
        Milestones.onChallengeCrystalCompleted(player, challengeId);
        player.sendMessage(new TextComponent("Challenge Complete: ").withStyle(ChatFormatting.GOLD)
                .append(new TextComponent(challengeName).withStyle(ChatFormatting.YELLOW)), player.getUUID());
        MutableComponent playerName = player.getName().copy()
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(9974168)));
        MiscUtils.broadcast(new TextComponent("").withStyle(ChatFormatting.WHITE)
                .append(playerName)
                .append(" has completed greed challenge: ")
                .append(new TextComponent(challengeName).withStyle(ChatFormatting.GOLD)));
    }
}
