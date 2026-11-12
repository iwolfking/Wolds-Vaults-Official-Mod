package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperBossDpsTracker;

import java.util.function.Supplier;

/** Sent by the boss DPS HUD's reset button: drops the sender's current fight statistics. */
public class ServerboundResetBossDpsMessage {
    public static void encode(ServerboundResetBossDpsMessage message, FriendlyByteBuf buffer) {
    }

    public static ServerboundResetBossDpsMessage decode(FriendlyByteBuf buffer) {
        return new ServerboundResetBossDpsMessage();
    }

    public static void handle(ServerboundResetBossDpsMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                HyperBossDpsTracker.reset(player);
            }
        });
        context.setPacketHandled(true);
    }
}
