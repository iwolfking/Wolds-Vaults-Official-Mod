package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.events.TickEvents;

import java.util.UUID;
import java.util.function.Supplier;

public record StopFlightMessage(UUID playerUUID) {
    public static void encode(StopFlightMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playerUUID);
    }

    public static StopFlightMessage decode(FriendlyByteBuf buffer) {
        return new StopFlightMessage(buffer.readUUID());
    }

    public static void handle(StopFlightMessage message, Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
        if (player != null) {
            TickEvents.stopFlying(player);
        }
        context.get().setPacketHandled(true);
    }
}
