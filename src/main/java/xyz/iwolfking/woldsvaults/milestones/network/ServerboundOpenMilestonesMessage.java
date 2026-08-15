package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.milestones.MilestoneFlusher;
import xyz.iwolfking.woldsvaults.milestones.container.GreedMilestonesContainer;

import java.util.function.Supplier;

/**
 * Serverbound request to open the greed tab of the player menu. Opens the slotless
 * {@link GreedMilestonesContainer} and pushes a full milestone sync so the screen never renders a
 * stale header while it waits for the next delta beat.
 */
public class ServerboundOpenMilestonesMessage extends Message<ServerboundOpenMilestonesMessage> {
    public ServerboundOpenMilestonesMessage() {
    }

    @Override
    public ServerboundOpenMilestonesMessage read(FriendlyByteBuf buffer) {
        return new ServerboundOpenMilestonesMessage();
    }

    @Override
    public void write(ServerboundOpenMilestonesMessage message, FriendlyByteBuf buffer) {
    }

    @Override
    public void onMessage(ServerboundOpenMilestonesMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            NetworkHooks.openGui(player, new SimpleMenuProvider(
                    (windowId, inventory, owner) -> new GreedMilestonesContainer(windowId, owner),
                    new TextComponent("Greed")));
            MilestoneFlusher.syncAll(player);
        });
        ctx.setPacketHandled(true);
    }
}
