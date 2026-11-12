package xyz.iwolfking.woldsvaults.blocks.containers;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorAssemblerTileEntity;
import xyz.iwolfking.woldsvaults.init.ModContainers;

import javax.annotation.Nullable;
import java.util.function.Function;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Slot positions are placeholders here: the full-window screen lays every slot out from the window size in
 * its {@code init()}. The server never reads them.
 */
public class ExtractorAssemblerContainer extends AbstractContainerMenu {
    public static final int BUTTON_ASSEMBLE = 0;
    public static final int BUTTON_REPAIR = 1;
    public static final int PLAYER_SLOTS_START = ExtractorAssemblerTileEntity.SLOT_COUNT;
    public static final int PLAYER_SLOT_COUNT = 36;

    private final Function<Player, Boolean> isUsableByPlayer;
    private final BlockPos pos;
    @Nullable
    private final ExtractorAssemblerTileEntity tile;
    private final BaseItemStackHandler inventory;
    private final boolean clientSide;

    private ExtractorAssemblerContainer(MenuType<?> type, int id, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer,
                                        @Nullable ExtractorAssemblerTileEntity tile, BlockPos pos) {
        super(type, id);
        this.isUsableByPlayer = isUsableByPlayer;
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.tile = tile;
        this.clientSide = playerInventory.player.level.isClientSide();
        if (tile == null) {
            WoldLogger.error("Extractor Assembler container opened at {} without a tile entity; the slots will not work.", this.pos);
            this.inventory = ExtractorAssemblerTileEntity.createInventoryHandler(null);
        } else {
            this.inventory = tile.getInventory();
        }
        for (int i = 0; i < ExtractorAssemblerTileEntity.PART_SLOTS; i++) {
            this.addSlot(new PartSlot(i, 8 + i * 18, 22));
        }
        for (int i = 0; i < ExtractorAssemblerTileEntity.ATTACHMENT_SLOTS; i++) {
            this.addSlot(new AttachmentSlot(i, 8 + i * 18, 50));
        }
        this.addSlot(new DockSlot(152, 50));
        this.addSlot(new RepairSlot(152, 70));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Nullable
    private static ExtractorAssemblerTileEntity clientTile(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level.getBlockEntity(pos) instanceof ExtractorAssemblerTileEntity tile) {
            return tile;
        }
        return null;
    }

    public static ExtractorAssemblerContainer create(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        return new ExtractorAssemblerContainer(ModContainers.EXTRACTOR_ASSEMBLER_CONTAINER, windowId, playerInventory, p -> true,
                clientTile(playerInventory, pos), pos);
    }

