package xyz.iwolfking.woldsvaults.milestones.trials;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

/** Server-side gate on "Take Trial": reputation for the next rank, plus any god-alignment level. */
public final class GreedTrialRequirements {
    private GreedTrialRequirements() {
    }

    /** The rank the player is climbing toward: greed tier plus one, so tier 0 answers 1. */
    public static int nextRank(ServerPlayer player) {
        return PlayerGreedTreeData.get(player.server).getGreedTier(player) + 1;
    }

    /** Highest level the player holds with any single god, not a total across the four. */
    public static int bestGodLevel(ServerPlayer player) {
        GodAlignmentData alignment = GodAlignmentData.get(player.server);
        int best = 0;
        for (VaultGod god : VaultGod.values()) {
            best = Math.max(best, alignment.getLevel(player.getUUID(), god));
        }
        return best;
    }

    public static boolean hasReputation(ServerPlayer player, int rank) {
        return PlayerGreedTreeData.get(player.server).getGreedReputation(player)
                >= MilestoneRankLadder.getThreshold(rank);
    }

    public static boolean hasGodLevel(ServerPlayer player, int rank) {
        int gate = MilestoneRankLadder.getGodLevelGate(rank);
        return gate <= 0 || bestGodLevel(player) >= gate;
    }

    /** Whether the trial for a rank may be started now; a rank with no trial row never can be. */
    public static boolean isReady(ServerPlayer player, int rank) {
        return GreedTrial.forRank(rank) != null && hasReputation(player, rank) && hasGodLevel(player, rank);
    }
}
