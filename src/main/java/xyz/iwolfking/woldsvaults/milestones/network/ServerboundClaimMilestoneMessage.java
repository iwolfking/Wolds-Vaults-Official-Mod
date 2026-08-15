package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.milestones.Milestones;

import java.util.function.Supplier;

/**
 * Serverbound claim request from the greed trader screen. An empty id claims everything at once.
 * The server re-checks that the greed trader container is open before paying anything out.
 */
public class ServerboundClaimMilestoneMessage extends Message<ServerboundClaimMilestoneMessage> {
    public static final String CLAIM_ALL = "";

    private final String milestoneId;

    public ServerboundClaimMilestoneMessage() {
        this.milestoneId = CLAIM_ALL;
    }

    public ServerboundClaimMilestoneMessage(String milestoneId) {
        this.milestoneId = milestoneId == null ? CLAIM_ALL : milestoneId;
    }

    @Override
    public ServerboundClaimMilestoneMessage read(FriendlyByteBuf buffer) {
        return new ServerboundClaimMilestoneMessage(buffer.readUtf());
    }

    @Override
    public void write(ServerboundClaimMilestoneMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.milestoneId);
    }

    @Override
    public void onMessage(ServerboundClaimMilestoneMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (CLAIM_ALL.equals(message.milestoneId)) {
                Milestones.claimAll(player);
            } else {
                Milestones.claim(player, message.milestoneId);
            }
        });
        ctx.setPacketHandled(true);
    }
}
