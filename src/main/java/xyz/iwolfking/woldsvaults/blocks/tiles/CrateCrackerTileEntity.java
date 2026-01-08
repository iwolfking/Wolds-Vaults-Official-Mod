package xyz.iwolfking.woldsvaults.blocks.tiles;

import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModSounds;

import java.util.*;

public class CrateCrackerTileEntity extends BlockEntity
{
    private int initialItemsInCrate = 0;


    /**
     * Instantiates a new Crate Cracker table.
     *
     * @param pos the pos
     * @param state the state
     */
    public CrateCrackerTileEntity(BlockPos pos, BlockState state)
    {
        super(ModBlocks.CRATE_CRACKER_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);

        // Item extraction related stuff
        this.extractionHandler = new CrateItemExtractor();
        this.extractionCapability = LazyOptional.of(() -> this.extractionHandler);

        // Energy related stuff.
        int energyStorage = WoldsVaultsConfig.SERVER.dollDismantlerEnergyStorage.get();
        int energyTransfer = WoldsVaultsConfig.SERVER.dollDismantlerEnergyTransfer.get();

        this.energyConsumption = WoldsVaultsConfig.SERVER.dollDismantlerEnergyConsumption.get();
        this.energyStorage = new EnergyStorage(energyStorage, energyTransfer, energyTransfer);

        this.energyCapability = LazyOptional.of(() -> this.energyStorage);

        // Volume
        this.volume = 0.25F;
    }


    /**
     * Gets selected Crate.
     *
     * @return the selected Crate
     */
    public ItemStack getCrate()
    {
        return this.inventory.getStackInSlot(0);
    }

    private static List<OverSizedItemStack> getItems(ItemStack crate) {
        CompoundTag tag = crate.getTag();
        if (tag == null)
            return List.of();
        CompoundTag cmp = tag.getCompound("BlockEntityTag");
        List<OverSizedItemStack> items = new ArrayList<>();
        NBTHelper.readCollection(cmp, "items", CompoundTag.class, OverSizedItemStack::deserialize, items);
        List<OverSizedItemStack> compactItems = new ArrayList<>();
        for (OverSizedItemStack it : items) {
            boolean added = false;
            for (int i = 0; i < compactItems.size(); i++) {
                OverSizedItemStack ci = compactItems.get(i);
                if (ci.stack().getItem().equals(it.stack().getItem()) && (Objects.equals(ci.stack().getItem(), it.stack().getItem()) || it.stack().getItem() instanceof iskallia.vault.gear.item.VaultGearItem))
                    if (ci.stack().getItem() instanceof iskallia.vault.gear.item.VaultGearItem) {
                        ItemStack ciStack = ci.stack().getContainerItem();
                        ItemStack itStack = it.stack().getContainerItem();
                        GearDataCache ciData = GearDataCache.of(ciStack);
                        GearDataCache itData = GearDataCache.of(itStack);
                        if (Objects.equals(ciData.getGearRollType(), itData.getGearRollType()) && ciData
                                .hasAttribute(ModGearAttributes.IS_LEGENDARY) == itData.hasAttribute(ModGearAttributes.IS_LEGENDARY)) {
                            compactItems.set(i, new OverSizedItemStack(ciStack, ci.amount() + it.amount()));
                            added = true;
                        }
                    } else {
                        compactItems.set(i, new OverSizedItemStack(ci.stack(), ci.amount() + it.amount()));
                        added = true;
                    }
            }
            if (!added)
                compactItems.add(it);
        }
        compactItems.sort(
                Comparator.comparingInt(OverSizedItemStack::amount).reversed()
                        .thenComparing(s -> s.stack().getItem() instanceof iskallia.vault.gear.item.VaultGearItem)
                        .thenComparing(x -> {
                            ResourceLocation rn = x.stack().getItem().getRegistryName();
                            return (rn == null) ? "" : rn.toString();
                        }));
        return compactItems;
    }

    /**
     * This method updates selected Crate item.
     *
     * @param itemStack The selected Crate item.
     */
    public void updateCrate(ItemStack itemStack)
    {
        this.inventory.setStackInSlot(0, itemStack);

        if(itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof VaultCrateBlock) {
            List<OverSizedItemStack> items = getItems(itemStack);
            int total = 0;
            for(OverSizedItemStack stack : items) {
                total += stack.overSizedStack().getCount();
            }
            CrateCrackerTileEntity.this.totalItemsInCrate = total;
            CrateCrackerTileEntity.this.extractionHandler.setCrateLootData(items);
            if(items.isEmpty()) {
                CrateCrackerTileEntity.this.removeVaultCrate();
            }
        }
    }


