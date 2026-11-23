package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/**
 * Tells a player what Simon's Set lets their clicks do: whether right click shoots prism seeds (and the cooldown
 * between them), whether sneak right click resonates, and how far an aimed left click reaches a crystal.
 */
public record SimonSetMessage(boolean canShoot, int cooldownTicks, boolean canResonate, float clickRange) {
    public static void encode(SimonSetMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.canShoot);
        buffer.writeVarInt(message.cooldownTicks);
        buffer.writeBoolean(message.canResonate);
        buffer.writeFloat(message.clickRange);
    }

    public static SimonSetMessage decode(FriendlyByteBuf buffer) {
        return new SimonSetMessage(buffer.readBoolean(), buffer.readVarInt(), buffer.readBoolean(), buffer.readFloat());
    }

    public static void handle(SimonSetMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> xyz.iwolfking.woldsvaults.client.simon.ClientSimon.set(message.canShoot, message.cooldownTicks, message.canResonate, message.clickRange));
    }
}
