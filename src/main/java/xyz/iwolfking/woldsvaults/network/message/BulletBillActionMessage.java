package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.bullet.BulletBill;

import java.util.function.Supplier;

public record BulletBillActionMessage(int action) {
    public enum Action { TRANSFORM, ARM, THRUST_ON, THRUST_OFF }

    public BulletBillActionMessage(Action action) {
        this(action.ordinal());
    }

    public static void encode(BulletBillActionMessage message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.action);
    }

    public static BulletBillActionMessage decode(FriendlyByteBuf buffer) {
        return new BulletBillActionMessage(buffer.readByte());
    }

    public static void handle(BulletBillActionMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onServer(context, player -> {
            Action[] actions = Action.values();
            if (message.action < 0 || message.action >= actions.length) {
                return;
            }
            switch (actions[message.action]) {
                case TRANSFORM -> BulletBill.transform(player);
                case ARM -> BulletBill.arm(player);
                case THRUST_ON -> BulletBill.thrust(player, true);
                case THRUST_OFF -> BulletBill.thrust(player, false);
            }
        });
    }
}
