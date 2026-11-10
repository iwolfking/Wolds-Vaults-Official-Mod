package xyz.iwolfking.woldsvaults.integration.fruit_pouch;

import com.shiftthedev.vaultfruitbag.VFBRegistry;
import com.shiftthedev.vaultfruitbag.config.VFBConfig;
import com.shiftthedev.vaultfruitbag.containers.FruitBagContainer;
import com.shiftthedev.vaultfruitbag.helpers.FruitBagHelper;
import iskallia.vault.item.ItemVaultFruit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;

public class FruitPouchHelper {
    public static boolean interceptPlayerInventoryItemAddition(Inventory playerInventory, ItemStack toAdd) {
        if (!VFBConfig.validFruits.contains(toAdd.getItem()) || toAdd.isEmpty()) {
            return false;
        }

        ItemStack fruitBag = ItemStack.EMPTY;
        int bagSlot = -1;

        for (int slot = 0; slot < playerInventory.getContainerSize(); slot++) {
            ItemStack invStack = playerInventory.getItem(slot);
            if (invStack.is(VFBRegistry.FRUIT_BAG)) {
                fruitBag = invStack;
                bagSlot = slot;
                break;
            }
        }

        if (fruitBag.isEmpty()) {
            return false;
        }

        Item fruitItem = toAdd.getItem();


        int addCount = toAdd.getCount();

        int currentCount = FruitBagHelper.getFruitCount(fruitBag, fruitItem);

        int newTotal = currentCount + addCount;

        String registryName = fruitItem.getRegistryName().toString();
        FruitBagHelper.setFruitStack(fruitBag, registryName, newTotal);

        toAdd.setCount(0);

        return true;
    }
}
