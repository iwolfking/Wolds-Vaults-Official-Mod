package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Clientbound heartbeat of the hyperboss Magic Missile charge: remaining charge ticks and the full
 * charge window, sent every tick while the volley telegraphs. A zero window clears the display.
 */
public class MagicMissileWarningMessage {
    private final int remainingTicks;
    private final int windowTicks;

    public MagicMissileWarningMessage(int remainingTicks, int windowTicks) {
        this.remainingTicks = remainingTicks;
        this.windowTicks = windowTicks;
    }

    public static void encode(MagicMissileWarningMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.remainingTicks);
        buffer.writeVarInt(message.windowTicks);
    }

    public static MagicMissileWarningMessage decode(FriendlyByteBuf buffer) {
        return new MagicMissileWarningMessage(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(MagicMissileWarningMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.hyper.ClientMagicMissileWarning.update(
                        message.remainingTicks, message.windowTicks)));
        context.setPacketHandled(true);
    }
}
