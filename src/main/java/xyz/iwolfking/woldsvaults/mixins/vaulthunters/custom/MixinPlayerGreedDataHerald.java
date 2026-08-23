package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.world.data.PlayerGreedData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

/** Sets greed tier 1 when the Herald falls, through {@code setGreedTier} and only if the tier is still 0. */
@Mixin(value = PlayerGreedData.class, remap = false)
public class MixinPlayerGreedDataHerald {

    @Inject(method = "onHeraldCompleted", at = @At("TAIL"))
    private void joinGreedLadder(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.server == null) {
            return;
        }
        PlayerGreedTreeData treeData = PlayerGreedTreeData.get(serverPlayer.server);
        if (treeData.getGreedTier(serverPlayer) != 0) {
            return;
        }
        treeData.setGreedTier(serverPlayer, MilestoneRankLadder.FIRST_RANK);
        WoldsVaults.LOGGER.info("{} beat the Herald; greed rank set to {}",
                serverPlayer.getGameProfile().getName(), MilestoneRankLadder.FIRST_RANK);
    }
}
