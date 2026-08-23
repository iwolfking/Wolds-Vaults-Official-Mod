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

/** Inverts the greed effect rebuild: gear attribute and stat boost node modifiers are removed. */
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
