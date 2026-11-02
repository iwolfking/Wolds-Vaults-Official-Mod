package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Tells a player which enemy their Lightning Rod has marked (Lightning Fiend 6-piece), or -1 once it lapses. */
public class LightningMarkMessage {
    private final int entityId;

    public LightningMarkMessage(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(LightningMarkMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
    }

    public static LightningMarkMessage decode(FriendlyByteBuf buffer) {
        return new LightningMarkMessage(buffer.readInt());
    }

    public static void handle(LightningMarkMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.lightning.ClientLightningForm.mark(message.entityId)));
        context.setPacketHandled(true);
    }
}
