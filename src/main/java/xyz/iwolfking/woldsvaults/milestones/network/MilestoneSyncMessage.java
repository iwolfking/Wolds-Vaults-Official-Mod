package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Clientbound milestone delta. Carries only the counters that changed since the previous flush,
 * or the player's whole set when {@code full} is set (login). Every delta also carries the claimed
 * tier count for the same milestones and the currently pinned milestone id, so the client store is
 * never out of step with a claim or a pin.
 */
public class MilestoneSyncMessage extends Message<MilestoneSyncMessage> {
    private final boolean full;
    private final Map<String, Long> values;
    private final Map<String, Integer> claimedTiers;
    private final String pinned;

    public MilestoneSyncMessage() {
        this.full = false;
        this.values = new HashMap<>();
        this.claimedTiers = new HashMap<>();
        this.pinned = "";
    }

    public MilestoneSyncMessage(boolean full, Map<String, Long> values, Map<String, Integer> claimedTiers, String pinned) {
        this.full = full;
        this.values = values;
        this.claimedTiers = claimedTiers;
        this.pinned = pinned == null ? "" : pinned;
    }

    @Override
    public MilestoneSyncMessage read(FriendlyByteBuf buffer) {
        boolean full = buffer.readBoolean();
        int size = buffer.readVarInt();
        Map<String, Long> values = new HashMap<>(Math.max(16, size));
        for (int i = 0; i < size; i++) {
            values.put(buffer.readUtf(), buffer.readVarLong());
        }
        int claimedSize = buffer.readVarInt();
        Map<String, Integer> claimedTiers = new HashMap<>(Math.max(16, claimedSize));
        for (int i = 0; i < claimedSize; i++) {
            claimedTiers.put(buffer.readUtf(), buffer.readVarInt());
        }
        return new MilestoneSyncMessage(full, values, claimedTiers, buffer.readUtf());
    }

    @Override
    public void write(MilestoneSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.full);
        buffer.writeVarInt(message.values.size());
        message.values.forEach((id, value) -> {
            buffer.writeUtf(id);
            buffer.writeVarLong(value);
        });
        buffer.writeVarInt(message.claimedTiers.size());
        message.claimedTiers.forEach((id, value) -> {
            buffer.writeUtf(id);
            buffer.writeVarInt(value);
        });
        buffer.writeUtf(message.pinned);
    }

    @Override
    public void onMessage(MilestoneSyncMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (message.full) {
                ClientMilestoneData.replaceAll(message.values, message.claimedTiers);
            } else {
                ClientMilestoneData.apply(message.values, message.claimedTiers);
            }
            ClientMilestoneData.setPinned(message.pinned.isEmpty() ? null : message.pinned);
        });
        context.get().setPacketHandled(true);
    }
}
