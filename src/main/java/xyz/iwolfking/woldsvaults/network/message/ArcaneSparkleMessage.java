package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/** A puff of glowing sparks for nearby clients to draw: where, how many, their colour and how far they spread. */
public record ArcaneSparkleMessage(Vec3 at, int count, int rgb, float spread) {
    public static void encode(ArcaneSparkleMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.at.x);
        buffer.writeDouble(message.at.y);
        buffer.writeDouble(message.at.z);
        buffer.writeVarInt(message.count);
        buffer.writeInt(message.rgb);
        buffer.writeFloat(message.spread);
    }

    public static ArcaneSparkleMessage decode(FriendlyByteBuf buffer) {
        Vec3 at = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new ArcaneSparkleMessage(at, buffer.readVarInt(), buffer.readInt(), buffer.readFloat());
    }

    public static void handle(ArcaneSparkleMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> xyz.iwolfking.woldsvaults.client.arcane.LaserBeams.sparkle(message.at, ColorUtil.vector(message.rgb), message.count, message.spread));
    }
}
