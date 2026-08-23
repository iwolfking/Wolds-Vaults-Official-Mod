package xyz.iwolfking.woldsvaults.milestones.trials;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

/**
 * The gate in front of the "Take Trial" button: enough reputation for the next rank, plus the
 * god-alignment level that band jumps ask for. Server side only - both inputs live in world saved
 * data, so the client learns the outcome through the milestone status message instead.
 */
public final class GreedTrialRequirements {
    private GreedTrialRequirements() {
    }

    /**
     * The rank the player is currently climbing toward. Tier 0 means the Herald has not been beaten
     * yet, so this answers 1 - the ladder's entry rank, which beating the Herald awards outright
     * and which has no trial of its own.
     */
    public static int nextRank(ServerPlayer player) {
        return PlayerGreedTreeData.get(player.server).getGreedTier(player) + 1;
    }

    /**
     * Highest level the player holds with any single god. The band-jump gate is satisfied by one
     * god reaching the level, not by a total across the four.
     */
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

    /**
     * Whether the player may start the trial for the given rank right now. A rank with no trial row
     * is never takeable, which today means the entry rank and every rank past Legend.
     *
     * <p>Legend is therefore the ceiling as shipped. {@link MilestoneRankLadder} prices Legend+
     * ranks and the greed screens render them, but nothing in the addon promotes anyone past
     * Legend: the only writers of the rank are the trial payout, which stops at the last trial row,
     * and the Herald award. A Legend+ climb needs a promoter of its own before it exists in game.</p>
     */
    public static boolean isReady(ServerPlayer player, int rank) {
        return GreedTrial.forRank(rank) != null && hasReputation(player, rank) && hasGodLevel(player, rank);
    }
}
