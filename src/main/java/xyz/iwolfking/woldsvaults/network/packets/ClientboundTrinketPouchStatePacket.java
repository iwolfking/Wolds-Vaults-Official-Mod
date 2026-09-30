package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchMenu;

import java.util.function.Supplier;

public class ClientboundTrinketPouchStatePacket {
    private final int containerId;
    private final CompoundTag state;

    public ClientboundTrinketPouchStatePacket(int containerId, CompoundTag state) {
        this.containerId = containerId;
        this.state = state;
    }

    public static void encode(ClientboundTrinketPouchStatePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.containerId);
        buf.writeNbt(packet.state);
    }

    public static ClientboundTrinketPouchStatePacket decode(FriendlyByteBuf buf) {
        return new ClientboundTrinketPouchStatePacket(buf.readVarInt(), buf.readNbt());
    }

    public static void handle(ClientboundTrinketPouchStatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> applyState(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyState(ClientboundTrinketPouchStatePacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.containerMenu instanceof PouchMenu menu
                && menu.containerId == packet.containerId && packet.state != null) {
            menu.receiveState(packet.state);
        }
    }
}
