package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.util.InventoryUtil;
import net.joseph.vaultfilters.mixin.compat.sophisticatedcore.mixin.InventoryHandlerSlotTrackerAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.deposit.DepositUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.ClientboundBackpackContentsRefreshPacket;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackpackUtils {

    public static ItemStack findBackpack(Player player) {

        Optional<ItemStack> backpack = InventoryUtil.findAllItems(player).stream().map(InventoryUtil.ItemAccess::getStack).filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof BackpackItem && hasUpgradeOfType(DepositUpgradeWrapper.class, itemStack) && !isBackpackEmpty(itemStack)).findFirst();
        return backpack.orElse(ItemStack.EMPTY);
    }

    public static boolean isBackpackEmpty(ItemStack stack) {
        AtomicBoolean empty = new AtomicBoolean(false);
        stack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).ifPresent((iBackpackWrapper) -> empty.set(((InventoryHandlerSlotTrackerAccessor)iBackpackWrapper.getInventoryHandler().getSlotTracker()).getEmptySlots().size() == ((BackpackItem)stack.getItem()).getNumberOfSlots()));
        return empty.get();
    }

    public static void sendRefreshPacket(ItemStack backpack, ServerPlayer player) {
        backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).ifPresent(w -> w.getContentsUuid().ifPresent(uuid -> ModNetwork.sendToClient(new ClientboundBackpackContentsRefreshPacket(uuid, BackpackStorage.get().getOrCreateBackpackContents(uuid)), player)));
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

    public static boolean isNestedContainer(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem) return true;
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock && stack.hasTag()) {
            return stack.getTag().contains("BlockEntityTag");
        }
        return false;
    }

}
