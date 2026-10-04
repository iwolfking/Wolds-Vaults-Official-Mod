package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchMenu;

import java.util.function.Supplier;

public class ServerboundOpenTrinketPouchPacket {
    private final int containerId;
    private final int pouchSlot;

    public ServerboundOpenTrinketPouchPacket(int containerId, int pouchSlot) {
        this.containerId = containerId;
        this.pouchSlot = pouchSlot;
    }

    public static void encode(ServerboundOpenTrinketPouchPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.containerId);
        buf.writeInt(packet.pouchSlot);
    }

    public static ServerboundOpenTrinketPouchPacket decode(FriendlyByteBuf buf) {
        return new ServerboundOpenTrinketPouchPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(ServerboundOpenTrinketPouchPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu.containerId == packet.containerId) {
                PouchMenu.open(player, packet.pouchSlot);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
