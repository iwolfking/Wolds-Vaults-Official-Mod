package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.bullet.BulletBill;

import java.util.function.Supplier;

// A Bullet Bill control from the client, carried out by the server if the set allows it: a double tap of jump
// transforms, the rider's right click arms the detonation, and pressing or letting go of jump fires or cuts the jets.
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
