package xyz.iwolfking.woldsvaults.armory.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.armory.ArmoryTab;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryNavigation;

import java.util.function.Supplier;

/** Client asks to open another tab or page of an armory it is looking at. */
public record ServerboundArmoryNavMessage(BlockPos pos, int page, ArmoryTab tab) {
    public static void encode(ServerboundArmoryNavMessage message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeVarInt(message.page);
        buf.writeEnum(message.tab);
    }

    public static ServerboundArmoryNavMessage decode(FriendlyByteBuf buf) {
        return new ServerboundArmoryNavMessage(buf.readBlockPos(), buf.readVarInt(), buf.readEnum(ArmoryTab.class));
    }

    public static void handle(ServerboundArmoryNavMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ArmoryNavigation.open(player, message.pos, message.page, message.tab);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
