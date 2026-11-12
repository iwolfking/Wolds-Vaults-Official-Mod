package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.milestones.Milestones;

import java.util.function.Supplier;

public class ServerboundUnpinMilestoneMessage extends Message<ServerboundUnpinMilestoneMessage> {

    private final String milestoneId;

    public ServerboundUnpinMilestoneMessage() {
        this.milestoneId = "";
    }
    public ServerboundUnpinMilestoneMessage(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    @Override
    public ServerboundUnpinMilestoneMessage read(FriendlyByteBuf buffer) {
        return new ServerboundUnpinMilestoneMessage(buffer.readUtf());
    }

    @Override
    public void write(ServerboundUnpinMilestoneMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.milestoneId);
    }

    @Override
    public void onMessage(ServerboundUnpinMilestoneMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
              Milestones.unpin(player, message.milestoneId);
            }
        });
        ctx.setPacketHandled(true);
    }
}
