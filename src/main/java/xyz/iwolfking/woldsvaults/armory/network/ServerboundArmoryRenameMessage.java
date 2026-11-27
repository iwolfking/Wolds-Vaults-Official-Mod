package xyz.iwolfking.woldsvaults.armory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.armory.ArmoryPage;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryMenu;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryNavigation;
import xyz.iwolfking.woldsvaults.armory.service.ArmoryService;

import java.util.function.Supplier;

/** Renames the open armory page; a blank name clears it. */
public record ServerboundArmoryRenameMessage(int containerId, String name) {
    private static final int MAX_WIRE_LENGTH = ArmoryPage.MAX_NAME_LENGTH * 4;

    public static void encode(ServerboundArmoryRenameMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.containerId);
        buf.writeUtf(message.name, MAX_WIRE_LENGTH);
    }

    public static ServerboundArmoryRenameMessage decode(FriendlyByteBuf buf) {
        return new ServerboundArmoryRenameMessage(buf.readVarInt(), buf.readUtf(MAX_WIRE_LENGTH));
    }

    public static void handle(ServerboundArmoryRenameMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !(player.containerMenu instanceof ArmoryMenu menu) || menu.containerId != message.containerId
                    || !(player.getLevel().getBlockEntity(menu.pos()) instanceof ArmoryBlockEntity armory)) {
                return;
            }
            ArmoryService.rename(player, armory, menu.page(), message.name);
            ArmoryNavigation.sendState(player, menu, armory);
        });
        ctx.get().setPacketHandled(true);
    }
}
