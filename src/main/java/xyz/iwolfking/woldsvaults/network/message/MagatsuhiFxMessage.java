package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/**
 * Something Ten Magatsuhi shows nearby players. MARK: the owner's cut, a width by height square centred on the point,
 * lying on a block face (face = its Direction ordinal) or standing upright across the yaw (face -1), following an entity
 * when one is given, for ticks. CLEAR: the owner's cut is gone. AFTERIMAGE: a translucent box of width by height
 * standing on the point, where someone just teleported from. SLASH: both katanas' crossed slash at the point, facing
 * the yaw, width across. CROSS: the finisher's X centred on the point, facing the yaw, each stroke width long, cut in
 * one after the other.
 */
public record MagatsuhiFxMessage(int kind, int owner, double x, double y, double z, int face, float yaw, int entity, float width, float height, int ticks) {
    public enum Kind { MARK, CLEAR, AFTERIMAGE, SLASH, CROSS }

    public MagatsuhiFxMessage(Kind kind, int owner, Vec3 at, int face, float yaw, int entity, float width, float height, int ticks) {
        this(kind.ordinal(), owner, at.x, at.y, at.z, face, yaw, entity, width, height, ticks);
    }

    public static void encode(MagatsuhiFxMessage message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.kind);
        buffer.writeVarInt(message.owner);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeByte(message.face);
        buffer.writeFloat(message.yaw);
        buffer.writeVarInt(message.entity);
        buffer.writeFloat(message.width);
        buffer.writeFloat(message.height);
        buffer.writeVarInt(message.ticks);
    }

    public static MagatsuhiFxMessage decode(FriendlyByteBuf buffer) {
        return new MagatsuhiFxMessage(buffer.readByte(), buffer.readVarInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readByte(), buffer.readFloat(), buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(), buffer.readVarInt());
    }

    public static void handle(MagatsuhiFxMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> xyz.iwolfking.woldsvaults.client.magatsuhi.ClientMagatsuhi.fx(message));
    }
}
