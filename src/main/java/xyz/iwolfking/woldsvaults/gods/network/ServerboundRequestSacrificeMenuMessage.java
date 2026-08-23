package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.gods.sacrifice.SacrificeAltarLogic;

import java.util.function.Supplier;

/** The open altar screen's refresh poll: the server answers with a fresh menu snapshot. */
public class ServerboundRequestSacrificeMenuMessage extends Message<ServerboundRequestSacrificeMenuMessage> {
    @Override
    public ServerboundRequestSacrificeMenuMessage read(FriendlyByteBuf buffer) {
        return new ServerboundRequestSacrificeMenuMessage();
    }

    @Override
    public void write(ServerboundRequestSacrificeMenuMessage message, FriendlyByteBuf buffer) {
    }

    @Override
    public void onMessage(ServerboundRequestSacrificeMenuMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender != null) {
                SacrificeAltarLogic.openMenu(sender);
            }
        });
        context.get().setPacketHandled(true);
    }
}
