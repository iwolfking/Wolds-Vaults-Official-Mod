package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.magatsuhi.TenMagatsuhi;

import java.util.function.Supplier;

/** A right click with a Ten Magatsuhi katana: the server works out whether it cuts, swaps or returns. */
public record MagatsuhiActionMessage() {
    public static void encode(MagatsuhiActionMessage message, FriendlyByteBuf buffer) {
    }

    public static MagatsuhiActionMessage decode(FriendlyByteBuf buffer) {
        return new MagatsuhiActionMessage();
    }

    public static void handle(MagatsuhiActionMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onServer(context, TenMagatsuhi::act);
    }
}
