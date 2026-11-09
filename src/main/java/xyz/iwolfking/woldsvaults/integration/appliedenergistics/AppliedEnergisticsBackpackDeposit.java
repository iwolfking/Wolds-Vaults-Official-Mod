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
import net.minecraftforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.vhapi.api.util.MessageUtils;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;
import xyz.iwolfking.woldsvaults.mixins.ae2.accessors.AccessorMEStorageMenu;

public class AppliedEnergisticsBackpackDeposit {
    private AppliedEnergisticsBackpackDeposit() {
    }

    public static void deposit(ServerPlayer player, MEStorageMenu menu, ItemStack backpack) {
        AccessorMEStorageMenu menuAccess = (AccessorMEStorageMenu) menu;
        MEStorage storage = menuAccess.getStorage();
        IEnergySource powerSource = menuAccess.getPowerSource();

        if (storage == null || powerSource == null || !menuAccess.invokeCanInteractWithGrid()) {
            MessageUtils.sendMessage(player, "The terminal is not connected to a powered network", ChatFormatting.RED);
            return;
        }

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
                        wrapper,
                        storage,
                        powerSource,
                        menu
                ))
                .orElse(new long[]{0L, 0L});

        long deposited = result[0];
        long leftover = result[1];

        if (deposited <= 0) {
            MessageUtils.sendMessage(
                    player,
                    leftover > 0
                            ? "The network has no room for your backpack items"
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
            IStorageWrapper wrapper,
            MEStorage storage,
            IEnergySource powerSource,
            MEStorageMenu menu
    ) {
        ITrackedContentsItemHandler inventory = wrapper.getInventoryForUpgradeProcessing();

        long[] counts = new long[2];

        IItemHandler destination = new IItemHandler() {
            @Override
            public int getSlots() {
                return inventory.getSlots();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty() || BackpackUtils.isNestedContainer(stack)) {
                    return stack;
                }

                AEItemKey key = AEItemKey.of(stack);
                if (key == null) {
                    return stack;
                }

                if (simulate) {
                    long available = storage.insert(
                            key,
                            stack.getCount(),
                            appeng.api.config.Actionable.SIMULATE,
                            menu.getActionSource()
                    );
                    int accepted = (int) Math.min(stack.getCount(), available);
                    ItemStack remainder = stack.copy();
                    remainder.shrink(accepted);
                    return remainder;
                }

                long inserted = StorageHelper.poweredInsert(
                        powerSource,
                        storage,
                        key,
                        stack.getCount(),
                        menu.getActionSource()
                );

                int accepted = (int) Math.min(stack.getCount(), inserted);
                counts[0] += accepted;

                ItemStack remainder = stack.copy();
                remainder.shrink(accepted);
                return remainder;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return !stack.isEmpty() && !BackpackUtils.isNestedContainer(stack);
            }
        };

        InventoryHelper.transfer(
                inventory,
                destination,
                extracted -> {
                    // InventoryHelper performs the actual extraction and insertion.
                }
        );

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            counts[1] += inventory.getStackInSlot(slot).getCount();
        }

        return counts;
    }

    private static boolean isResearched(ServerPlayer player) {
        PlayerResearchesData data = PlayerResearchesData.get(player.getLevel());
        return data.getResearches(player).isResearched("Altar Automation");
    }
}