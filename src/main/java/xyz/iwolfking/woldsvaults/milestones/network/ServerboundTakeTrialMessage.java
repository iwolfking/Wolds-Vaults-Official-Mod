package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.milestones.trials.GreedTrials;

import java.util.function.Supplier;

/** "Take Trial" click; carries no payload, {@link GreedTrials#take} derives and checks everything. */
public class ServerboundTakeTrialMessage extends Message<ServerboundTakeTrialMessage> {
    public ServerboundTakeTrialMessage() {
    }

    @Override
    public ServerboundTakeTrialMessage read(FriendlyByteBuf buffer) {
        return new ServerboundTakeTrialMessage();
    }

    @Override
    public void write(ServerboundTakeTrialMessage message, FriendlyByteBuf buffer) {
    }

    @Override
    public void onMessage(ServerboundTakeTrialMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                GreedTrials.take(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
