package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

// Tells a player's client to fade out, hold black and fade back in, in ticks, around a loading zone teleport.
public record LoadingZoneFadeMessage(int fadeOut, int hold, int fadeIn) {
    public static void encode(LoadingZoneFadeMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.fadeOut);
        buffer.writeVarInt(message.hold);
        buffer.writeVarInt(message.fadeIn);
    }

    public static LoadingZoneFadeMessage decode(FriendlyByteBuf buffer) {
        return new LoadingZoneFadeMessage(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(LoadingZoneFadeMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> xyz.iwolfking.woldsvaults.client.loadingzone.ClientLoadingZone.begin(message.fadeOut, message.hold, message.fadeIn));
    }
}
