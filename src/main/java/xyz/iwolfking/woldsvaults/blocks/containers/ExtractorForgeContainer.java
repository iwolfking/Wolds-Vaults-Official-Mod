package xyz.iwolfking.woldsvaults.blocks.containers;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import xyz.iwolfking.woldsvaults.blocks.containers.lib.WideContainerData;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorForgeTileEntity;
import xyz.iwolfking.woldsvaults.extraction.costs.CostLedger;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;
import xyz.iwolfking.woldsvaults.init.ModContainers;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Extraction Forge menu. Slot 0 is the blueprint slot (and where the finished part appears), slot 1 is the staged
 * blueprint (synced for the screen, never shown or clickable) and slots 2–8 are the deposit column: deposit slot
 * {@code i} takes only the staged ledger's {@code i}-th item line, only as many as are still owed, and pays them
 * in on the spot, so it never holds anything. Layout constants are shared with {@link ExtractorForgeScreen}.
 */
public class ExtractorForgeContainer extends AbstractContainerMenu {
    public static final int BUTTON_FABRICATE = 0;
    public static final int BUTTON_STAGE = 1;
    public static final int IMAGE_WIDTH = 340;
    public static final int IMAGE_HEIGHT = 256;
    public static final int MARGIN = 8;
    public static final int RAIL_W = 32;
    public static final int RAIL_Y0 = 8;
    public static final int CENTER_X = IMAGE_WIDTH / 2;
    public static final int BLUEPRINT_X = CENTER_X - 8;
    public static final int BLUEPRINT_Y = 13;
    public static final int DEPOSIT_X = MARGIN + RAIL_W / 2 - 8;
    public static final int DEPOSIT_Y0 = RAIL_Y0 + 3;
    public static final int DEPOSIT_PITCH = 23;
    public static final int INVENTORY_X = CENTER_X - 80;
    public static final int INVENTORY_Y = IMAGE_HEIGHT - 84;
    public static final int HOTBAR_Y = IMAGE_HEIGHT - 26;
    private static final int PLAYER_SLOTS_START = ExtractorForgeTileEntity.SLOT_COUNT;

    private final Function<Player, Boolean> isUsableByPlayer;
    private final BlockPos pos;
    private final BaseItemStackHandler inventory;
    private final ContainerData data;

    private ExtractorForgeContainer(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(type, id, playerInventory, buffer.readBlockPos());
    }

    private ExtractorForgeContainer(MenuType<?> type, int id, Inventory playerInventory, BlockPos pos) {
        this(type, id, playerInventory, p -> true, clientInventory(playerInventory, pos),
                WideContainerData.client(ExtractorForgeTileEntity.DATA_COUNT), null, pos);
    }

