package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.MirroredStats;

import java.util.UUID;
import java.util.function.Supplier;

/** Server to client: the server's values of the mirrored player stats. */
public record StatMirrorMessage(UUID player, float[] values) {
    public static void encode(StatMirrorMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.player);
        buffer.writeVarInt(message.values.length);
        for (float value : message.values) {
            buffer.writeFloat(value);
        }
    }

    public static StatMirrorMessage decode(FriendlyByteBuf buffer) {
        UUID player = buffer.readUUID();
        float[] values = new float[Math.min(16, buffer.readVarInt())];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.readFloat();
        }
        return new StatMirrorMessage(player, values);
    }

    public static void handle(StatMirrorMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MirroredStats.receive(message.player, message.values)));
        context.get().setPacketHandled(true);
    }
}
