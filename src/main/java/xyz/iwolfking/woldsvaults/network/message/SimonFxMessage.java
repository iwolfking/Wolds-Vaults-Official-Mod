package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.simon.SimonFx;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/**
 * A Simon's Set effect for nearby clients to draw: a crystal emerging from the face it grows on (debris of that block
 * kicked up round its base), or a crystal bursting into flying amethyst shards (a full shatter, or a smaller pulse
 * for a crystal that has more turns left in the sequence), each with a shockwave ring out to `size`, the finale's blast
 * (`size` its radius), a puff of `stage` coloured sparks spread over `size` blocks, or a Bullet Bill's detonation:
 * the finale's blast in fire colours, with `stage` (0 to 3) for how fast it crashed.
 */
public record SimonFxMessage(int kind, double x, double y, double z, int face, int stage, int rgb, float size) {
    public enum Kind { EMERGE, SHATTER, PULSE, FINALE, FACET, BLAST }

    public SimonFxMessage(Kind kind, double x, double y, double z, Direction face, int stage, int rgb, float size) {
        this(kind.ordinal(), x, y, z, face.get3DDataValue(), stage, rgb, size);
    }

    public static void encode(SimonFxMessage message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.kind);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeByte(message.face);
        buffer.writeByte(message.stage);
        buffer.writeInt(message.rgb);
        buffer.writeFloat(message.size);
    }

    public static SimonFxMessage decode(FriendlyByteBuf buffer) {
        return new SimonFxMessage(buffer.readByte(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readByte(), buffer.readByte(), buffer.readInt(), buffer.readFloat());
    }

    public static void handle(SimonFxMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> {
            Kind[] kinds = Kind.values();
            if (message.kind >= 0 && message.kind < kinds.length) {
                SimonFx.play(kinds[message.kind], message.x, message.y, message.z, Direction.from3DDataValue(message.face), message.stage, message.rgb, message.size);
            }
        });
    }
}
