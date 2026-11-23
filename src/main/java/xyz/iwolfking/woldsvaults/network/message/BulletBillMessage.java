package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.bullet.ClientBulletBill;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.bullet.BulletBill;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * A player's Bullet Bill form, for the clients that see them: START (with the ticks it lasts and the flight tuning, so
 * the rider's client flies it as the server does), BOOST (a Bullet Dash: the spin, red pulse and speed-up, for the
 * given ticks), THRUST (its jets firing, ticks 1, or cutting out, ticks 0), ARM (its detonation armed) and END.
 */
public record BulletBillMessage(int entityId, int kind, int ticks, @Nullable BulletBill.Tuning tuning) {
    public static final int START = 0;
    public static final int BOOST = 1;
    public static final int END = 2;
    public static final int THRUST = 3;
    public static final int ARM = 4;

    public static void encode(BulletBillMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeByte(message.kind);
        buffer.writeVarInt(message.ticks);
        if (message.kind == START && message.tuning != null) {
            message.tuning.write(buffer);
        }
    }

    public static BulletBillMessage decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        int kind = buffer.readByte();
        int ticks = buffer.readVarInt();
        return new BulletBillMessage(entityId, kind, ticks, kind == START ? BulletBill.Tuning.read(buffer) : null);
    }

    public static void handle(BulletBillMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> {
            switch (message.kind) {
                case START -> ClientBulletBill.start(message.entityId, message.ticks, message.tuning);
                case BOOST -> ClientBulletBill.boost(message.entityId, message.ticks);
                case THRUST -> ClientBulletBill.thrust(message.entityId, message.ticks != 0);
                case ARM -> ClientBulletBill.arm(message.entityId);
                default -> ClientBulletBill.end(message.entityId);
            }
        });
    }
}