    public static ExtractorAssemblerContainer create(int windowId, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer,
                                                     ExtractorAssemblerTileEntity tile, BlockPos pos) {
        return new ExtractorAssemblerContainer(ModContainers.EXTRACTOR_ASSEMBLER_CONTAINER, windowId, playerInventory, isUsableByPlayer, tile, pos);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Nullable
    public ExtractorAssemblerTileEntity getTile() {
        return this.tile;
    }

    public boolean isDocked() {
        return this.tile != null && this.tile.isDocked();
    }

    public Slot partSlot(int index) {
        return this.slots.get(index);
    }

    public Slot attachmentSlot(int hardpoint) {
        return this.slots.get(ExtractorAssemblerTileEntity.ATTACHMENT_SLOT_START + hardpoint);
    }

    public Slot dockSlot() {
        return this.slots.get(ExtractorAssemblerTileEntity.OUTPUT_SLOT);
    }

    public Slot repairSlot() {
        return this.slots.get(ExtractorAssemblerTileEntity.REPAIR_SLOT);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_ASSEMBLE && !player.level.isClientSide() && player.level.getBlockEntity(this.pos) instanceof ExtractorAssemblerTileEntity target) {
            target.assemble(player);
            return true;
        }
        if (id == BUTTON_REPAIR && !player.level.isClientSide() && player.level.getBlockEntity(this.pos) instanceof ExtractorAssemblerTileEntity target) {
            target.repair(player);
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
            if (!slot.mayPickup(player)) {
                return ItemStack.EMPTY;
            }
            if (!this.moveItemStackTo(stack, PLAYER_SLOTS_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return slot instanceof AttachmentSlot && this.isDocked() ? ItemStack.EMPTY : copy;
        }
        boolean moved = false;
        for (int target = 0; target < PLAYER_SLOTS_START && !moved; target++) {
            Slot targetSlot = this.slots.get(target);
            if (targetSlot.isActive() && !targetSlot.hasItem() && targetSlot.mayPlace(stack)) {
                moved = this.moveItemStackTo(stack, target, target + 1, false);
            }
        }
        if (!moved) {
            return ItemStack.EMPTY;
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

    /** A core part slot: a normal slot until an extractor is docked, then a read-only mirror of that extractor's part. */
    class PartSlot extends SlotItemHandler {
        PartSlot(int index, int x, int y) {
            super(ExtractorAssemblerContainer.this.inventory, index, x, y);
        }

        @Override
        public ItemStack getItem() {
            if (ExtractorAssemblerContainer.this.isDocked()) {
                return ExtractorAssemblerContainer.this.tile.ghostPart(this.getSlotIndex());
            }
            return super.getItem();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !ExtractorAssemblerContainer.this.isDocked() && ExtractorAssemblerTileEntity.slotAccepts(this.getSlotIndex(), stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !ExtractorAssemblerContainer.this.isDocked();
        }

        @Override
        public void set(ItemStack stack) {
            if (ExtractorAssemblerContainer.this.isDocked()) {
                return;
            }
            super.set(stack);
        }

        @Override
        public ItemStack remove(int amount) {
            if (ExtractorAssemblerContainer.this.isDocked()) {
                return ItemStack.EMPTY;
            }
            return super.remove(amount);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    /**
     * A hardpoint: holds an attachment for the next assembly while nothing is docked, and is a live view over the
     * docked extractor's attachment list otherwise. Writes through the view reach the extractor's NBT on the server
     * only; the client's copy arrives through the ordinary dock-slot sync.
     */
    class AttachmentSlot extends SlotItemHandler {
        private final int hardpoint;

        AttachmentSlot(int hardpoint, int x, int y) {
            super(ExtractorAssemblerContainer.this.inventory, ExtractorAssemblerTileEntity.ATTACHMENT_SLOT_START + hardpoint, x, y);
            this.hardpoint = hardpoint;
        }

        public int getHardpoint() {
            return this.hardpoint;
        }

        @Override
        public ItemStack getItem() {
            if (ExtractorAssemblerContainer.this.isDocked()) {
                return ExtractorAssemblerContainer.this.tile.dockedAttachment(this.hardpoint);
            }
            return super.getItem();
        }

        @Override
        public boolean isActive() {
            if (ExtractorAssemblerContainer.this.isDocked()) {
                return this.hardpoint < ExtractorAssemblerContainer.this.tile.hardpoints();
            }
            return true;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (ExtractorAssemblerContainer.this.isDocked()) {
                return ExtractorAssemblerContainer.this.tile.attachmentRefusal(this.hardpoint, stack) == null;
            }
            return ExtractorAssemblerTileEntity.slotAccepts(this.getSlotIndex(), stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return true;
        }

        @Override
        public void set(ItemStack stack) {
            if (ExtractorAssemblerContainer.this.isDocked()) {
                if (!ExtractorAssemblerContainer.this.clientSide) {
                    ExtractorAssemblerContainer.this.tile.setDockedAttachment(this.hardpoint, stack);
                }
                this.setChanged();
                return;
            }
            super.set(stack);
        }

        @Override
        public ItemStack remove(int amount) {
            if (ExtractorAssemblerContainer.this.isDocked()) {
                if (amount <= 0) {
                    return ItemStack.EMPTY;
                }
                if (ExtractorAssemblerContainer.this.clientSide) {
                    return this.getItem().copy();
                }
                return ExtractorAssemblerContainer.this.tile.removeDockedAttachment(this.hardpoint);
            }
            return super.remove(amount);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    /** The output slot doubles as the dock: a finished extractor lands here and an assembled one may be placed here. */
    /** Pays the docked extractor's repair cost; only usable while an extractor is docked. */
    class RepairSlot extends SlotItemHandler {
        RepairSlot(int x, int y) {
            super(ExtractorAssemblerContainer.this.inventory, ExtractorAssemblerTileEntity.REPAIR_SLOT, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ExtractorAssemblerContainer.this.isDocked();
        }
    }

    class DockSlot extends SlotItemHandler {
        DockSlot(int x, int y) {
            super(ExtractorAssemblerContainer.this.inventory, ExtractorAssemblerTileEntity.OUTPUT_SLOT, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ExtractorAssemblerTileEntity.canDock(ExtractorAssemblerContainer.this.inventory, stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }
}