    /**
     * This method removes vault doll from inventory.
     */
    public void removeVaultCrate()
    {
        this.inventory.setStackInSlot(0, ItemStack.EMPTY);
        this.extractionHandler.setCrateLootData(null);
        this.totalItemsInCrate = 0;
    }


    /**
     * This method returns if player can insert crate into table.
     *
     * @return {@code true} if player can insert crate, {@code false} otherwise.
     */
    public boolean playerCanInsertCrate()
    {
        return this.inventory.getStackInSlot(0).isEmpty();
    }


    /**
     * Gets inventory.
     *
     * @return the inventory
     */
    public ItemStackHandler getInventory()
    {
        return this.inventory;
    }


    /**
     * This method returns number of items currently stored inside doll.
     *
     * @return Number of items inside crate.
     */
    public int getTotalItemsInCrate()
    {
        return this.totalItemsInCrate;
    }



// ---------------------------------------------------------------------
// Section: Data storing and loading
// ---------------------------------------------------------------------


    /**
     * This method loads table content from NBT data.
     *
     * @param tag The NBT data that contains table content.
     */
    @Override
    public void load(@NotNull CompoundTag tag)
    {
        super.load(tag);

        if (tag.contains(CrateCrackerTileEntity.CRATE))
        {
            CompoundTag crate = tag.getCompound(CrateCrackerTileEntity.CRATE);
            this.totalItemsInCrate = crate.getInt(CrateCrackerTileEntity.SIZE);
            this.inventory.deserializeNBT(crate.getCompound(CrateCrackerTileEntity.ITEM));
        }

        // Load energy data from NBT
        this.energyStorage.receiveEnergy(tag.getInt(CrateCrackerTileEntity.ENERGY), false);
    }


    /**
     * This method saves table content into NBT data.
     *
     * @param tag The NBT data where table content need to be saved.
     */
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag)
    {
        super.saveAdditional(tag);

        CompoundTag crate = new CompoundTag();
        crate.putInt(CrateCrackerTileEntity.SIZE, this.totalItemsInCrate);
        crate.put(CrateCrackerTileEntity.ITEM, this.inventory.serializeNBT());

        tag.put(CrateCrackerTileEntity.CRATE, crate);

        // Put energy storing in tile entity
        tag.putInt(CrateCrackerTileEntity.ENERGY, this.energyStorage.getEnergyStored());
    }


    /**
     * This method updates NBT tag.
     *
     * @return Method that updates NBT tag.
     */
    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }


    /**
     * This method updates table content to client.
     *
     * @return Packet that is sent to client
     */
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }


