package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An Arc Lash (Lightning Fiend 6-piece) for nearby clients to draw: where its bolts leave from, which way each flies
 * and how far before it lands, and how fast they fly.
 */
public class LightningLashFxMessage {
    private static final int MAX_BOLTS = 32;

    private final Vec3 origin;
    private final List<Vec3> directions;
    private final List<Float> lengths;
    private final float speed;
    private final long seed;

    public LightningLashFxMessage(Vec3 origin, List<Vec3> directions, List<Float> lengths, float speed, long seed) {
        this.origin = origin;
        this.directions = directions;
        this.lengths = lengths;
        this.speed = speed;
        this.seed = seed;
    }

    public static void encode(LightningLashFxMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.origin.x);
        buffer.writeDouble(message.origin.y);
        buffer.writeDouble(message.origin.z);
        buffer.writeFloat(message.speed);
        buffer.writeLong(message.seed);
        int count = Math.min(message.directions.size(), MAX_BOLTS);
        buffer.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            Vec3 direction = message.directions.get(i);
            buffer.writeFloat((float) direction.x);
            buffer.writeFloat((float) direction.y);
            buffer.writeFloat((float) direction.z);
            buffer.writeFloat(message.lengths.get(i));
        }
    }

    public static LightningLashFxMessage decode(FriendlyByteBuf buffer) {
        Vec3 origin = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        float speed = buffer.readFloat();
        long seed = buffer.readLong();
        int count = Math.min(buffer.readVarInt(), MAX_BOLTS);
        List<Vec3> directions = new ArrayList<>(count);
        List<Float> lengths = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            directions.add(new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()));
            lengths.add(buffer.readFloat());
        }
        return new LightningLashFxMessage(origin, directions, lengths, speed, seed);
    }

    public static void handle(LightningLashFxMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (message.speed > 0.0F) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> xyz.iwolfking.woldsvaults.client.lightning.LightningLashRenderer.add(message.origin, message.directions, message.lengths, message.speed, message.seed)));
        }
        context.setPacketHandled(true);
    }
}
