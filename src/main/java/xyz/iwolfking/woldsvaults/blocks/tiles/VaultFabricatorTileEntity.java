package xyz.iwolfking.woldsvaults.blocks.tiles;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultFabricatorContainer;
import xyz.iwolfking.woldsvaults.blocks.containers.lib.WideContainerData;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.FabricationPatternItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Vault Fabricator: slot 0 holds a Fabrication Pattern, slot 1 is the output and slots 2–26 are the 5×5
 * grid, each cell accepting only the item the pattern puts there. With every cell filled, enough of the
 * pattern's fluid in the tank and energy in the buffer, it crafts one component over the recipe's ticks,
 * drawing its FE evenly. Pipes may insert into any slot: items are routed to the emptiest matching cell.
 */
public class VaultFabricatorTileEntity extends BaseInventoryTileEntity implements MenuProvider {
    public static final int PATTERN_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int GRID_START = 2;
    public static final int GRID_CELLS = 25;
    public static final int SLOT_COUNT = GRID_START + GRID_CELLS;
    public static final int TANK_CAPACITY = 8000;
    public static final int ENERGY_CAPACITY = 2_000_000;
    public static final int ENERGY_INPUT = 100_000;
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TICKS = 1;
    public static final int DATA_ENERGY = 2;
    public static final int DATA_FLUID = 3;
    public static final int DATA_COUNT = 4;

    private final BaseItemStackHandler inventory;
    private final FluidTank tank;
    private final StoredEnergy energy = new StoredEnergy();
    private int progress;
    private final LazyOptional<IItemHandler> itemCapability;
    private final LazyOptional<IFluidHandler> fluidCapability;
    private final LazyOptional<IEnergyStorage> energyCapability;
    private final ContainerData data = new WideContainerData(
            () -> this.progress,
            () -> {
                ExtractorCostsConfig.ComponentEntry recipe = this.recipe();
                return recipe == null ? 0 : recipe.ticks;
            },
            () -> this.energy.getEnergyStored(),
            this::fluidAmount);

