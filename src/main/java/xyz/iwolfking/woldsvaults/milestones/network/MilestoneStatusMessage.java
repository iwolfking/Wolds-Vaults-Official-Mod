package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import iskallia.vault.world.data.PlayerGreedTraderData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;
import xyz.iwolfking.woldsvaults.milestones.Milestones;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;
import xyz.iwolfking.woldsvaults.milestones.trials.GreedTrial;
import xyz.iwolfking.woldsvaults.milestones.trials.GreedTrialRequirements;

import java.util.function.Supplier;

/**
 * The greed main-screen header feed: everything the screen shows that is not a milestone counter.
 *
 * <p>{@code shopRerollCost} is a REPUTATION price, not a timer. The greed shop has no timed reset
 * of any kind: offers persist until the player pays to reroll them, and the price is
 * {@code 3 + resetCount} (capped at 36 by the addon), with the counter cleared on a greed tier-up.
 * The countdown widget already drawn on the trader screen reads the black market's shard-trade
 * reset clock and has nothing to do with the greed shop.</p>
 */
public class MilestoneStatusMessage extends Message<MilestoneStatusMessage> {
    private final int rank;
    private final int reputation;
    private final int nextRankThreshold;
    private final int unclaimedReputation;
    private final int shopRerollCost;
    private final int trialKind;
    private final int trialGodGate;
    private final int bestGodLevel;

    public MilestoneStatusMessage() {
        this(0, 0, 0, 0, 0, 0, 0, 0);
    }

    public MilestoneStatusMessage(int rank, int reputation, int nextRankThreshold, int unclaimedReputation,
                                  int shopRerollCost, int trialKind, int trialGodGate, int bestGodLevel) {
        this.rank = rank;
        this.reputation = reputation;
        this.nextRankThreshold = nextRankThreshold;
        this.unclaimedReputation = unclaimedReputation;
        this.shopRerollCost = shopRerollCost;
        this.trialKind = trialKind;
        this.trialGodGate = trialGodGate;
        this.bestGodLevel = bestGodLevel;
    }

    /**
     * Builds and sends the player's current header state. Called on login, after a claim, and by
     * the flusher whenever any of the five numbers has moved.
     */
    public static void sendTo(ServerPlayer player) {
        MilestoneNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), build(player));
    }

    /**
     * The three trial numbers are everything the "Take Trial" button needs to decide its own state
     * without a second round trip: which flavor of trial guards the next rank (0 when none does),
     * the god level that rank-up asks for (0 when it asks for none), and the best god level the
     * player actually holds. Reputation readiness the screen can already work out from
     * {@code reputation} against {@code nextRankThreshold}.
     */
    public static MilestoneStatusMessage build(ServerPlayer player) {
        PlayerGreedTreeData treeData = PlayerGreedTreeData.get(player.server);
        int rank = treeData.getGreedTier(player);
        int nextRank = rank + 1;
        GreedTrial trial = GreedTrial.forRank(nextRank);
        return new MilestoneStatusMessage(
                rank,
                treeData.getGreedReputation(player),
                MilestoneRankLadder.getThreshold(nextRank),
                Milestones.getUnclaimedRep(player.server, player.getUUID()),
                PlayerGreedTraderData.get(player.server).getResetCost(player.getUUID()),
                trial == null ? 0 : trial.getKind().ordinal() + 1,
                MilestoneRankLadder.getGodLevelGate(nextRank),
                GreedTrialRequirements.bestGodLevel(player));
    }

    public boolean matches(MilestoneStatusMessage other) {
        return other != null
                && this.rank == other.rank
                && this.reputation == other.reputation
                && this.nextRankThreshold == other.nextRankThreshold
                && this.unclaimedReputation == other.unclaimedReputation
                && this.shopRerollCost == other.shopRerollCost
                && this.trialKind == other.trialKind
                && this.trialGodGate == other.trialGodGate
                && this.bestGodLevel == other.bestGodLevel;
    }

    @Override
    public MilestoneStatusMessage read(FriendlyByteBuf buffer) {
        return new MilestoneStatusMessage(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt());
    }

    @Override
    public void write(MilestoneStatusMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.rank);
        buffer.writeVarInt(message.reputation);
        buffer.writeVarInt(message.nextRankThreshold);
        buffer.writeVarInt(message.unclaimedReputation);
        buffer.writeVarInt(message.shopRerollCost);
        buffer.writeVarInt(message.trialKind);
        buffer.writeVarInt(message.trialGodGate);
        buffer.writeVarInt(message.bestGodLevel);
    }

    @Override
    public void onMessage(MilestoneStatusMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientMilestoneData.setStatus(message.rank, message.reputation,
                    message.nextRankThreshold, message.unclaimedReputation, message.shopRerollCost);
            ClientMilestoneData.setTrialStatus(message.trialKind, message.trialGodGate, message.bestGodLevel);
        });
        context.get().setPacketHandled(true);
    }
}
