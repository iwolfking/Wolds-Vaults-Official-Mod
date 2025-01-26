package xyz.iwolfking.woldsvaults.blocks.containers;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.inventory.slot.OutputSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.blocks.containers.lib.infuser.CatalystSlot;
import xyz.iwolfking.woldsvaults.blocks.containers.lib.infuser.InfuserIngredientSlot;
import xyz.iwolfking.woldsvaults.blocks.tiles.VaultInfuserTileEntity;
import xyz.iwolfking.woldsvaults.data.recipes.CachedInfuserRecipeData;
import xyz.iwolfking.woldsvaults.init.ModContainers;

import java.util.function.Function;

public class VaultInfuserContainer extends AbstractContainerMenu {
    private final Function<Player, Boolean> isUsableByPlayer;
    private final ContainerData data;
    private final BlockPos pos;
    private final Level level;

    private VaultInfuserContainer(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(type, id, playerInventory, p -> false, VaultInfuserTileEntity.createInventoryHandler(null), new SimpleContainerData(10), buffer.readBlockPos());
    }


    private VaultInfuserContainer(MenuType<?> type, int id, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer, BaseItemStackHandler inventory, ContainerData data, BlockPos pos) {
        super(type, id);
        this.isUsableByPlayer = isUsableByPlayer;
        this.data = data;
        this.pos = pos;
        this.level = playerInventory.player.level;

        this.addSlot(new OutputSlot(inventory, 0, 135, 48));
        this.addSlot(new InfuserIngredientSlot(inventory, 1, 65, 48));
        this.addSlot(new CatalystSlot(inventory, 2, 38, 48));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 112 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 170));
        }

        this.addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pIndex == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (pIndex != 1 && pIndex != 0) {
                if (this.isInfusionIngredient(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isCatalyst(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 3 && pIndex < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isUsableByPlayer.apply(player);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public boolean isInfusionIngredient(ItemStack pStack) {
        return CachedInfuserRecipeData.getIngredients(this.level).contains(pStack.getItem());
    }

    public boolean isCatalyst(ItemStack pStack) {
        return CachedInfuserRecipeData.getCatalysts(this.level).contains(pStack.getItem());
    }

    public static VaultInfuserContainer create(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new VaultInfuserContainer(ModContainers.VAULT_INFUSER_CONTAINER, windowId, playerInventory, buffer);
    }

    public static VaultInfuserContainer create(int windowId, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer, BaseItemStackHandler inventory, ContainerData data, BlockPos pos) {
        return new VaultInfuserContainer(ModContainers.VAULT_INFUSER_CONTAINER, windowId, playerInventory, isUsableByPlayer, inventory, data, pos);
    }
}