    public VaultFabricatorTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.VAULT_FABRICATOR_TILE_ENTITY_TYPE, pos, state);
        this.inventory = createInventoryHandler(this::markDirtyAndDispatch, this);
        this.tank = new FluidTank(TANK_CAPACITY, this::acceptsFluid) {
            @Override
            protected void onContentsChanged() {
                VaultFabricatorTileEntity.this.setChanged();
            }
        };
        this.itemCapability = LazyOptional.of(RoutingHandler::new);
        this.fluidCapability = LazyOptional.of(() -> this.tank);
        this.energyCapability = LazyOptional.of(() -> this.energy);
    }

    public static BaseItemStackHandler createInventoryHandler(@Nullable Runnable onContentsChanged, @Nullable VaultFabricatorTileEntity tile) {
        BaseItemStackHandler inventory = new BaseItemStackHandler(SLOT_COUNT, onContentsChanged);
        inventory.setOutputSlots(OUTPUT_SLOT);
        inventory.addSlotLimit(PATTERN_SLOT, 1);
        inventory.setSlotValidator((slot, stack) -> {
            if (slot == PATTERN_SLOT) {
                return stack.getItem() instanceof FabricationPatternItem;
            }
            if (slot == OUTPUT_SLOT) {
                return false;
            }
            return tile == null || tile.cellAccepts(slot - GRID_START, stack);
        });
        return inventory;
    }

    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    public FluidTank getTank() {
        return this.tank;
    }

    private int fluidAmount() {
        return this.tank.getFluidAmount();
    }

    public ContainerData getData() {
        return this.data;
    }

    @Nullable
    public ExtractorCostsConfig.ComponentEntry recipe() {
        return FabricationPatternItem.recipe(this.inventory.getStackInSlot(PATTERN_SLOT));
    }

    @Nullable
    public static Item cellItem(@Nullable ExtractorCostsConfig.ComponentEntry recipe, int cell) {
        if (recipe == null) {
            return null;
        }
        String id = recipe.cell(cell);
        return id == null ? null : ExtractorCosts.resolveItem(id);
    }

    public boolean cellAccepts(int cell, ItemStack stack) {
        Item item = cellItem(this.recipe(), cell);
        return item != null && stack.getItem() == item;
    }

    private boolean acceptsFluid(FluidStack stack) {
        ExtractorCostsConfig.ComponentEntry recipe = this.recipe();
        if (recipe == null || recipe.fluid.isEmpty()) {
            return false;
        }
        Fluid fluid = ExtractorCosts.resolveFluid(recipe.fluid);
        return fluid != null && stack.getFluid() == fluid;
    }

    /**
     * Imprints the pattern in the pattern slot with the next (or previous) component in config order; a blank
     * card becomes encoded. Grid contents that no longer match stay where they are for the player to take out.
     */
    public void cyclePattern(int direction) {
        ItemStack pattern = this.inventory.getStackInSlot(PATTERN_SLOT);
        if (!(pattern.getItem() instanceof FabricationPatternItem)) {
            return;
        }
        List<String> keys = new ArrayList<>(ModConfigs.EXTRACTOR_COSTS.COMPONENTS.keySet());
        if (keys.isEmpty()) {
            WoldsVaults.LOGGER.error("extractor_costs.json has no COMPONENTS; the Vault Fabricator at {} has nothing to imprint.", this.getBlockPos());
            return;
        }
        String current = FabricationPatternItem.componentKey(pattern);
        int index = current == null ? (direction > 0 ? -1 : 0) : keys.indexOf(current);
        int next = Math.floorMod(index + direction, keys.size());
        this.inventory.setStackInSlot(PATTERN_SLOT, FabricationPatternItem.encode(keys.get(next)));
        this.progress = 0;
        this.markDirtyAndDispatch();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.tank.readFromNBT(tag.getCompound("Tank"));
        this.energy.set(tag.getInt("Energy"));
        this.progress = tag.getInt("Progress");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", this.tank.writeToNBT(new CompoundTag()));
        tag.putInt("Energy", this.energy.getEnergyStored());
        tag.putInt("Progress", this.progress);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VaultFabricatorTileEntity tile) {
        tile.tickCraft();
    }

    private boolean ready(ExtractorCostsConfig.ComponentEntry recipe, Item product) {
        for (int cell = 0; cell < GRID_CELLS; cell++) {
            Item item = cellItem(recipe, cell);
            if (item != null && this.inventory.getStackInSlot(GRID_START + cell).getItem() != item) {
                return false;
            }
        }
        if (!recipe.fluid.isEmpty() && recipe.fluidAmount > 0 && this.tank.getFluidAmount() < recipe.fluidAmount) {
            return false;
        }
        ItemStack output = this.inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || (output.getItem() == product && output.getCount() < output.getMaxStackSize());
    }

    private void tickCraft() {
        ExtractorCostsConfig.ComponentEntry recipe = this.recipe();
        Item product = recipe == null ? null : ExtractorCosts.resolveItem(recipe.item);
        if (recipe == null || product == null || !this.ready(recipe, product)) {
            if (this.progress != 0) {
                this.progress = 0;
                this.setChanged();
            }
            return;
        }
        int ticks = Math.max(1, recipe.ticks);
        int perTick = (int) Math.ceil((double) recipe.fe / ticks);
        if (this.energy.getEnergyStored() < perTick) {
            return;
        }
        this.energy.set(this.energy.getEnergyStored() - perTick);
        this.progress++;
        if (this.progress >= ticks) {
            for (int cell = 0; cell < GRID_CELLS; cell++) {
                if (cellItem(recipe, cell) != null) {
                    this.inventory.getStackInSlot(GRID_START + cell).shrink(1);
                }
            }
            if (!recipe.fluid.isEmpty() && recipe.fluidAmount > 0) {
                this.tank.drain(recipe.fluidAmount, IFluidHandler.FluidAction.EXECUTE);
            }
            ItemStack output = this.inventory.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                this.inventory.setStackInSlot(OUTPUT_SLOT, new ItemStack(product));
            } else {
                output.grow(1);
            }
            this.progress = 0;
            this.markDirtyAndDispatch();
        } else {
            this.setChanged();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (!this.isRemoved()) {
            if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return this.itemCapability.cast();
            }
            if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return this.fluidCapability.cast();
            }
            if (cap == CapabilityEnergy.ENERGY) {
                return this.energyCapability.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.itemCapability.invalidate();
        this.fluidCapability.invalidate();
        this.energyCapability.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.woldsvaults.vault_fabricator");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return VaultFabricatorContainer.create(windowId, playerInventory, this::isUsableByPlayer, this.inventory, this.data, this.getBlockPos());
    }

    /** An FE buffer that accepts power from any side and is drained only by crafting. */
    private class StoredEnergy extends EnergyStorage {
        StoredEnergy() {
            super(ENERGY_CAPACITY, ENERGY_INPUT, 0);
        }

        void set(int value) {
            this.energy = Math.max(0, Math.min(this.capacity, value));
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                VaultFabricatorTileEntity.this.setChanged();
            }
            return received;
        }
    }

    /** Pipes see 27 slots; any insert is routed to the emptiest grid cell that wants the item, and only the output extracts. */
    private class RoutingHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return VaultFabricatorTileEntity.this.inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            BaseItemStackHandler inv = VaultFabricatorTileEntity.this.inventory;
            if (stack.isEmpty()) {
                return stack;
            }
            int limit = Math.min(64, stack.getMaxStackSize());
            int[] counts = new int[GRID_CELLS];
            boolean[] accepts = new boolean[GRID_CELLS];
            for (int cell = 0; cell < GRID_CELLS; cell++) {
                counts[cell] = inv.getStackInSlot(GRID_START + cell).getCount();
                accepts[cell] = VaultFabricatorTileEntity.this.cellAccepts(cell, stack);
            }
            int remaining = stack.getCount();
            while (remaining > 0) {
                int best = -1;
                for (int cell = 0; cell < GRID_CELLS; cell++) {
                    if (accepts[cell] && counts[cell] < limit && (best < 0 || counts[cell] < counts[best])) {
                        best = cell;
                    }
                }
                if (best < 0) {
                    break;
                }
                if (!simulate) {
                    ItemStack one = stack.copy();
                    one.setCount(1);
                    if (!inv.insertItem(GRID_START + best, one, false).isEmpty()) {
                        break;
                    }
                }
                counts[best]++;
                remaining--;
            }
            if (remaining == stack.getCount()) {
                return stack;
            }
            ItemStack rest = stack.copy();
            rest.setCount(remaining);
            return rest;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == OUTPUT_SLOT ? VaultFabricatorTileEntity.this.inventory.extractItem(OUTPUT_SLOT, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            for (int cell = 0; cell < GRID_CELLS; cell++) {
                if (VaultFabricatorTileEntity.this.cellAccepts(cell, stack)) {
                    return true;
                }
            }
            return false;
        }
    }
}
