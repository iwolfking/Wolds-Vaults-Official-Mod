package xyz.iwolfking.woldsvaults.integration.refinedstorage.packet;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import java.util.function.Supplier;

import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;

public record ServerboundDepositBackpackToRSGridPacket() {
    public static void encode(ServerboundDepositBackpackToRSGridPacket packet, FriendlyByteBuf buffer) {
    }

    public static ServerboundDepositBackpackToRSGridPacket decode(FriendlyByteBuf buffer) {
        return new ServerboundDepositBackpackToRSGridPacket();
    }

    public static void handle(ServerboundDepositBackpackToRSGridPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                AbstractContainerMenu container = player.containerMenu;
                if (container instanceof GridContainerMenu menu) {
                    IGrid grid = menu.getGrid();
                    if (grid != null && grid.getItemHandler() != null) {
                        if (!checkResearch(player)) return;
                        ItemStack backpack = BackpackUtils.findBackpack(player);
                        if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem)) {
                            player.displayClientMessage(new TextComponent("No backpack in your curios or inventory").withStyle(ChatFormatting.RED), false);
                        } else {
                            backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).ifPresent(wrapper -> {
                                InventoryHandler inventory = wrapper.getInventoryHandler();
                                long deposited = 0L;
                                long leftover = 0L;
                                for (int slot = 0; slot < inventory.getSlots(); slot++) {
                                    ItemStack slotStack = inventory.getStackInSlot(slot);
                                    if (!slotStack.isEmpty()) {
                                        ItemStack extracted = inventory.extractItem(slot, slotStack.getCount(), false);
                                        if (!extracted.isEmpty()) {
                                            ItemStack remainder = grid.getItemHandler().onInsert(player, extracted, false);
                                            deposited += extracted.getCount() - (remainder.isEmpty() ? 0 : remainder.getCount());
                                            if (!remainder.isEmpty()) {
                                                ItemStack couldNotReturn = inventory.insertItem(slot, remainder, false);
                                                if (!couldNotReturn.isEmpty()) {
                                                    player.getInventory().add(couldNotReturn);
                                                }
                                                leftover += remainder.getCount();
                                            }
                                        }
                                    }
                                }
                                if (deposited <= 0L) {
                                    player.displayClientMessage(new TextComponent(leftover > 0L ? "The network has no room for your backpack items" : "Nothing in your backpack to deposit").withStyle(ChatFormatting.RED), false);
                                } else {
                                    player.displayClientMessage(new TextComponent(leftover > 0L ? "Deposited " + deposited + " items, " + leftover + " did not fit" : "Deposited " + deposited + " items").withStyle(leftover > 0L ? ChatFormatting.YELLOW : net.minecraft.ChatFormatting.GREEN), true);
                                    BackpackUtils.sendRefreshPacket(backpack, player);
                                    player.inventoryMenu.broadcastChanges();
                                }
                            });
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static boolean checkResearch(ServerPlayer player) {
        PlayerResearchesData data = PlayerResearchesData.get(player.getLevel());
        return data.getResearches(player).isResearched("Altar Automation");
    }
}