// ---------------------------------------------------------------------
// Section: Private methods
// ---------------------------------------------------------------------


    /**
     * This method ejects items from tile entity into block bellow it.
     */
    private void autoEjectItems()
    {
        BlockPos belowPos = this.getBlockPos().below();

        // Get the BlockEntity below (if any)
        BlockEntity belowBlockEntity = this.getLevel().getBlockEntity(belowPos);

        if (belowBlockEntity != null)
        {

            // Check if the block below has an IItemHandler capability (i.e., it can accept items)
            belowBlockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).
                    ifPresent(belowHandler ->
                    {
                        // Try to transfer items from this block's inventory to the inventory below
                        for (int slot = 0; slot < this.extractionHandler.getSlots(); slot++)
                        {

                            ItemStack stackInSlot = this.extractionHandler.getStackInSlot(slot);

                            if (!stackInSlot.isEmpty())
                            {
                                int count = stackInSlot.getCount();
                                boolean repopulate = count > stackInSlot.getMaxStackSize();

                                if  (repopulate)
                                {
                                    // Adjust the size of stack to be max stack size.
                                    stackInSlot = stackInSlot.copy();
                                    stackInSlot.setCount(stackInSlot.getMaxStackSize());
                                    count -= stackInSlot.getMaxStackSize();
                                }

                                // Attempt to move the stack to the below inventory
                                ItemStack remainingStack = this.transferStack(belowHandler, stackInSlot);

                                if (repopulate)
                                {
                                    // Add missing stack count to remainingStack item.
                                    if (remainingStack.isEmpty())
                                    {
                                        remainingStack = stackInSlot.copy();
                                        remainingStack.setCount(count);
                                    }
                                    else
                                    {
                                        remainingStack.setCount(remainingStack.getCount() + count);
                                    }
                                }

                                if (this.extractionHandler.transferStack(slot, stackInSlot, remainingStack))
                                {
                                    // Only move 1 stack per tick.
                                    break;
                                }
                            }
                        }

                        this.setChanged(); // Mark the block entity as changed to save its state
                    });
        }
    }


    /**
     * This method tries to transfer stack into target handler.
     *
     * @param targetHandler TargetHandler that is receiving item
     * @param stack The item stack that handler is receiving
     * @return Remaining item stack that were not inserted.
     */
    private ItemStack transferStack(IItemHandler targetHandler, ItemStack stack)
    {
        for (int slot = 0; slot < targetHandler.getSlots(); slot++)
        {
            ItemStack stackInSlot = targetHandler.getStackInSlot(slot);

            if (targetHandler.insertItem(slot, stack, true).getCount() != stack.getCount())
            {
                if (stackInSlot.isEmpty())
                {
                    targetHandler.insertItem(slot, stack, false);
                    stack = ItemStack.EMPTY;
                }
                else if (ItemHandlerHelper.canItemStacksStack(stackInSlot, stack))
                {
                    stack = targetHandler.insertItem(slot, stack, false);
                }
            }

            if (stack.isEmpty())
            {
                break;
            }
        }

        return stack;
    }


    /**
     * This method checks if energy levels are enough for tile entity to operate.
     *
     * @return {@code true} if it has enough energy, {@code false} otherwise
     */
    public boolean canOperate()
    {
        return this.energyStorage.getEnergyStored() >= this.energyConsumption;
    }


    /**
     * This method consumes energy for operating
     */
    private void consumeEnergy()
    {
        this.energyStorage.extractEnergy(this.energyConsumption, false);
    }

    public void updateCrateContents() {
        ItemStack crateStack = this.inventory.getStackInSlot(0);
        if(crateStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof VaultCrateBlock) {
            CompoundTag tag = crateStack.getTag();
            if(tag == null) {
                return;
            }
            CompoundTag cmp = tag.getCompound("BlockEntityTag");
            List<OverSizedItemStack> items = CrateCrackerTileEntity.this.extractionHandler.crateLootData;
            NBTHelper.writeCollection(cmp, "items", items, CompoundTag.class, OverSizedItemStack::serialize);
        }
    }


    /**
     * This method sends update to client about changes in tile entity.
     */
    private void triggerUpdate()
    {
        this.setChanged();

        updateCrateContents();

        if (this.level instanceof ServerLevel)
        {
            this.level.sendBlockUpdated(this.getBlockPos(),
                    this.getBlockState(),
                    this.getBlockState(),
                    Block.UPDATE_CLIENTS);
        }
    }

    public int getInitialItemsInCrate()
    {
        return this.initialItemsInCrate;
    }


