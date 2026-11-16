package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.arcane.PrismRenderer;

import java.util.function.Supplier;

/**
 * An Arcane: Prism pyramid placed, for clients to draw. It carries where the pyramid is, the heading its first beam
 * faced (radians from +X), the game time it appeared and how long it lasts. It also carries how far its beams reach,
 * whether it has the split etching's four extra beams, and how far bent light carries and how wide a thing is to it.
 */
public record ArcanePrismMessage(Vec3 position, float heading, long created, int durationTicks, float reach, boolean split, float bentLength, float bentRadius) {
    public static void encode(ArcanePrismMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
        buffer.writeFloat(message.heading);
        buffer.writeLong(message.created);
        buffer.writeVarInt(message.durationTicks);
        buffer.writeFloat(message.reach);
        buffer.writeBoolean(message.split);
        buffer.writeFloat(message.bentLength);
        buffer.writeFloat(message.bentRadius);
    }

    public static ArcanePrismMessage decode(FriendlyByteBuf buffer) {
        Vec3 position = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return new ArcanePrismMessage(position, buffer.readFloat(), buffer.readLong(), buffer.readVarInt(), buffer.readFloat(),
                buffer.readBoolean(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(ArcanePrismMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PrismRenderer.addPrism(message.position, message.heading,
                message.created, message.durationTicks, message.reach, message.split, message.bentLength, message.bentRadius)));
        context.setPacketHandled(true);
    }
}
