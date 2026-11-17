package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/** Tells a player whether Ten Magatsuhi lets their right click with a katana cut, swap and return. */
public record MagatsuhiSetMessage(boolean canCut) {
    public static void encode(MagatsuhiSetMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.canCut);
    }

    public static MagatsuhiSetMessage decode(FriendlyByteBuf buffer) {
        return new MagatsuhiSetMessage(buffer.readBoolean());
    }

    public static void handle(MagatsuhiSetMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> xyz.iwolfking.woldsvaults.client.magatsuhi.ClientMagatsuhi.set(message.canCut));
    }
}
