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
    private static final int MAX_DECODED_ENTRIES = 1024;

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
        Map<String, Long> values = new HashMap<>(initialCapacity(size));
        for (int i = 0; i < size; i++) {
            values.put(buffer.readUtf(), buffer.readVarLong());
        }
        int claimedSize = buffer.readVarInt();
        Map<String, Integer> claimedTiers = new HashMap<>(initialCapacity(claimedSize));
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

    /**
     * Bounds the map capacity a decoded packet may ask for. The count is attacker-controlled, and
     * {@code new HashMap<>(size)} allocates its whole backing array on the first put, so an
     * unbounded count turns a few bytes on the wire into a multi-gigabyte allocation on the netty
     * thread. The loops that follow are still bounded by the frame length, so a lying count only
     * costs a rehash.
     */
    private static int initialCapacity(int size) {
        return Math.min(Math.max(16, size), MAX_DECODED_ENTRIES);
    }
}
