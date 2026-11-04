package xyz.iwolfking.woldsvaults.blocks.tiles;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.containers.ExtractorForgeContainer;
import xyz.iwolfking.woldsvaults.blocks.containers.lib.WideContainerData;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig;
import xyz.iwolfking.woldsvaults.extraction.costs.CostLedger;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

import javax.annotation.Nullable;

/**
 * The Extraction Forge. Slot 0 takes an identified blueprint; Stage moves it into the forge (slot 1, never
 * reachable from the menu) and locks it in. Only a staged blueprint's cost ledger accepts payment: the seven
 * deposit slots of the menu, item pipes, fluid pipes and filled buckets used on the forge all pay into it, and
 * nothing is refunded or taken back out. Once every line is paid, Fabricate runs the job: it draws
 * {@code FORGE.fePerTickPerQuality} × quality FE per tick for {@code FORGE.secondsPerQuality} × quality seconds
 * from the energy buffer, stalling while the buffer is short, and then puts the finished part in slot 0. Slots 2–8
 * held items in the old deposit flow; anything still in them is dropped at the forge on the first tick.
 */
public class ExtractorForgeTileEntity extends BaseInventoryTileEntity implements MenuProvider, ExtractorStationTile {
    public static final int BLUEPRINT_SLOT = 0;
    public static final int STAGED_SLOT = 1;
    public static final int DEPOSIT_SLOT_START = 2;
    public static final int DEPOSIT_SLOTS = 7;
    public static final int SLOT_COUNT = DEPOSIT_SLOT_START + DEPOSIT_SLOTS;
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TOTAL_TICKS = 1;
    public static final int DATA_ENERGY = 2;
    public static final int DATA_CAPACITY = 3;
    public static final int DATA_FE_PER_TICK = 4;
    public static final int DATA_FABRICATING = 5;
    public static final int DATA_COUNT = 6;

    private final BaseItemStackHandler inventory;
    private final ForgeEnergy energy = new ForgeEnergy();
    private boolean fabricating;
    private int progress;
    private int totalTicks;
    private int fePerTick;
    private boolean legacyChecked;
    private final LazyOptional<IItemHandler> itemCapability;
    private final LazyOptional<IFluidHandler> fluidCapability;
    private final LazyOptional<IEnergyStorage> energyCapability;
    private final ContainerData data = new WideContainerData(
            () -> this.progress,
            () -> this.fabricating ? this.totalTicks : this.previewTicks(),
            () -> this.energy.stored,
            () -> this.energy.getMaxEnergyStored(),
            () -> this.fabricating ? this.fePerTick : this.previewFePerTick(),
            () -> this.fabricating ? 1 : 0);

