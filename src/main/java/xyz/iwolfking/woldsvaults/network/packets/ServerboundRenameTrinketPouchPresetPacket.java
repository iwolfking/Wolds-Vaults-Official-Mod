package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchMenu;

import java.util.function.Supplier;

public class ServerboundRenameTrinketPouchPresetPacket {
    private final int containerId;
    private final int preset;
    private final String name;

    public ServerboundRenameTrinketPouchPresetPacket(int containerId, int preset, String name) {
        this.containerId = containerId;
        this.preset = preset;
        this.name = name;
    }

    public static void encode(ServerboundRenameTrinketPouchPresetPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.containerId);
        buf.writeVarInt(packet.preset);
        buf.writeUtf(packet.name, PouchContents.MAX_PRESET_NAME_LENGTH);
    }

    public static ServerboundRenameTrinketPouchPresetPacket decode(FriendlyByteBuf buf) {
        return new ServerboundRenameTrinketPouchPresetPacket(buf.readInt(), buf.readVarInt(), buf.readUtf(PouchContents.MAX_PRESET_NAME_LENGTH));
    }

    public static void handle(ServerboundRenameTrinketPouchPresetPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof PouchMenu menu && menu.containerId == packet.containerId) {
                menu.renamePreset(player, packet.preset, packet.name);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