// ---------------------------------------------------------------------
// Section: Implementations
// ---------------------------------------------------------------------


    /**
     * This method manages entity ticking process.
     */
    public void tick()
    {
        if (this.getLevel() != null && !this.getLevel().isClientSide)
        {
            // If doll is present and not air, trigger the sound loop
            if (!this.getCrate().isEmpty())
            {
                // Process item ejecting and energy consumption
                if (this.canOperate())
                {
                    if(this.extractionHandler.crateLootData == null) {
                        updateCrate(this.getCrate());
                    }

                    // Play sound every 2 seconds if volume is set larger than 0.
                    if (this.volume != 0)
                    {
                        if (this.soundTickCooldown <= 0)
                        {
                            this.getLevel().playSound(null,
                                    this.getBlockPos(),
                                    ModSounds.BLENDER,
                                    SoundSource.BLOCKS,
                                    (float) this.volume,
                                    1.0F);

                            this.soundTickCooldown = 19;
                        }
                        else
                        {
                            this.soundTickCooldown--;
                        }
                    }

                    if (this.extractionTick <= 0)
                    {
                        this.autoEjectItems();
                        this.extractionTick = WoldsVaultsConfig.SERVER.dollDismantlerExtractionSpeed.get();
                    }

                    this.consumeEnergy();
                    this.extractionTick--;
                }
            }
            else
            {
                // Reset the cooldown when no doll is present
                this.soundTickCooldown = 0;
            }
        }

        if (this.level != null && this.level.isClientSide)
        {
            if (!this.getCrate().isEmpty() && this.canOperate())
            {
                spawnCrateParticles();
            }
        }

    }

    private void spawnCrateParticles()
    {
        if (!(this.getCrate().getItem() instanceof BlockItem blockItem))
            return;

        BlockState state = blockItem.getBlock().defaultBlockState();
        Random rand = this.level.random;

        if (rand.nextFloat() > 0.3F)
            return;

        double x = this.worldPosition.getX() + 0.5D;
        double y = this.worldPosition.getY() + 0.8D;
        double z = this.worldPosition.getZ() + 0.5D;

        double dx = (rand.nextDouble() - 0.5D) * 0.05D;
        double dy = rand.nextDouble() * 0.05D;
        double dz = (rand.nextDouble() - 0.5D) * 0.05D;

        this.level.addParticle(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                x + (rand.nextDouble() - 0.5D) * 0.4D,
                y,
                z + (rand.nextDouble() - 0.5D) * 0.4D,
                dx,
                dy,
                dz
        );
    }



    /**
     * Manages tile entity removing
     */
    @Override
    public void setRemoved()
    {
        super.setRemoved();
        this.energyCapability.invalidate();
        this.extractionCapability.invalidate();
    }


    /**
     * Invalidates capabilities for tile entities
     */
    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        this.energyCapability.invalidate();
        this.extractionCapability.invalidate();
    }


    /**
     * This method returns requested capability for given side direction.
     *
     * @param cap The capability to check
     * @param side The Side to check from,
     * <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     * @param <T> Capability Type.
     * @return LazyOptional with capability.
     */
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if (side == Direction.DOWN)
            {
                // Output only
                return this.extractionCapability.cast();
            }
            else
            {
                // Input only
                return LazyOptional.of(() -> this.inventory).cast();
            }
        }

        if (cap == CapabilityEnergy.ENERGY)
            return this.energyCapability.cast();

        return super.getCapability(cap, side);
    }



