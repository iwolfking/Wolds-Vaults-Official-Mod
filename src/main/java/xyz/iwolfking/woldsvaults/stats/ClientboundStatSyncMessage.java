package xyz.iwolfking.woldsvaults.stats;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatSyncClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** The player's Statistics tab values as the server computes them, keyed like {@link StatSources}. */
public record ClientboundStatSyncMessage(Map<String, Double> values) {
    public static void encode(ClientboundStatSyncMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.values.size());
        message.values.forEach((key, value) -> {
            buf.writeUtf(key);
            buf.writeDouble(value);
        });
    }

    public static ClientboundStatSyncMessage decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Double> values = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            values.put(buf.readUtf(), buf.readDouble());
        }
        return new ClientboundStatSyncMessage(values);
    }

    public static void handle(ClientboundStatSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> apply(message));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void apply(ClientboundStatSyncMessage message) {
        StatSyncClient.accept(message.values);
    }
}
