package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Short arcs of lightning for nearby clients to draw (Lightning Rod): points in pairs, start then end. */
public class LightningArcFxMessage {
    private static final int MAX_POINTS = 64;

    private final List<Vec3> points;
    private final long seed;

    public LightningArcFxMessage(List<Vec3> points, long seed) {
        this.points = points;
        this.seed = seed;
    }

    public static void encode(LightningArcFxMessage message, FriendlyByteBuf buffer) {
        buffer.writeLong(message.seed);
        int size = Math.min(message.points.size(), MAX_POINTS);
        buffer.writeVarInt(size);
        for (Vec3 point : message.points.subList(0, size)) {
            buffer.writeDouble(point.x);
            buffer.writeDouble(point.y);
            buffer.writeDouble(point.z);
        }
    }

    public static LightningArcFxMessage decode(FriendlyByteBuf buffer) {
        long seed = buffer.readLong();
        int size = Math.min(buffer.readVarInt(), MAX_POINTS);
        List<Vec3> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            points.add(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
        return new LightningArcFxMessage(points, seed);
    }

    public static void handle(LightningArcFxMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.lightning.LightningDashRenderer.addArcs(message.points, message.seed)));
        context.setPacketHandled(true);
    }
}
