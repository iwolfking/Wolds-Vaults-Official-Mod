package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.rampage.ClientRampageCdm;

import java.util.function.Supplier;

/** The Rampage damage bonus as the percentage the HUD renders; sent only when that integer changes. */
public class RampageCdmMessage {
    private final int percent;

    public RampageCdmMessage(int percent) {
        this.percent = percent;
    }

    public static void encode(RampageCdmMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.percent);
    }

    public static RampageCdmMessage decode(FriendlyByteBuf buffer) {
        return new RampageCdmMessage(buffer.readVarInt());
    }

    public static void handle(RampageCdmMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientRampageCdm.update(message.percent));
        context.setPacketHandled(true);
    }
}
