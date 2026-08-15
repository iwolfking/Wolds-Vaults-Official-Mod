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
 * or the player's whole set when {@code full} is set (login).
 */
public class MilestoneSyncMessage extends Message<MilestoneSyncMessage> {
    private final boolean full;
    private final Map<String, Long> values;

    public MilestoneSyncMessage() {
        this.full = false;
        this.values = new HashMap<>();
    }

    public MilestoneSyncMessage(boolean full, Map<String, Long> values) {
        this.full = full;
        this.values = values;
    }

    @Override
    public MilestoneSyncMessage read(FriendlyByteBuf buffer) {
        boolean full = buffer.readBoolean();
        int size = buffer.readVarInt();
        Map<String, Long> values = new HashMap<>(Math.max(16, size));
        for (int i = 0; i < size; i++) {
            values.put(buffer.readUtf(), buffer.readVarLong());
        }
        return new MilestoneSyncMessage(full, values);
    }

    @Override
    public void write(MilestoneSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.full);
        buffer.writeVarInt(message.values.size());
        message.values.forEach((id, value) -> {
            buffer.writeUtf(id);
            buffer.writeVarLong(value);
        });
    }

    @Override
    public void onMessage(MilestoneSyncMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (message.full) {
                ClientMilestoneData.replaceAll(message.values);
            } else {
                ClientMilestoneData.apply(message.values);
            }
        });
        context.get().setPacketHandled(true);
    }
}
