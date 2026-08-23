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

/**
 * Puts a player on the greed ladder the moment the Herald falls.
 *
 * <p>Base's {@code onHeraldCompleted} only records the flag; the greed tier stays at 0 until
 * something else moves it, and under the rework nothing does - the first trial row is the climb to
 * Scavenger 2, so rank 1 has no trial to earn it. That left the whole ladder unreachable and every
 * rank-facing surface reading "Unranked" forever. Tier 0 now means exactly one thing: the Herald
 * has not been beaten yet.</p>
 *
 * <p>The write goes through {@code setGreedTier}, so the addon's own coherence hook on that method
 * floors reputation to the new band (rank 1's floor is 0, so nothing is gained or lost) and the
 * client sync fires. It is guarded on the tier still being 0 so a re-completion - or a save that
 * already climbed - can never drag a ranked player back down to Scavenger 1.</p>
 */
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