    private ExtractorForgeContainer(MenuType<?> type, int id, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer,
                                    BaseItemStackHandler inventory, ContainerData data, @Nullable ExtractorForgeTileEntity tile, BlockPos pos) {
        super(type, id);
        this.isUsableByPlayer = isUsableByPlayer;
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.inventory = inventory;
        this.data = data;
        this.addSlot(new BlueprintSlot(inventory, this));
        this.addSlot(new StagedSlot(inventory));
        Consumer<ItemStack> payer = tile == null ? stack -> { } : tile::payDeposit;
        SimpleContainer nothing = new SimpleContainer(ExtractorForgeTileEntity.DEPOSIT_SLOTS);
        for (int i = 0; i < ExtractorForgeTileEntity.DEPOSIT_SLOTS; i++) {
            this.addSlot(new DepositSlot(nothing, i, DEPOSIT_X, DEPOSIT_Y0 + i * DEPOSIT_PITCH, this, payer));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, INVENTORY_X + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, INVENTORY_X + col * 18, HOTBAR_Y));
        }
        this.addDataSlots(data);
    }

    private static BaseItemStackHandler clientInventory(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level.getBlockEntity(pos) instanceof ExtractorForgeTileEntity tile) {
            return tile.getInventory();
        }
        return ExtractorForgeTileEntity.createInventoryHandler(null);
    }

    public static ExtractorForgeContainer create(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new ExtractorForgeContainer(ModContainers.EXTRACTOR_FORGE_CONTAINER, windowId, playerInventory, buffer);
    }

    public static ExtractorForgeContainer create(int windowId, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer,
                                                 BaseItemStackHandler inventory, ContainerData data, ExtractorForgeTileEntity tile, BlockPos pos) {
        return new ExtractorForgeContainer(ModContainers.EXTRACTOR_FORGE_CONTAINER, windowId, playerInventory, isUsableByPlayer, inventory, data, tile, pos);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int data(int index) {
        return WideContainerData.read(this.data, index);
    }

    public boolean isFabricating() {
        return this.data(ExtractorForgeTileEntity.DATA_FABRICATING) != 0;
    }

    public ItemStack staged() {
        return this.inventory.getStackInSlot(ExtractorForgeTileEntity.STAGED_SLOT);
    }

    public boolean isStaged() {
        return !this.staged().isEmpty();
    }

    @Nullable
    public CompoundTag stagedLedger() {
        return ExtractorForgeTileEntity.stagedLedger(this.staged());
    }

    /** The ledger's item lines in order: deposit slot {@code i} pays line {@code i}. */
    public static List<ExtractorCosts.CostLine> itemLines(@Nullable CompoundTag ledger) {
        List<ExtractorCosts.CostLine> out = new ArrayList<>();
        for (ExtractorCosts.CostLine line : CostLedger.lines(ledger)) {
            if (!line.fluid()) {
                out.add(line);
            }
        }
        return out;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.level.isClientSide() || !(player.level.getBlockEntity(this.pos) instanceof ExtractorForgeTileEntity tile)) {
            return false;
        }
        if (id == BUTTON_FABRICATE) {
            tile.fabricate(player);
            return true;
        }
        if (id == BUTTON_STAGE) {
            tile.stage(player);
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
            if (index != ExtractorForgeTileEntity.BLUEPRINT_SLOT || !this.moveItemStackTo(stack, PLAYER_SLOTS_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved;
            if (stack.getItem() instanceof ExtractorBlueprintItem) {
                moved = this.moveItemStackTo(stack, ExtractorForgeTileEntity.BLUEPRINT_SLOT, ExtractorForgeTileEntity.BLUEPRINT_SLOT + 1, false);
            } else {
                moved = this.moveItemStackTo(stack, ExtractorForgeTileEntity.DEPOSIT_SLOT_START, PLAYER_SLOTS_START, false);
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

    /** Takes a blueprint while nothing is staged; afterwards stays shut until the finished part appears in it. */
    static class BlueprintSlot extends SlotItemHandler {
        private final ExtractorForgeContainer menu;

        BlueprintSlot(IItemHandler handler, ExtractorForgeContainer menu) {
            super(handler, ExtractorForgeTileEntity.BLUEPRINT_SLOT, BLUEPRINT_X, BLUEPRINT_Y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !this.menu.isStaged() && !this.menu.isFabricating() && ExtractorForgeTileEntity.slotAccepts(this.getSlotIndex(), stack);
        }
    }

    /** The staged blueprint: synced so the screen can read its ledger, but inactive, so it is never drawn or clicked. */
    static class StagedSlot extends SlotItemHandler {
        StagedSlot(IItemHandler handler) {
            super(handler, ExtractorForgeTileEntity.STAGED_SLOT, BLUEPRINT_X, BLUEPRINT_Y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }

    /** One deposit slot: accepts its ledger line's item up to what is owed and pays it in immediately. */
    static class DepositSlot extends Slot {
        private final int line;
        private final ExtractorForgeContainer menu;
        private final Consumer<ItemStack> payer;

        DepositSlot(SimpleContainer nothing, int line, int x, int y, ExtractorForgeContainer menu, Consumer<ItemStack> payer) {
            super(nothing, line, x, y);
            this.line = line;
            this.menu = menu;
            this.payer = payer;
        }

        private long owed(ItemStack stack) {
            if (this.menu.isFabricating()) {
                return 0L;
            }
            CompoundTag ledger = this.menu.stagedLedger();
            List<ExtractorCosts.CostLine> lines = itemLines(ledger);
            if (ledger == null || this.line >= lines.size()) {
                return 0L;
            }
            ExtractorCosts.CostLine target = lines.get(this.line);
            if (!stack.isEmpty() && target.item() != stack.getItem()) {
                return 0L;
            }
            return CostLedger.remaining(ledger, target);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !stack.isEmpty() && this.owed(stack) > 0L;
        }

        @Override
        public int getMaxStackSize() {
            return (int) Math.max(1L, Math.min(64L, this.owed(ItemStack.EMPTY)));
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return (int) Math.max(1L, Math.min(stack.getMaxStackSize(), this.owed(stack)));
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean hasItem() {
            return false;
        }

        @Override
        public void set(ItemStack stack) {
            this.payer.accept(stack);
            this.setChanged();
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            return ItemStack.EMPTY;
        }
    }
}