// ---------------------------------------------------------------------
// Section: Private classes
// ---------------------------------------------------------------------


    /**
     * This class manages item extraction from DollLootData file.
     */
    private class CrateItemExtractor extends ItemStackHandler {

        /**
         * This variable stores vault doll data that is handled by current extractor.
         */
        protected List<OverSizedItemStack> crateLootData;

        CrateItemExtractor() {
            super(0);
            this.crateLootData = null;
        }

        /**
         * This method allows to change doll data file for item handler.
         *
         * @param crateLootData New doll data item.
         */
        public void setCrateLootData(List<OverSizedItemStack> crateLootData) {
            this.crateLootData = crateLootData;
        }


        /**
         * Size of slots depends on loot amount inside doll data file.
         *
         * @return amount of item stacks in doll loot data file.
         */
        @Override
        public int getSlots() {
            return this.crateLootData != null ? this.crateLootData.size() : 0;
        }


        /**
         * This method returns item stack in given slot.
         *
         * @param slot Slot to query
         * @return ItemStack in given slot inside doll loot data file.
         */
        @Override
        @NotNull
        public ItemStack getStackInSlot(int slot)
        {
            if (crateLootData == null || slot >= crateLootData.size())
                return ItemStack.EMPTY;

            OverSizedItemStack data = crateLootData.get(slot);
            ItemStack stack = data.stack().copy();
            stack.setCount(Math.min(stack.getMaxStackSize(), data.amount()));
            return stack;
        }



        /**
         * This method handles item extraction from data file.
         *
         * @param slot     Slot to extract from.
         * @param amount   Amount to extract (may be greater than the current stack's max limit)
         * @param simulate If true, the extraction is only simulated
         * @return Extracted item stack from data file.
         */
        @Override
        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0) {
                return ItemStack.EMPTY;
            }

            if (!(CrateCrackerTileEntity.this.getCrate().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof VaultCrateBlock)) {
                // Not a Vault Crate.
                return ItemStack.EMPTY;
            }

            if (!(CrateCrackerTileEntity.this.level instanceof ServerLevel)) {
                // Only server side
                return ItemStack.EMPTY;
            }

            if (this.crateLootData == null) {
                // Crate data is not set
                return ItemStack.EMPTY;
            }

            List<OverSizedItemStack> loot = this.crateLootData;

            if (loot.size() <= slot) {
                // Outside range.
                return ItemStack.EMPTY;
            }

            ItemStack stackInSlot = loot.get(slot).overSizedStack();

            if (stackInSlot.isEmpty()) {
                // Stack is empty
                return ItemStack.EMPTY;
            }

            if (simulate) {
                if (stackInSlot.getCount() < amount) {
                    return stackInSlot.copy();
                } else {
                    ItemStack copy = stackInSlot.copy();
                    copy.setCount(amount);
                    return copy;
                }
            } else {
                if (stackInSlot.getCount() < amount) {
                    loot.remove(slot);
                    CrateCrackerTileEntity.this.totalItemsInCrate -= stackInSlot.getCount();

                    if (loot.isEmpty()) {
                        // Remove doll item itself. It triggers update!
                        CrateCrackerTileEntity.this.removeVaultCrate();
                    } else {
                        CrateCrackerTileEntity.this.triggerUpdate();
                    }

                    return stackInSlot;
                } else {
                    ItemStack copy = stackInSlot.copy();
                    copy.setCount(amount);

                    stackInSlot.shrink(amount);
                    CrateCrackerTileEntity.this.totalItemsInCrate -= amount;

                    if (stackInSlot.isEmpty()) {
                        loot.remove(slot);
                    }

                    if (loot.isEmpty()) {
                        // Remove doll item itself. It triggers update!
                        CrateCrackerTileEntity.this.removeVaultCrate();
                    } else {
                        CrateCrackerTileEntity.this.triggerUpdate();
                    }

                    return copy;
                }
            }
        }


        /**
         * This method handles item transfer from current handler.
         *
         * @param slot      The slot from which item is transferred.
         * @param extracted The stack in slot that is transferred.
         * @param remaining The remaining stack that should be kept inside inventory.
         * @return {@code true} if transfer happened, {@code false} otherwise.
         */
        public boolean transferStack(int slot, ItemStack extracted, ItemStack remaining) {
            if (crateLootData == null || slot >= crateLootData.size())
                return false;

            OverSizedItemStack data = crateLootData.get(slot);

            int extractedAmount = extracted.getCount();
            int remainingAmount = remaining.getCount();
            int delta = extractedAmount - remainingAmount;

            if (delta <= 0)
                return false;

            int newAmount = data.amount() - delta;

            if (newAmount <= 0) {
                crateLootData.remove(slot);
            } else {
                crateLootData.set(slot, data.copyAmount(newAmount));
            }

            totalItemsInCrate -= delta;

            if (crateLootData.isEmpty())
                CrateCrackerTileEntity.this.removeVaultCrate();
            else
                CrateCrackerTileEntity.this.triggerUpdate();

            return true;
        }
    }




// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This variable stores extraction handler that manages item extraction from vault doll data
     */
    private final CrateItemExtractor extractionHandler;

    /**
     * This optional stores extraction capability object.
     */
    private final LazyOptional<IItemHandler> extractionCapability;

    /**
     * This variable stores energy storage for current object.
     */
    private final EnergyStorage energyStorage;

    /**
     * This optional stores energy capability object.
     */
    private final LazyOptional<EnergyStorage> energyCapability;

    /**
     * This variable stores how much energy is consumed by current tile entity.
     */
    private final int energyConsumption;

    /**
     * This variable stores the volume of tile entity.
     */
    private final double volume;

    /**
     * This variable stores cooldown before next sound should be played.
     */
    private int soundTickCooldown = 0;

    /**
     * This variable stores extraction tick count.
     */
    private int extractionTick = 0;

    /**
     * This variable stores how many items are in the doll currently.
     */
    private int totalItemsInCrate = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(1)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            CrateCrackerTileEntity.this.setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack)
        {
            return stack.getItem() instanceof BlockItem bi &&
                    bi.getBlock() instanceof VaultCrateBlock;
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 1;
        }
    };


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------

    /**
     * The parameter for data storage.
     */
    private static final String CRATE = "crate";

    /**
     * The parameter for data storage.
     */
    private static final String SIZE = "size";

    /**
     * The parameter for data storage.
     */
    private static final String ITEM = "item";

    /**
     * The parameter for data storage.
     */
    private static final String ENERGY = "energy";
}
