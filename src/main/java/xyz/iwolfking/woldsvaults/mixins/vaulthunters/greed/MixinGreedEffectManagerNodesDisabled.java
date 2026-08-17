package xyz.iwolfking.woldsvaults.mixins.vaulthunters.greed;

import iskallia.vault.greed.GreedNode;
import iskallia.vault.greed.effect.GreedEffectManager;
import iskallia.vault.greed.node.GearAttributeGreedNode;
import iskallia.vault.greed.node.StatBoostGreedNode;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Design decision D32: on login the base mod re-applies every unlocked greed node's effect. Two
 * node types put real vanilla attribute modifiers on the player ({@code greed_gear_attribute} via
 * {@code GearAttributeSkill}, {@code greed_stat_boost} directly), so simply skipping the rebuild
 * would leave any already-applied modifier in place. This replaces the rebuild with the inverse
 * pass: those two node types have their modifiers REMOVED, so a player who logs in after the update
 * comes back clean.
 *
 * <p>Only the two modifier-carrying types are touched. {@code greed_skill_point}'s removal hook
 * subtracts granted skill points, which would be a data change rather than a neutralisation, so it
 * is deliberately skipped - the greed skill points a player was already given stay given, and the
 * node's own rebuild hook is a no-op in the base mod anyway.
 */
@Mixin(value = GreedEffectManager.class, remap = false)
public abstract class MixinGreedEffectManagerNodesDisabled {
    @Inject(method = "rebuildEffects(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$stripGreedNodeEffects(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
        MinecraftServer server = player.getServer();
        if (server == null || ModConfigs.GREED_NODES == null) {
            WoldsVaults.LOGGER.error("Skipping greed node effect strip for {}: server {} greed node config {}.",
                    player.getGameProfile().getName(), server, ModConfigs.GREED_NODES);
            return;
        }
        PlayerGreedTreeData greedData = PlayerGreedTreeData.get(server);
        for (String nodeId : greedData.getUnlockedNodes(player.getUUID())) {
            GreedNode node = ModConfigs.GREED_NODES.getNode(nodeId);
            if (node instanceof GearAttributeGreedNode || node instanceof StatBoostGreedNode) {
                node.onRemove(player);
            }
        }
    }
}
