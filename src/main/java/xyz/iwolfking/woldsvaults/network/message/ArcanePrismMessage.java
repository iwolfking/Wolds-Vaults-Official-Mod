package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/**
 * An Arcane: Prism pyramid placed, for clients to draw. It carries where the pyramid is, the angle it first faced, the
 * game time it appeared and how long it lasts. It also carries how far its beams reach, whether it has the split
 * etching's four extra beams, and how far bent light carries and how wide a thing is to it. Its beams turn at
 * {@link #SPIN_DEGREES_PER_TICK} from that first angle on both sides, and clients bend them the same way the server
 * does, so what is drawn is what hits.
 */
public record ArcanePrismMessage(Vec3 position, float yaw, long created, int durationTicks, float reach, boolean split, float bentRange, float bentRadius) {
    public static final float SPIN_DEGREES_PER_TICK = 3.0F;

    public static void encode(ArcanePrismMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
        buffer.writeFloat(message.yaw);
        buffer.writeLong(message.created);
        buffer.writeVarInt(message.durationTicks);
        buffer.writeFloat(message.reach);
        buffer.writeBoolean(message.split);
        buffer.writeFloat(message.bentRange);
        buffer.writeFloat(message.bentRadius);
    }

    public static ArcanePrismMessage decode(FriendlyByteBuf buffer) {
        Vec3 position = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new ArcanePrismMessage(position, buffer.readFloat(), buffer.readLong(), buffer.readVarInt(), buffer.readFloat(),
                buffer.readBoolean(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(ArcanePrismMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> xyz.iwolfking.woldsvaults.client.arcane.LaserBeams.addPrism(message.position, message.yaw, message.created,
                message.durationTicks, message.reach, message.split, message.bentRange, message.bentRadius));
    }
}
