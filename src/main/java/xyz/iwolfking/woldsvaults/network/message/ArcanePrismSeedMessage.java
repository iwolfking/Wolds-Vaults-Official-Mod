package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/**
 * An Arcane: Prism seed launched, for clients to draw in flight. It carries the hand it left, the mob it homes in on
 * (-1 for none), where it was aimed (used when that mob can't be seen), the game time it left, and how many ticks it
 * flies.
 */
public record ArcanePrismSeedMessage(Vec3 start, int targetId, Vec3 aim, long launched, int flightTicks) {
    public static void encode(ArcanePrismSeedMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.start.x);
        buffer.writeDouble(message.start.y);
        buffer.writeDouble(message.start.z);
        buffer.writeInt(message.targetId);
        buffer.writeDouble(message.aim.x);
        buffer.writeDouble(message.aim.y);
        buffer.writeDouble(message.aim.z);
        buffer.writeLong(message.launched);
        buffer.writeVarInt(message.flightTicks);
    }

    public static ArcanePrismSeedMessage decode(FriendlyByteBuf buffer) {
        Vec3 start = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        int targetId = buffer.readInt();
        Vec3 aim = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new ArcanePrismSeedMessage(start, targetId, aim, buffer.readLong(), buffer.readVarInt());
    }

    public static void handle(ArcanePrismSeedMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> xyz.iwolfking.woldsvaults.client.arcane.LaserBeams.addSeed(message.start, message.targetId, message.aim,
                message.launched, message.flightTicks));
    }
}
