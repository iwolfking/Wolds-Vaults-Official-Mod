package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Clientbound milestone delta, or the whole set when {@code full} is set. Also carries their
 * claimed tier counts and the pinned milestone id.
 */
public class MilestoneSyncMessage extends Message<MilestoneSyncMessage> {
    private static final int MAX_DECODED_ENTRIES = 1024;

    private final boolean full;
    private final Map<String, Long> values;
    private final Map<String, Integer> claimedTiers;
    private final Set<String> pinned;

    public MilestoneSyncMessage() {
        this.full = false;
        this.values = new HashMap<>();
        this.claimedTiers = new HashMap<>();
        this.pinned = new HashSet<>();
    }

    public MilestoneSyncMessage(boolean full, Map<String, Long> values, Map<String, Integer> claimedTiers, Set<String> pinned) {
        this.full = full;
        this.values = values;
        this.claimedTiers = claimedTiers;
        this.pinned = pinned;
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
        int pinnedSize = buffer.readVarInt();
        Set<String> pinned = new HashSet<>(initialCapacity(claimedSize));

        for (int i = 0; i < pinnedSize; i++) {
            pinned.add(buffer.readUtf());
        }
        return new MilestoneSyncMessage(full, values, claimedTiers, pinned);
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
        buffer.writeVarInt(message.pinned.size());
        message.pinned.forEach(buffer::writeUtf);
    }

    @Override
    public void onMessage(MilestoneSyncMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (message.full) {
                ClientMilestoneData.replaceAll(message.values, message.claimedTiers);
            } else {
                ClientMilestoneData.apply(message.values, message.claimedTiers);
            }
            ClientMilestoneData.setPinned(message.pinned);
        });
        context.get().setPacketHandled(true);
    }

    /** Clamps the map capacity a decoded packet may ask for to 16..{@link #MAX_DECODED_ENTRIES}. */
    private static int initialCapacity(int size) {
        return Math.min(Math.max(16, size), MAX_DECODED_ENTRIES);
    }
}
