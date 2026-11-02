package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** A Thunderstrike (Lightning Fiend 6-piece) for nearby clients to draw: where it lands and how far it reaches. */
public class LightningStrikeFxMessage {
    private final Vec3 at;
    private final float radius;
    private final long seed;

    public LightningStrikeFxMessage(Vec3 at, float radius, long seed) {
        this.at = at;
        this.radius = radius;
        this.seed = seed;
    }

    public static void encode(LightningStrikeFxMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.at.x);
        buffer.writeDouble(message.at.y);
        buffer.writeDouble(message.at.z);
        buffer.writeFloat(message.radius);
        buffer.writeLong(message.seed);
    }

    public static LightningStrikeFxMessage decode(FriendlyByteBuf buffer) {
        Vec3 at = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new LightningStrikeFxMessage(at, buffer.readFloat(), buffer.readLong());
    }

    public static void handle(LightningStrikeFxMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.lightning.LightningDashRenderer.addStrike(message.at, message.radius, message.seed)));
        context.setPacketHandled(true);
    }
}
