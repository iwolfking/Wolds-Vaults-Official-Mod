package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.blocks.LoadingZoneMarkerBlock;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.loadingzone.LoadingZoneShape;

import java.util.function.Supplier;

// A builder's settings for a loading zone post or arrival marker.
public record LoadingZoneConfigMessage(BlockPos pos, String link, int height, int order, int rotation, LoadingZoneShape.Leads leads, String pocket) {
    public static void encode(LoadingZoneConfigMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeUtf(message.link, LoadingZoneMarkerBlock.MAX_LINK_LENGTH);
        buffer.writeVarInt(message.height);
        buffer.writeVarInt(message.order);
        buffer.writeVarInt(message.rotation);
        buffer.writeVarInt(message.leads.ordinal());
        buffer.writeUtf(message.pocket, LoadingZoneMarkerBlock.MAX_POCKET_LENGTH);
    }

    public static LoadingZoneConfigMessage decode(FriendlyByteBuf buffer) {
        return new LoadingZoneConfigMessage(buffer.readBlockPos(), buffer.readUtf(LoadingZoneMarkerBlock.MAX_LINK_LENGTH), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                LoadingZoneShape.Leads.byId(buffer.readVarInt()), buffer.readUtf(LoadingZoneMarkerBlock.MAX_POCKET_LENGTH));
    }

    public static void handle(LoadingZoneConfigMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onServer(context, player -> LoadingZoneMarkerBlock.configure(player, message.pos, message.link, message.height, message.order, message.rotation, message.leads, message.pocket));
    }
}
