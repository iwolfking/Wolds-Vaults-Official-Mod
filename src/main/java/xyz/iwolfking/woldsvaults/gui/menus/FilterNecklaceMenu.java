package xyz.iwolfking.woldsvaults.gui.menus;

import net.blay09.mods.cookingforblockheads.menu.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.init.ModContainers;
import xyz.iwolfking.woldsvaults.items.FilterNecklaceItem;

public class FilterNecklaceMenu extends AbstractContainerMenu {
    private final ItemStack stack;
    private final IItemHandler itemHandler;

    public FilterNecklaceMenu(int id, Inventory playerInv, FriendlyByteBuf buffer) {
        this(id, playerInv, playerInv.player.getMainHandItem());
    }

    public FilterNecklaceMenu(int id, Inventory playerInv, ItemStack stack) {
        super(ModContainers.FILTER_NECKLACE_CONTAINER, id);
        this.stack = stack;
        this.itemHandler = ((FilterNecklaceItem)stack.getItem()).getInventory(stack);

        for(int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(itemHandler, i, 8 + i * 18, 18));
        }

        int startX = 8;
        int startY = 50;

        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        for(int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, startX + col * 18, startY + 58));
        }
    }



    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }


    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            int containerSlotCount = 9;

            if(index < containerSlotCount) {
                if(!this.moveItemStackTo(stackInSlot, containerSlotCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                if(!this.moveItemStackTo(stackInSlot, 0, containerSlotCount, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if(stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }

            slot.onTake(player, stackInSlot);
        }

        return itemstack;
    }
}
