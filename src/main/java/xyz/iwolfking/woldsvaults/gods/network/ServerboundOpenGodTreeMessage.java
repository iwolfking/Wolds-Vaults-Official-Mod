package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.container.GodTreeContainer;

import java.util.function.Supplier;

/** Opens the gods tab: the slotless {@link GodTreeContainer} plus a fresh alignment sync. */
public class ServerboundOpenGodTreeMessage extends Message<ServerboundOpenGodTreeMessage> {
    public ServerboundOpenGodTreeMessage() {
    }

    @Override
    public ServerboundOpenGodTreeMessage read(FriendlyByteBuf buffer) {
        return new ServerboundOpenGodTreeMessage();
    }

    @Override
    public void write(ServerboundOpenGodTreeMessage message, FriendlyByteBuf buffer) {
    }

    @Override
    public void onMessage(ServerboundOpenGodTreeMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.getServer() == null) {
                return;
            }
            NetworkHooks.openGui(player, new SimpleMenuProvider(
                    (windowId, inventory, owner) -> new GodTreeContainer(windowId, owner),
                    new TextComponent("Gods")));
            GodAlignmentData.get(player.getServer()).sync(player);
        });
        ctx.setPacketHandled(true);
    }
}
