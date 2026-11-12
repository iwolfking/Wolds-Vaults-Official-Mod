package xyz.iwolfking.woldsvaults.blocks.containers;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.inventory.slot.OutputSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import xyz.iwolfking.woldsvaults.blocks.containers.lib.WideContainerData;
import xyz.iwolfking.woldsvaults.blocks.tiles.VaultFabricatorTileEntity;
import xyz.iwolfking.woldsvaults.init.ModContainers;
import xyz.iwolfking.woldsvaults.items.FabricationPatternItem;

import java.util.function.Function;

public class VaultFabricatorContainer extends AbstractContainerMenu {
    public static final int BUTTON_PREVIOUS = 0;
    public static final int BUTTON_NEXT = 1;
    public static final int IMAGE_WIDTH = 216;
    public static final int IMAGE_HEIGHT = 212;
    public static final int GRID_X = IMAGE_WIDTH / 2 - 44;
    public static final int GRID_Y = 22;
    public static final int PATTERN_X = 26;
    public static final int OUTPUT_X = 187;
    public static final int OUTPUT_Y = GRID_Y + 36;
    public static final int INVENTORY_X = (IMAGE_WIDTH - 162) / 2 + 1;
    private static final int PLAYER_SLOTS_START = VaultFabricatorTileEntity.SLOT_COUNT;

    private final Function<Player, Boolean> isUsableByPlayer;
    private final BlockPos pos;
    private final ContainerData data;

    private VaultFabricatorContainer(MenuType<?> type, int id, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer,
                                     BaseItemStackHandler inventory, ContainerData data, BlockPos pos) {
        super(type, id);
        this.isUsableByPlayer = isUsableByPlayer;
        this.pos = pos;
        this.data = data;
        this.addSlot(new SlotItemHandler(inventory, VaultFabricatorTileEntity.PATTERN_SLOT, PATTERN_X, GRID_Y));
        this.addSlot(new OutputSlot(inventory, VaultFabricatorTileEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y));
        for (int cell = 0; cell < VaultFabricatorTileEntity.GRID_CELLS; cell++) {
            this.addSlot(new SlotItemHandler(inventory, VaultFabricatorTileEntity.GRID_START + cell, GRID_X + (cell % 5) * 18, GRID_Y + (cell / 5) * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, INVENTORY_X + col * 18, IMAGE_HEIGHT - 82 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, INVENTORY_X + col * 18, IMAGE_HEIGHT - 24));
        }
        this.addDataSlots(data);
    }

    public static VaultFabricatorContainer create(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BaseItemStackHandler inventory = playerInventory.player.level.getBlockEntity(pos) instanceof VaultFabricatorTileEntity tile
                ? tile.getInventory() : VaultFabricatorTileEntity.createInventoryHandler(null, null);
        return new VaultFabricatorContainer(ModContainers.VAULT_FABRICATOR_CONTAINER, windowId, playerInventory, p -> true, inventory,
                WideContainerData.client(VaultFabricatorTileEntity.DATA_COUNT), pos);
    }

    public static VaultFabricatorContainer create(int windowId, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer,
                                                  BaseItemStackHandler inventory, ContainerData data, BlockPos pos) {
        return new VaultFabricatorContainer(ModContainers.VAULT_FABRICATOR_CONTAINER, windowId, playerInventory, isUsableByPlayer, inventory, data, pos);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int data(int index) {
        return WideContainerData.read(this.data, index);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.level.isClientSide() || !(player.level.getBlockEntity(this.pos) instanceof VaultFabricatorTileEntity tile)) {
            return false;
        }
        if (id == BUTTON_PREVIOUS || id == BUTTON_NEXT) {
            tile.cyclePattern(id == BUTTON_NEXT ? 1 : -1);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < PLAYER_SLOTS_START) {
            if (!this.moveItemStackTo(stack, PLAYER_SLOTS_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof FabricationPatternItem) {
            if (!this.moveItemStackTo(stack, VaultFabricatorTileEntity.PATTERN_SLOT, VaultFabricatorTileEntity.PATTERN_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            for (int target = VaultFabricatorTileEntity.GRID_START; target < PLAYER_SLOTS_START && !stack.isEmpty(); target++) {
                Slot cell = this.slots.get(target);
                if (cell.mayPlace(stack) && (!cell.hasItem() || ItemStack.isSameItemSameTags(cell.getItem(), stack))) {
                    moved |= this.moveItemStackTo(stack, target, target + 1, false);
                }
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isUsableByPlayer.apply(player);
    }
}
