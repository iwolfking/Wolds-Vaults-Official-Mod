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

    public MilestoneStatusMessage() {
        this(0, 0, 0, 0, 0);
    }

    public MilestoneStatusMessage(int rank, int reputation, int nextRankThreshold, int unclaimedReputation, int shopRerollCost) {
        this.rank = rank;
        this.reputation = reputation;
        this.nextRankThreshold = nextRankThreshold;
        this.unclaimedReputation = unclaimedReputation;
        this.shopRerollCost = shopRerollCost;
    }

    /**
     * Builds and sends the player's current header state. Called on login, after a claim, and by
     * the flusher whenever any of the five numbers has moved.
     */
    public static void sendTo(ServerPlayer player) {
        MilestoneNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), build(player));
    }

    public static MilestoneStatusMessage build(ServerPlayer player) {
        PlayerGreedTreeData treeData = PlayerGreedTreeData.get(player.server);
        int rank = treeData.getGreedTier(player);
        return new MilestoneStatusMessage(
                rank,
                treeData.getGreedReputation(player),
                MilestoneRankLadder.getThreshold(rank + 1),
                Milestones.getUnclaimedRep(player.server, player.getUUID()),
                PlayerGreedTraderData.get(player.server).getResetCost(player.getUUID()));
    }

    public boolean matches(MilestoneStatusMessage other) {
        return other != null
                && this.rank == other.rank
                && this.reputation == other.reputation
                && this.nextRankThreshold == other.nextRankThreshold
                && this.unclaimedReputation == other.unclaimedReputation
                && this.shopRerollCost == other.shopRerollCost;
    }

    @Override
    public MilestoneStatusMessage read(FriendlyByteBuf buffer) {
        return new MilestoneStatusMessage(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt());
    }

    @Override
    public void write(MilestoneStatusMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.rank);
        buffer.writeVarInt(message.reputation);
        buffer.writeVarInt(message.nextRankThreshold);
        buffer.writeVarInt(message.unclaimedReputation);
        buffer.writeVarInt(message.shopRerollCost);
    }

    @Override
    public void onMessage(MilestoneStatusMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientMilestoneData.setStatus(message.rank, message.reputation,
                message.nextRankThreshold, message.unclaimedReputation, message.shopRerollCost));
        context.get().setPacketHandled(true);
    }
}
