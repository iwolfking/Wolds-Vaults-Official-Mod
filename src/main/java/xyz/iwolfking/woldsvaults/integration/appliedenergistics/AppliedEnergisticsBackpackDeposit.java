package xyz.iwolfking.woldsvaults.integration.appliedenergistics;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.menu.me.common.MEStorageMenu;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;
import xyz.iwolfking.woldsvaults.mixins.ae2.accessors.AccessorMEStorageMenu;

public class AppliedEnergisticsBackpackDeposit {
    private AppliedEnergisticsBackpackDeposit() {
    }

    public static void deposit(ServerPlayer player, MEStorageMenu menu) {
        AccessorMEStorageMenu menuAccess = (AccessorMEStorageMenu) menu;
        MEStorage storage = menuAccess.getStorage();
        IEnergySource powerSource = menuAccess.getPowerSource();
        if (storage != null && powerSource != null && menuAccess.invokeCanInteractWithGrid()) {
            if (!isResearched(player)) {
                player.displayClientMessage(new TextComponent("Altar Automation research is required.").withStyle(ChatFormatting.RED), false);
                return;
            }
            ItemStack backpack = BackpackUtils.findBackpack(player);
            if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem)) {
                player.displayClientMessage(new TextComponent("No backpack in your curios or inventory").withStyle(ChatFormatting.RED), false);
            } else {
                long[] depositedAndLeftover = backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                        .map(wrapper -> depositContents(wrapper.getInventoryHandler(), storage, powerSource, menu))
                        .orElse(new long[]{0L, 0L});
                long deposited = depositedAndLeftover[0];
                long leftover = depositedAndLeftover[1];
                if (deposited <= 0L) {
                    player.displayClientMessage(new TextComponent(leftover > 0L ? "The network has no room for your backpack items" : "Nothing in your backpack to deposit").withStyle(ChatFormatting.RED), false);
                } else {
                    player.level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6F, 0.8F);
                    BackpackUtils.sendRefreshPacket(backpack, player);
                    player.displayClientMessage(new TextComponent(leftover > 0L ? "Deposited " + deposited + " items, " + leftover + " did not fit" : "Deposited " + deposited + " items").withStyle(leftover > 0L ? ChatFormatting.YELLOW : ChatFormatting.GREEN), false);
                }
            }
        } else {
            player.displayClientMessage(new TextComponent("The terminal is not connected to a powered network").withStyle(ChatFormatting.RED), false);
        }
    }

    private static boolean isResearched(ServerPlayer player) {
        PlayerResearchesData data = PlayerResearchesData.get(player.getLevel());
        return data.getResearches(player).isResearched("Altar Automation");
    }

    private static long[] depositContents(InventoryHandler inventory, MEStorage storage, IEnergySource powerSource, MEStorageMenu menu) {
        long deposited = 0L;
        long leftover = 0L;

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (!slotStack.isEmpty() && !BackpackUtils.isNestedContainer(slotStack)) {
                AEItemKey key = AEItemKey.of(slotStack);
                int remaining = slotStack.getCount();

                while (remaining > 0) {
                    ItemStack extracted = inventory.extractItem(slot, remaining, false);
                    if (extracted.isEmpty()) {
                        break;
                    }

                    long inserted = StorageHelper.poweredInsert(powerSource, storage, key, extracted.getCount(), menu.getActionSource());
                    deposited += inserted;
                    remaining -= extracted.getCount();
                    if (inserted < extracted.getCount()) {
                        ItemStack notInserted = extracted.copy();
                        notInserted.setCount(extracted.getCount() - (int) inserted);
                        ItemStack couldNotReturn = inventory.insertItem(slot, notInserted, false);
                        if (!couldNotReturn.isEmpty()) {
                            menu.getPlayer().getInventory().add(couldNotReturn);
                        }
                        break;
                    }
                }

                leftover += inventory.getStackInSlot(slot).getCount();
            }
        }

        return new long[]{deposited, leftover};
    }

}
