package xyz.iwolfking.woldsvaults.armory.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryClientState;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryMenu;

import java.util.function.Supplier;

/** The open page's marks, layout and stat snapshot, plus a fresh header, for the player's open armory menu. */
public record ClientboundArmoryStateMessage(int containerId, ArmoryViewInfo info, ArmoryClientState state) {
    public static void encode(ClientboundArmoryStateMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.containerId);
        message.info.write(buf);
        message.state.write(buf);
    }

    public static ClientboundArmoryStateMessage decode(FriendlyByteBuf buf) {
        return new ClientboundArmoryStateMessage(buf.readVarInt(), ArmoryViewInfo.read(buf), ArmoryClientState.read(buf));
    }

    public static void handle(ClientboundArmoryStateMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> apply(message));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void apply(ClientboundArmoryStateMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.containerMenu instanceof ArmoryMenu menu
                && menu.containerId == message.containerId) {
            menu.acceptState(message.info, message.state);
        }
    }
}
