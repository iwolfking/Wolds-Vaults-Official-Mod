package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.milestones.Milestones;

import java.util.function.Supplier;

public class ServerboundPinMilestoneMessage extends Message<ServerboundPinMilestoneMessage> {

    private final String milestoneId;

    public ServerboundPinMilestoneMessage() {
        this.milestoneId = "";
    }
    public ServerboundPinMilestoneMessage(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    @Override
    public ServerboundPinMilestoneMessage read(FriendlyByteBuf buffer) {
        return new ServerboundPinMilestoneMessage(buffer.readUtf());
    }

    @Override
    public void write(ServerboundPinMilestoneMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.milestoneId);
    }

    @Override
    public void onMessage(ServerboundPinMilestoneMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
              Milestones.pin(player, message.milestoneId);
            }
        });
        ctx.setPacketHandled(true);
    }
}