    public ExtractorForgeTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EXTRACTOR_FORGE_TILE_ENTITY_TYPE, pos, state);
        this.inventory = createInventoryHandler(this::markDirtyAndDispatch);
        IItemHandler ledgerItems = CostLedger.itemHandler(this::ledger, this::markDirtyAndDispatch);
        this.itemCapability = LazyOptional.of(() -> new AutomationHandler(ledgerItems));
        this.fluidCapability = LazyOptional.of(() -> CostLedger.fluidHandler(this::ledger, this::markDirtyAndDispatch));
        this.energyCapability = LazyOptional.of(() -> this.energy);
    }

    public static BaseItemStackHandler createInventoryHandler(@Nullable Runnable onContentsChanged) {
        BaseItemStackHandler inventory = new BaseItemStackHandler(SLOT_COUNT, onContentsChanged);
        inventory.addSlotLimit(BLUEPRINT_SLOT, 1);
        inventory.addSlotLimit(STAGED_SLOT, 1);
        inventory.setSlotValidator(ExtractorForgeTileEntity::slotAccepts);
        return inventory;
    }

    public static boolean slotAccepts(int slot, ItemStack stack) {
        return (slot == BLUEPRINT_SLOT || slot == STAGED_SLOT) && stack.getItem() instanceof ExtractorBlueprintItem;
    }

    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    public ContainerData getData() {
        return this.data;
    }

    private static ExtractorCostsConfig.ForgeEntry forgeConfig() {
        return ModConfigs.EXTRACTOR_COSTS.FORGE;
    }

    /** The staged blueprint's cost ledger, or null when nothing is staged: only a staged blueprint takes payment. */
    @Nullable
    public CompoundTag ledger() {
        return stagedLedger(this.inventory.getStackInSlot(STAGED_SLOT));
    }

    /** The cost ledger of a staged blueprint stack, or null if the stack is not an identified blueprint. */
    @Nullable
    public static CompoundTag stagedLedger(ItemStack staged) {
        if (!(staged.getItem() instanceof ExtractorBlueprintItem) || !ExtractorBlueprintItem.isIdentified(staged)) {
            return null;
        }
        return ExtractorBlueprintItem.ledger(staged);
    }

    public boolean isStaged() {
        return !this.inventory.getStackInSlot(STAGED_SLOT).isEmpty();
    }

    public boolean isFabricating() {
        return this.fabricating;
    }

    private ItemStack previewBlueprint() {
        ItemStack staged = this.inventory.getStackInSlot(STAGED_SLOT);
        return staged.isEmpty() ? this.inventory.getStackInSlot(BLUEPRINT_SLOT) : staged;
    }

    private double previewQuality() {
        ItemStack blueprint = this.previewBlueprint();
        if (!(blueprint.getItem() instanceof ExtractorBlueprintItem) || !ExtractorBlueprintItem.isIdentified(blueprint)) {
            return -1.0D;
        }
        PartData part = ExtractorBlueprintItem.getPart(blueprint);
        return part == null ? ExtractorBlueprintItem.getQuality(blueprint) : part.quality;
    }

    private int previewTicks() {
        double quality = this.previewQuality();
        return quality < 0.0D ? 0 : forgeConfig().ticks(quality);
    }

    private int previewFePerTick() {
        double quality = this.previewQuality();
        return quality < 0.0D ? 0 : forgeConfig().fePerTick(quality);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.fabricating = tag.getBoolean("Fabricating");
        this.progress = tag.getInt("Progress");
        this.totalTicks = tag.getInt("TotalTicks");
        this.fePerTick = tag.getInt("FePerTick");
        this.energy.stored = Math.max(0, tag.getInt("Energy"));
        FluidStack legacy = FluidStack.loadFluidStackFromNBT(tag.getCompound("Tank"));
        if (!legacy.isEmpty()) {
            WoldsVaults.LOGGER.error("Extraction Forge at {} held {} mB of {} in its old tank; the forge no longer has a tank and the fluid was discarded.",
                    this.getBlockPos(), legacy.getAmount(), legacy.getDisplayName().getString());
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Fabricating", this.fabricating);
        tag.putInt("Progress", this.progress);
        tag.putInt("TotalTicks", this.totalTicks);
        tag.putInt("FePerTick", this.fePerTick);
        tag.putInt("Energy", this.energy.stored);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ExtractorForgeTileEntity tile) {
        if (!tile.legacyChecked) {
            tile.legacyChecked = true;
            tile.clearLegacyContents();
        }
        if (tile.fabricating) {
            tile.tickFabrication();
        }
    }

    private void tickFabrication() {
        if (!this.isStaged()) {
            WoldsVaults.LOGGER.error("Extraction Forge at {} was fabricating with no staged blueprint; the job was cancelled.", this.getBlockPos());
            this.stopJob();
            return;
        }
        if (this.energy.stored < this.fePerTick) {
            return;
        }
        this.energy.stored -= this.fePerTick;
        this.progress++;
        this.setChanged();
        if (this.progress >= this.totalTicks) {
            this.finishJob();
        }
    }

    private void finishJob() {
        ItemStack blueprint = this.inventory.getStackInSlot(STAGED_SLOT);
        ExtractorBlueprintItem item = (ExtractorBlueprintItem) blueprint.getItem();
        PartData part = ExtractorBlueprintItem.getPart(blueprint);
        ExtractorPartItem partItem = ModItems.EXTRACTOR_PARTS.get(item.getPartType());
        if (part == null || partItem == null) {
            WoldsVaults.LOGGER.error("Extraction Forge at {} finished a job whose blueprint has no part ({}); the blueprint was returned to the slot.",
                    this.getBlockPos(), blueprint.getItem().getRegistryName());
            this.inventory.setStackInSlot(BLUEPRINT_SLOT, blueprint);
        } else {
            this.inventory.setStackInSlot(BLUEPRINT_SLOT, ExtractorPartItem.create(partItem, part));
            WoldsVaults.LOGGER.info("Extraction Forge at {} fabricated {} ({}) in {} ticks at {} FE/t.",
                    this.getBlockPos(), part.name, part.describeOrigin(), this.totalTicks, this.fePerTick);
        }
        this.inventory.setStackInSlot(STAGED_SLOT, ItemStack.EMPTY);
        this.stopJob();
    }

    private void stopJob() {
        this.fabricating = false;
        this.progress = 0;
        this.totalTicks = 0;
        this.fePerTick = 0;
        this.markDirtyAndDispatch();
    }

    /** Slots 2–8 and a part in slot 1 come from the old deposit/output flow; return them to the world. */
    private void clearLegacyContents() {
        if (this.level == null) {
            return;
        }
        ItemStack staged = this.inventory.getStackInSlot(STAGED_SLOT);
        if (!staged.isEmpty() && !(staged.getItem() instanceof ExtractorBlueprintItem)) {
            if (this.inventory.getStackInSlot(BLUEPRINT_SLOT).isEmpty()) {
                WoldsVaults.LOGGER.error("Extraction Forge at {} had {} in its old output slot; moved it to the blueprint slot.",
                        this.getBlockPos(), staged.getHoverName().getString());
                this.inventory.setStackInSlot(BLUEPRINT_SLOT, staged);
            } else {
                WoldsVaults.LOGGER.error("Extraction Forge at {} had {} in its old output slot and the blueprint slot is full; dropped it at the forge.",
                        this.getBlockPos(), staged.getHoverName().getString());
                this.spill(staged);
            }
            this.inventory.setStackInSlot(STAGED_SLOT, ItemStack.EMPTY);
        }
        for (int slot = DEPOSIT_SLOT_START; slot < SLOT_COUNT; slot++) {
            ItemStack stack = this.inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                WoldsVaults.LOGGER.error("Extraction Forge at {} still held {} x{} in old deposit slot {}; deposits no longer store items, dropped it at the forge.",
                        this.getBlockPos(), stack.getHoverName().getString(), stack.getCount(), slot);
                this.spill(stack);
                this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    /** Drops a stack on top of the forge controller. */
    public void spill(ItemStack stack) {
        if (this.level != null && !stack.isEmpty()) {
            BlockPos pos = this.getBlockPos();
            Containers.dropItemStack(this.level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, stack);
        }
    }

    /**
     * Pays a stack handed to a deposit slot of the menu into the staged ledger. The slot only hands over what the
     * ledger still owes, so anything left over means the ledger changed in between; it is dropped at the forge.
     */
    public void payDeposit(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag ledger = this.ledger();
        int taken = CostLedger.payItems(ledger, stack, false);
        if (taken < stack.getCount()) {
            ItemStack left = stack.copy();
            left.setCount(stack.getCount() - taken);
            WoldsVaults.LOGGER.error("Extraction Forge at {} was handed {} x{} but the ledger took only {}; dropped the rest at the forge.",
                    this.getBlockPos(), stack.getHoverName().getString(), stack.getCount(), taken);
            this.spill(left);
        }
        if (taken > 0) {
            this.markDirtyAndDispatch();
        }
    }

    @Override
    public boolean isWorking() {
        return this.fabricating;
    }

    @Override
    public ItemStack getDisplayExtractor() {
        return ItemStack.EMPTY;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!this.isRemoved() && cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return this.fluidCapability.cast();
        }
        if (!this.isRemoved() && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.itemCapability.cast();
        }
        if (!this.isRemoved() && cap == CapabilityEnergy.ENERGY) {
            return this.energyCapability.cast();
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
        return new TranslatableComponent("container.woldsvaults.extractor_forge");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return ExtractorForgeContainer.create(windowId, playerInventory, this::isUsableByPlayer, this.inventory, this.data, this, this.getBlockPos());
    }

    /** Null when Stage may fire, else the reason it may not. */
    @Nullable
    public String stageRefusal() {
        if (this.fabricating) {
            return "The forge is busy fabricating.";
        }
        if (this.isStaged()) {
            return "A blueprint is already staged.";
        }
        ItemStack blueprint = this.inventory.getStackInSlot(BLUEPRINT_SLOT);
        if (!(blueprint.getItem() instanceof ExtractorBlueprintItem)) {
            return "Place an identified blueprint in the blueprint slot.";
        }
        if (!ExtractorBlueprintItem.isIdentified(blueprint) || ExtractorBlueprintItem.getPart(blueprint) == null) {
            return "Identify the blueprint first.";
        }
        if (!CostLedger.hasCost(ExtractorBlueprintItem.ledger(blueprint))) {
            return "This blueprint has no fabrication cost.";
        }
        return null;
    }

    /** Moves the blueprint from slot 0 into the forge, after which it can only leave as the finished part. */
    public void stage(Player player) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        String refusal = this.stageRefusal();
        if (refusal != null) {
            tell(player, refusal);
            return;
        }
        ItemStack blueprint = this.inventory.getStackInSlot(BLUEPRINT_SLOT);
        this.inventory.setStackInSlot(STAGED_SLOT, blueprint);
        this.inventory.setStackInSlot(BLUEPRINT_SLOT, ItemStack.EMPTY);
        this.markDirtyAndDispatch();
        PartData part = ExtractorBlueprintItem.getPart(blueprint);
        WoldsVaults.LOGGER.info("{} staged {} at the Extraction Forge at {}.", player.getName().getString(), part.name, this.getBlockPos());
    }

    /** Null when Fabricate may fire, else the reason it may not. */
    @Nullable
    public String fabricateRefusal() {
        if (this.fabricating) {
            return "The forge is already fabricating.";
        }
        if (!this.isStaged()) {
            return "Stage a blueprint first.";
        }
        if (!CostLedger.isComplete(this.ledger())) {
            return "The fabrication cost is not fully paid.";
        }
        if (!this.inventory.getStackInSlot(BLUEPRINT_SLOT).isEmpty()) {
            return "Take the item out of the blueprint slot first.";
        }
        if (forgeConfig().energyCapacity <= 0) {
            WoldsVaults.LOGGER.error("Extraction Forge at {} cannot fabricate: the FORGE block of extractor_costs did not load.", this.getBlockPos());
            return "The forge's power table did not load; see the log.";
        }
        return null;
    }

    /** Starts the job for the staged, fully paid blueprint. */
    public void fabricate(Player player) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        String refusal = this.fabricateRefusal();
        if (refusal != null) {
            tell(player, refusal);
            return;
        }
        double quality = this.previewQuality();
        this.fePerTick = forgeConfig().fePerTick(quality);
        this.totalTicks = forgeConfig().ticks(quality);
        this.progress = 0;
        this.fabricating = true;
        this.markDirtyAndDispatch();
        WoldsVaults.LOGGER.info("{} started fabricating at the Extraction Forge at {}: Q{}, {} ticks at {} FE/t.",
                player.getName().getString(), this.getBlockPos(), quality, this.totalTicks, this.fePerTick);
    }

    private static void tell(Player player, String message) {
        player.displayClientMessage(new TextComponent(message).withStyle(ChatFormatting.YELLOW), false);
    }

    /** The forge's FE buffer; capacity and input rate come from the FORGE block of extractor_costs. */
    private class ForgeEnergy implements IEnergyStorage {
        private int stored;

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int room = Math.max(0, this.getMaxEnergyStored() - this.stored);
            int received = Math.min(room, Math.min(maxReceive, forgeConfig().energyInput));
            if (received > 0 && !simulate) {
                this.stored += received;
                ExtractorForgeTileEntity.this.setChanged();
            }
            return Math.max(0, received);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return this.stored;
        }

        @Override
        public int getMaxEnergyStored() {
            return Math.max(0, forgeConfig().energyCapacity);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    /** What pipes see: slot 0 hands out a finished part; every other slot pays into the staged blueprint's ledger. */
    private class AutomationHandler implements IItemHandler {
        private final IItemHandler ledger;

        AutomationHandler(IItemHandler ledger) {
            this.ledger = ledger;
        }

        private ItemStack finishedPart() {
            ItemStack stack = ExtractorForgeTileEntity.this.inventory.getStackInSlot(BLUEPRINT_SLOT);
            return stack.getItem() instanceof ExtractorPartItem ? stack : ItemStack.EMPTY;
        }

        @Override
        public int getSlots() {
            return 1 + this.ledger.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? this.finishedPart() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == 0 ? stack : this.ledger.insertItem(slot - 1, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || this.finishedPart().isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack part = this.finishedPart();
            if (!simulate) {
                ExtractorForgeTileEntity.this.inventory.setStackInSlot(BLUEPRINT_SLOT, ItemStack.EMPTY);
            }
            return part.copy();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 1 : this.ledger.getSlotLimit(slot - 1);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != 0 && this.ledger.isItemValid(slot - 1, stack);
        }
    }
}
