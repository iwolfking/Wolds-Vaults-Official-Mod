package xyz.iwolfking.woldsvaults.network.packets;

import iskallia.vault.item.gear.TrinketItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.effect.trinkets.SpeedLimitTrinketEffect;

import java.util.function.Supplier;

public class ServerboundSetTrinketSpeedCapPacket {
    private final int capPercent;
    private final boolean creativeInventory;
    private final int slotIndex;

    public ServerboundSetTrinketSpeedCapPacket(int capPercent, boolean creativeInventory, int slotIndex) {
        this.capPercent = capPercent;
        this.creativeInventory = creativeInventory;
        this.slotIndex = slotIndex;
    }

    public static void encode(ServerboundSetTrinketSpeedCapPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.capPercent);
        buf.writeBoolean(packet.creativeInventory);
        buf.writeInt(packet.slotIndex);
    }

    public static ServerboundSetTrinketSpeedCapPacket decode(FriendlyByteBuf buf) {
        return new ServerboundSetTrinketSpeedCapPacket(buf.readInt(), buf.readBoolean(), buf.readInt());
    }

    public static void handle(ServerboundSetTrinketSpeedCapPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || packet.slotIndex < 0) {
                return;
            }
            ItemStack stack;
            AbstractContainerMenu menu;
            if (packet.creativeInventory) {
                menu = player.inventoryMenu;
                stack = player.getInventory().getItem(packet.slotIndex);
            } else {
                menu = player.containerMenu;
                if (packet.slotIndex >= menu.slots.size()) {
                    WoldsVaults.LOGGER.warn("Weighted Boots: speed cap packet from {} targets menu slot {} but the open menu only has {} slots, ignoring.", player.getGameProfile().getName(), packet.slotIndex, menu.slots.size());
                    return;
                }
                stack = menu.getSlot(packet.slotIndex).getItem();
            }
            if (!(stack.getItem() instanceof TrinketItem) || !TrinketItem.isIdentified(stack) || !(TrinketItem.getTrinket(stack).orElse(null) instanceof SpeedLimitTrinketEffect)) {
                WoldsVaults.LOGGER.warn("Weighted Boots: speed cap packet from {} targets slot {} which does not hold identified Weighted Boots, ignoring.", player.getGameProfile().getName(), packet.slotIndex);
                return;
            }
            SpeedLimitTrinketEffect.setCapPercent(stack, packet.capPercent);
            menu.broadcastChanges();
        });
        ctx.get().setPacketHandled(true);
    }
}
