package xyz.iwolfking.woldsvaults.api.util;


import iskallia.vault.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.items.SlotItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.deposit.DepositUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import top.theillusivec4.curios.api.CuriosApi;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.ClientboundBackpackContentsRefreshPacket;

import java.util.Optional;
import java.util.UUID;

public class BackpackUtils {

    public static ItemStack findBackpack(Player player) {
        Optional<ItemStack> backpack = InventoryUtil.findAllItems(player).stream()
                .map(InventoryUtil.ItemAccess::getStack)
                .filter(itemStack -> !itemStack.isEmpty()
                        && itemStack.getItem() instanceof BackpackItem
                        && hasUpgradeOfType(DepositUpgradeWrapper.class, itemStack)
                        && !isBackpackEmpty(itemStack))
                .findFirst();
        return backpack.orElse(ItemStack.EMPTY);
    }

    public static boolean isBackpackEmpty(ItemStack stack) {
        return stack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                .map(wrapper -> {
                    Optional<UUID> uuid = wrapper.getContentsUuid();
                    if (uuid.isEmpty()) {
                        return true;
                    }

                    CompoundTag contents = BackpackStorage.get()
                            .getOrCreateBackpackContents(uuid.get());

                    CompoundTag inventory = contents.getCompound("inventory");
                    ListTag items = inventory.getList("Items", Tag.TAG_COMPOUND);

                    return items.isEmpty();
                })
                .orElse(true);
    }

    public static void sendRefreshPacket(ItemStack backpack, ServerPlayer player) {
        backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                .ifPresent(w -> w.getContentsUuid()
                        .ifPresent(uuid -> ModNetwork.sendToClient(
                                new ClientboundBackpackContentsRefreshPacket(uuid, BackpackStorage.get().getOrCreateBackpackContents(uuid)), player)));
    }

    public static boolean hasUpgradeOfType(Class<? extends UpgradeWrapperBase<?, ?>> upgradeType, ItemStack stack) {
        return stack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                .map(iBackpackWrapper -> !iBackpackWrapper.getUpgradeHandler().getWrappersThatImplement(upgradeType).isEmpty())
                .orElse(false);
    }

    public static void refreshWrapper(ItemStack stack, UUID uuid) {
        stack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).ifPresent(wrapper -> {
            if (wrapper.getContentsUuid().map(uuid::equals).orElse(false)) {
                wrapper.onContentsNbtUpdated();
            }
        });
    }

    public static Optional<InventoryHandler> getBackpackInventory(ItemStack backpack) {
        return backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).map(IBackpackWrapper::getInventoryHandler);
    }

    public static boolean isNestedContainer(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem) return true;
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock && stack.hasTag()) {
            return stack.getTag().contains("BlockEntityTag");
        }
        return false;
    }

    public static ItemStack getCuriosBackpack(ServerPlayer player) {
        var curiosItem = CuriosApi.getCuriosHelper().findFirstCurio(player, itemStack -> itemStack.getItem() instanceof BackpackItem);
        if (curiosItem.isPresent()) {
            return curiosItem.get().stack();
        }
        return ItemStack.EMPTY;
    }

    public static long[] depositInventoryToMenu(
            ItemStack backpack,
            AbstractContainerMenu menu,
            int startSlot,
            int endSlot,
            ServerPlayer player
    ) {
        long[] counts = new long[2];

        InventoryHandler backpackInventory = BackpackUtils.getBackpackInventory(backpack).orElse(null);
        if (backpackInventory == null) {
            return counts;
        }

        for (int slot = 0; slot < backpackInventory.getSlots(); slot++) {
            ItemStack stack = backpackInventory.getStackInSlot(slot);

            if (stack.isEmpty() || BackpackUtils.isNestedContainer(stack)) {
                continue;
            }
            if (stack.isEmpty() || BackpackUtils.isNestedContainer(stack)) {
                continue;
            }

            ItemStack extracted = backpackInventory.extractItem(
                    slot,
                    stack.getCount(),
                    false
            );

            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack inputStack = extracted.copy();

            for (int i = startSlot; i < endSlot; i++) {
                if (inputStack.isEmpty()) break;

                Slot destSlot = menu.getSlot(i);

                if (destSlot.container == player.getInventory()) {
                    continue;
                }

                if (!destSlot.mayPlace(inputStack)) {
                    continue;
                }

                if (!destSlot.container.canPlaceItem(destSlot.getContainerSlot(), inputStack)) {
                    continue;
                }

                if(destSlot instanceof SlotItemHandler slotItemHandler) {
                    if(!slotItemHandler.mayPlace(inputStack)) {
                        continue;
                    }
                }

                if(!destSlot.mayPickup(player)) {
                    continue;
                }

                ItemStack slotStack = destSlot.getItem();

                if (!slotStack.isEmpty()) {
                    if (ItemStack.isSameItemSameTags(slotStack, inputStack)) {
                        int maxStackSize = Math.min(destSlot.getMaxStackSize(inputStack), inputStack.getMaxStackSize());
                        int spaceLeft = maxStackSize - slotStack.getCount();

                        if (spaceLeft > 0) {
                            int amountToMove = Math.min(inputStack.getCount(), spaceLeft);
                            slotStack.grow(amountToMove);
                            inputStack.shrink(amountToMove);
                            destSlot.setChanged();
                        }
                    }
                } else {
                    // Placement logic for empty slots
                    int maxStackSize = Math.min(destSlot.getMaxStackSize(inputStack), inputStack.getMaxStackSize());
                    int amountToMove = Math.min(inputStack.getCount(), maxStackSize);

                    destSlot.set(inputStack.split(amountToMove));
                    destSlot.setChanged();
                }
            }

            long insertedCount = extracted.getCount() - inputStack.getCount();
            counts[0] += insertedCount;

            if (!inputStack.isEmpty()) {
                ItemStack remainder = backpackInventory.insertItem(slot, inputStack, false);

                if (!remainder.isEmpty()) {
                    for (int s = 0; s < backpackInventory.getSlots(); s++) {
                        if (remainder.isEmpty()) break;
                        remainder = backpackInventory.insertItem(s, remainder, false);
                    }
                }
            }
        }

        menu.broadcastChanges();
        return counts;
    }
}