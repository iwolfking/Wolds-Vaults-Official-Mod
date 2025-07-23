package xyz.iwolfking.woldsvaults.api.helper;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoldInventoryUtil {
    public static List<InventoryUtil.ItemAccess> getCoinPouchItemAccess(InventoryUtil.ItemAccess access) {
        List<InventoryUtil.ItemAccess> accesses = new ArrayList<>();
        if (access.getItem() instanceof WalletItem) {
            ItemStack container = access.getStack();
            NonNullList<ItemStack> contents = WalletItem.getWalletInventory(container);

            for (int slot = 0; slot < contents.size(); slot++) {
                ItemStack stack = contents.get(slot);
                if (!stack.isEmpty()) {
                    int finalSlot = slot;
                    accesses.add(access.chain(stack, (containerStack, newStack) -> {
                        NonNullList<ItemStack> ctContents = WalletItem.getWalletInventory(containerStack);
                        ctContents.set(finalSlot, newStack);
                        WalletItem.putWalletInventory(containerStack, ctContents);
                    }));
                }
            }
        }

        return accesses;
    }
}
