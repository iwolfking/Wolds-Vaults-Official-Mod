package xyz.iwolfking.woldsvaults.integration.sophisticatedstorage;

import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import xyz.iwolfking.vhapi.api.util.MessageUtils;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;

public class StorageContainerDeposit {
    private StorageContainerDeposit() {
    }

    public static void deposit(ServerPlayer player, StorageContainerMenuBase<?> menu, ItemStack backpack) {
        if (!isResearched(player)) {
            MessageUtils.sendMessage(player, "Altar Automation research is required.", ChatFormatting.RED);
            return;
        }

        if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem)) {
            MessageUtils.sendMessage(player, "No backpack in your curios or inventory", ChatFormatting.RED);
            return;
        }

        long[] result = backpack
                .getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                .map(wrapper -> depositContents(
                        wrapper.getInventoryHandler(),
                        menu.getStorageWrapper().getInventoryHandler(),
                        player
                ))
                .orElse(new long[]{0L, 0L});

        long deposited = result[0];
        long leftover = result[1];

        if (deposited <= 0) {
            MessageUtils.sendMessage(
                    player,
                    leftover > 0
                            ? "The storage has no room for your backpack items"
                            : "Nothing in your backpack to deposit",
                    ChatFormatting.RED
            );
            return;
        }

        player.level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.6F,
                0.8F
        );

        MessageUtils.sendMessage(
                player,
                leftover > 0
                        ? "Deposited " + deposited + " items, " + leftover + " did not fit"
                        : "Deposited " + deposited + " items",
                leftover > 0 ? ChatFormatting.YELLOW : ChatFormatting.GREEN
        );
    }

    private static long[] depositContents(
            InventoryHandler inventory,
            IItemHandler destination,
            ServerPlayer player
    ) {
        long[] counts = new long[2];

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);

            if (stack.isEmpty() || BackpackUtils.isNestedContainer(stack)) {
                continue;
            }

            ItemStack extracted = inventory.extractItem(
                    slot,
                    stack.getCount(),
                    false
            );

            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack remainder = insertIntoStorage(destination, extracted);

            counts[0] += extracted.getCount() - remainder.getCount();

            if (!remainder.isEmpty()) {
                ItemStack couldNotReturn = inventory.insertItem(slot, remainder, false);

                if (!couldNotReturn.isEmpty()) {
                    player.getInventory().add(couldNotReturn);
                }

                counts[1] += remainder.getCount();
            }
        }

        return counts;
    }

    private static ItemStack insertIntoStorage(
            IItemHandler destination,
            ItemStack stack
    ) {
        ItemStack remainder = stack.copy();

        for (int slot = 0; slot < destination.getSlots() && !remainder.isEmpty(); slot++) {
            remainder = destination.insertItem(slot, remainder, false);
        }

        return remainder;
    }

    private static boolean isResearched(ServerPlayer player) {
        PlayerResearchesData data = PlayerResearchesData.get(player.getLevel());
        return data.getResearches(player).isResearched("Altar Automation");
    }
}
