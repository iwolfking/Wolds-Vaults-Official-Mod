package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.simon.SimonsSet;

import java.util.function.Supplier;

/** A Simon's Set click: right click shoots a prism seed, sneak right click resonates, an aimed left click hits a crystal. */
public record SimonActionMessage(int action, int target) {
    public enum Action { SHOOT, RESONATE, HIT }

    public SimonActionMessage(Action action, int target) {
        this(action.ordinal(), target);
    }

    public static void encode(SimonActionMessage message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.action);
        buffer.writeVarInt(message.target);
    }

    public static SimonActionMessage decode(FriendlyByteBuf buffer) {
        return new SimonActionMessage(buffer.readByte(), buffer.readVarInt());
    }

    public static void handle(SimonActionMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onServer(context, player -> {
            Action[] actions = Action.values();
            if (message.action < 0 || message.action >= actions.length) {
                return;
            }
            switch (actions[message.action]) {
                case SHOOT -> SimonsSet.shoot(player);
                case RESONATE -> SimonsSet.resonate(player);
                case HIT -> SimonsSet.clickCrystal(player, message.target);
            }
        });
    }
}
