package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.rampage.ClientRampageCdm;

import java.util.function.Supplier;

/**
 * The Rampage damage bonus, as the percentage the HUD indicator renders.
 *
 * <p>Rampage stopped being visible to the client when the_vault 3.21.6 moved it out of
 * {@code PlayerDamageHelper} - the registry the base indicator reads - and into a direct
 * {@code LivingHurtEvent} multiplication. Nothing syncs it any more, so this packet does.
 *
 * <p>Sent only when the rendered integer changes, which is at most a couple of times a second and
 * only while Ultra Rampaging is live. An int rather than a float for the same reason: the client
 * has no use for precision the display cannot show.
 */
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
