package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.arcane.LaserBeams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** A laser beam for clients to draw: a line through the points, its color and width, kept for some ticks. */
public record ArcaneBeamMessage(List<Vec3> points, int rgb, float width, int ticks) {
    private static final int MAX_POINTS = 32;

    public static void encode(ArcaneBeamMessage message, FriendlyByteBuf buffer) {
        int count = Math.min(MAX_POINTS, message.points.size());
        buffer.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            Vec3 p = message.points.get(i);
            buffer.writeDouble(p.x);
            buffer.writeDouble(p.y);
            buffer.writeDouble(p.z);
        }
        buffer.writeInt(message.rgb);
        buffer.writeFloat(message.width);
        buffer.writeVarInt(message.ticks);
    }

    public static ArcaneBeamMessage decode(FriendlyByteBuf buffer) {
        int count = Math.min(MAX_POINTS, buffer.readVarInt());
        List<Vec3> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            points.add(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
        return new ArcaneBeamMessage(points, buffer.readInt(), buffer.readFloat(), buffer.readVarInt());
    }

    public static void handle(ArcaneBeamMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (message.points.size() >= 2) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LaserBeams.add(message.points, message.rgb, message.width, message.ticks)));
        }
        context.setPacketHandled(true);
    }
}
