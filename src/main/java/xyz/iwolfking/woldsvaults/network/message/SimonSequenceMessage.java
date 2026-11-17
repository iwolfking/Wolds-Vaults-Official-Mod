package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/** The crystals a player's Simon sequence is waiting for them to click (none once it ends), so left clicks on them can be picked. */
public record SimonSequenceMessage(int[] crystalIds) {
    public static void encode(SimonSequenceMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarIntArray(message.crystalIds);
    }

    public static SimonSequenceMessage decode(FriendlyByteBuf buffer) {
        return new SimonSequenceMessage(buffer.readVarIntArray(64));
    }

    public static void handle(SimonSequenceMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onClient(context, () -> () -> xyz.iwolfking.woldsvaults.client.simon.ClientSimon.waitFor(message.crystalIds));
    }
}
