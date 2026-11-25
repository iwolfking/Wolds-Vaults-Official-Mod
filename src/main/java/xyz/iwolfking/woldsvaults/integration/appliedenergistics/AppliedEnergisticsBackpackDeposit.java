package xyz.iwolfking.woldsvaults.integration.appliedenergistics;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.menu.me.common.MEStorageMenu;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.deposit.DepositFilterLogic;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.vhapi.api.util.MessageUtils;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;
import xyz.iwolfking.woldsvaults.mixins.ae2.accessors.AccessorMEStorageMenu;

import java.util.List;

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

        List<DepositFilterLogic> depositFilterLogics = BackpackUtils.getDepositUpgradeFilters(backpack);

        long[] result = BackpackUtils.getBackpackInventory(backpack)
                .map(wrapper -> depositContents(
                        depositFilterLogics,
                        wrapper,
                        storage,
                        powerSource,
                        menu,
                        menuAccess
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
                        ? "Deposited " + deposited + " items, " + leftover + " were not deposited"
                        : "Deposited " + deposited + " items",
                leftover > 0 ? ChatFormatting.YELLOW : ChatFormatting.GREEN
        );
    }

    private static long[] depositContents(
            List<DepositFilterLogic> depositFilterLogics,
            InventoryHandler inventory,
            MEStorage storage,
            IEnergySource powerSource,
            MEStorageMenu menu,
            AccessorMEStorageMenu menuAccess
    ) {
        long[] counts = new long[2];

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!menuAccess.invokeCanInteractWithGrid()) {
                break;
            }

            if (powerSource.extractAEPower(1.0D, Actionable.SIMULATE, PowerMultiplier.CONFIG) <= 0.0D) {
                break;
            }

            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (slotStack.isEmpty() || BackpackUtils.isNestedContainer(slotStack)) {
                continue;
            }

            if (!BackpackUtils.matchesDepositFilters(depositFilterLogics, slotStack)) {
                continue;
            }

            AEItemKey key = AEItemKey.of(slotStack);
            if (key == null) {
                continue;
            }

            int originalCount = slotStack.getCount();

            long simulatedInsertable = storage.insert(
                    key,
                    originalCount,
                    Actionable.SIMULATE,
                    menu.getActionSource()
            );

            if (simulatedInsertable <= 0) {
                continue;
            }

            long inserted = StorageHelper.poweredInsert(
                    powerSource,
                    storage,
                    key,
                    simulatedInsertable,
                    menu.getActionSource()
            );

            int accepted = (int) Math.min(originalCount, Math.max(0L, inserted));
            if (accepted > 0) {
                ItemStack extracted = inventory.extractItem(slot, accepted, false);
                counts[0] += extracted.getCount();

                if (extracted.getCount() < accepted) {
                    counts[0] -= (accepted - extracted.getCount());
                }
            }

            if (accepted < simulatedInsertable
                    || powerSource.extractAEPower(1.0D, Actionable.SIMULATE, PowerMultiplier.CONFIG) <= 0.0D) {
                break;
            }
        }

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            counts[1] += inventory.getStackInSlot(slot).getCount();
        }

        return counts;
    }

    private static boolean isResearched(ServerPlayer player) {
        PlayerResearchesData data = PlayerResearchesData.get(player.getLevel());
        return data.getResearches(player).isResearched(WoldConstants.DELOOT_RESEARCH);
    }
}