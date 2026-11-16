package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** A laser beam for nearby clients to draw: a line through the points, its colour and width, kept for some ticks. */
public record ArcaneBeamMessage(List<Vec3> points, int rgb, float width, int ticks) {
    private static final int MAX_POINTS = 32;

    public static void encode(ArcaneBeamMessage message, FriendlyByteBuf buffer) {
        int count = Math.min(MAX_POINTS, message.points.size());
        buffer.writeByte(count);
        for (int i = 0; i < count; i++) {
            Vec3 p = message.points.get(i);
            buffer.writeDouble(p.x);
            buffer.writeDouble(p.y);
            buffer.writeDouble(p.z);
        }
        buffer.writeInt(message.rgb);
        buffer.writeFloat(message.width);
        buffer.writeByte(message.ticks);
    }

    public static ArcaneBeamMessage decode(FriendlyByteBuf buffer) {
        int count = Math.min(MAX_POINTS, buffer.readByte());
        List<Vec3> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            points.add(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
        return new ArcaneBeamMessage(points, buffer.readInt(), buffer.readFloat(), buffer.readByte());
    }

    public static void handle(ArcaneBeamMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> {
            if (message.points.size() >= 2) {
                xyz.iwolfking.woldsvaults.client.arcane.LaserBeams.add(message.points, message.rgb, message.width, message.ticks);
            }
        });
    }
}
