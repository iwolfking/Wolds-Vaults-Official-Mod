package xyz.iwolfking.woldsvaults.armory.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryMenu;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryNavigation;
import xyz.iwolfking.woldsvaults.armory.service.ArmoryService;

import java.util.function.Supplier;

/**
 * Save on the open armory page. Carries the H-screen stat lists the client computed for itself, which the page
 * shows on its Gear tab (display only; nothing reads them back as game state).
 */
public record ServerboundArmorySaveMessage(int containerId, boolean overwrite, CompoundTag stats) {
    public static void encode(ServerboundArmorySaveMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.containerId);
        buf.writeBoolean(message.overwrite);
        buf.writeNbt(message.stats);
    }

    public static ServerboundArmorySaveMessage decode(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        boolean overwrite = buf.readBoolean();
        CompoundTag stats = buf.readNbt();
        return new ServerboundArmorySaveMessage(containerId, overwrite, stats == null ? new CompoundTag() : stats);
    }

    public static void handle(ServerboundArmorySaveMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !(player.containerMenu instanceof ArmoryMenu menu) || menu.containerId != message.containerId
                    || !(player.getLevel().getBlockEntity(menu.pos()) instanceof ArmoryBlockEntity armory)) {
                return;
            }
            ArmoryService.save(player, armory, menu.page(), message.stats, message.overwrite);
            menu.broadcastChanges();
            ArmoryNavigation.sendState(player, menu, armory);
        });
        ctx.get().setPacketHandled(true);
    }
}
