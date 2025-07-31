package xyz.iwolfking.woldsvaults.blocks.tiles;

import iskallia.vault.entity.entity.DollMiniMeEntity;
import iskallia.vault.init.ModEntities;
import iskallia.vault.init.ModItems;
import iskallia.vault.integration.IntegrationRefinedStorage;
import iskallia.vault.item.VaultDollItem;
import iskallia.vault.util.InventoryUtil;
import iskallia.vault.world.data.DollLootData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
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
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModGameRules;
import xyz.iwolfking.woldsvaults.init.ModSounds;

import java.util.List;
import java.util.Optional;

/**
 * The Doll dissecting table allows to unpack vault dolls without crashing the server.
 */
public class DollDismantlingTileEntity extends BlockEntity
{
    /**
     * Instantiates a new Doll Dissecting table.
     *
     * @param pos the pos
     * @param state the state
     */
    public DollDismantlingTileEntity(BlockPos pos, BlockState state)
    {
        super(ModBlocks.DOLL_DISMANTLING_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);

        // Item extraction related stuff
        this.extractionHandler = new DollItemExtractor();
        this.extractionCapability = LazyOptional.of(() -> this.extractionHandler);

        // Energy related stuff.
        int energyStorage = WoldsVaultsConfig.SERVER.dollDismantlerEnergyStorage.get();
        int energyTransfer = WoldsVaultsConfig.SERVER.dollDismantlerEnergyTransfer.get();

        this.energyConsumption = WoldsVaultsConfig.SERVER.dollDismantlerEnergyConsumption.get();
        this.energyStorage = new EnergyStorage(energyStorage, energyTransfer, energyTransfer);

        this.energyCapability = LazyOptional.of(() -> this.energyStorage);

        // Volume
        this.volume = 0.5F;
    }


    /**
     * Gets selected Doll.
     *
     * @return the selected Doll
     */
    public ItemStack getDoll()
    {
        return this.inventory.getStackInSlot(0);
    }


    /**
     * This method updates selected Doll item.
     *
     * @param itemStack The selected Doll item.
     */
    public void updateDoll(ItemStack itemStack, ServerPlayer player)
    {
        this.inventory.setStackInSlot(0, itemStack);

        CompoundTag dollTag = itemStack.getOrCreateTag();

        VaultDollItem.getDollUUID(dollTag).ifPresentOrElse(dollUuid ->
        {
            if (!dollTag.contains(DollDismantlingTileEntity.REWARD))
            {
                // Handle XP assigning to the player.
                PlayerVaultStatsData statsData = PlayerVaultStatsData.get(player.getLevel());
                statsData.addVaultExp(player, VaultDollItem.getExperience(dollTag));
                dollTag.putBoolean(DollDismantlingTileEntity.REWARD, true);
            }

            DollLootData dollLootData = DollLootData.get(player.getLevel(), dollUuid);
            List<ItemStack> loot = dollLootData.getLoot();

            // Count items from all item-stacks
            DollDismantlingTileEntity.this.totalItemsInDoll = loot.stream().mapToInt(ItemStack::getCount).sum();

            if (!dollTag.contains(DollDismantlingTileEntity.AMOUNT))
            {
                // Update items as rotten once on updating vault doll.
                InventoryUtil.makeItemsRotten(loot);
                dollLootData.setDirty();

                // Update original item count for displaying doll
                dollTag.putLong(DollDismantlingTileEntity.AMOUNT, DollDismantlingTileEntity.this.totalItemsInDoll);
            }

            // Update doll loot data for current doll.
            this.extractionHandler.setDollLootData(dollLootData);

            if (loot.isEmpty())
            {
                // Remove vault doll if there are not items in it.
                DollDismantlingTileEntity.this.removeVaultDoll();
            }
        }, this::removeVaultDoll);
    }


    /**
     * This method removes vault doll from inventory.
     */
    public void removeVaultDoll()
    {
        this.inventory.setStackInSlot(0, ItemStack.EMPTY);
        this.extractionHandler.setDollLootData(null);
        this.totalItemsInDoll = 0;

        this.updateMiniMe();
    }


    /**
     * This method returns if player can insert doll into table.
     *
     * @param stack Doll itemStack
     * @param player Player entity.
     * @return {@code true} if player can insert doll, {@code false} otherwise.
     */
    public boolean playerCanInsertDoll(ItemStack stack, Player player)
    {
        if(!GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, player.getLevel())) {
            return false;
        }

        return player.isCreative() ||
                !VaultDollItem.getPlayerGameProfile(stack).
                        flatMap(gp -> Optional.of(gp.getId())).
                        map(uuid -> !uuid.equals(player.getUUID())).
                        orElse(true);
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
     * @return Number of items inside doll.
     */
    public int getTotalItemsInDoll()
    {
        return this.totalItemsInDoll;
    }


    /**
     * This method returns the entity that is inside block.
     *
     * @return The mini me entity.
     */
    @Nullable
    public DollMiniMeEntity getMiniMeEntity()
    {
        return this.miniMeEntity;
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

        if (tag.contains(DollDismantlingTileEntity.DOLL))
        {
            CompoundTag doll = tag.getCompound(DollDismantlingTileEntity.DOLL);
            this.totalItemsInDoll = doll.getInt(DollDismantlingTileEntity.SIZE);
            this.inventory.deserializeNBT(doll.getCompound(DollDismantlingTileEntity.ITEM));

            if (doll.contains(DollDismantlingTileEntity.MINI))
            {
                // Create new mini-me entity
                this.miniMeEntity = new DollMiniMeEntity(ModEntities.DOLL_MINI_ME, this.getLevel());
                this.miniMeEntity.deserializeNBT(doll.getCompound(DollDismantlingTileEntity.MINI));
            }
            else
            {
                // Remove mini-me entity
                this.miniMeEntity = null;
            }
        }

        // Load energy data from NBT
        this.energyStorage.receiveEnergy(tag.getInt(DollDismantlingTileEntity.ENERGY), false);
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

        CompoundTag doll = new CompoundTag();
        doll.putInt(DollDismantlingTileEntity.SIZE, this.totalItemsInDoll);
        doll.put(DollDismantlingTileEntity.ITEM, this.inventory.serializeNBT());

        if (this.miniMeEntity != null)
        {
            doll.put(DollDismantlingTileEntity.MINI, this.miniMeEntity.serializeNBT());
        }

        tag.put(DollDismantlingTileEntity.DOLL, doll);

        // Put energy storing in tile entity
        tag.putInt(DollDismantlingTileEntity.ENERGY, this.energyStorage.getEnergyStored());
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


    /**
     * This method sets mini me entity for this tile.
     */
    private void updateMiniMe()
    {
        if (!this.getDoll().is(ModItems.VAULT_DOLL) || this.getLevel() == null)
        {
            this.miniMeEntity = null;
        }
        else
        {
            this.miniMeEntity = new DollMiniMeEntity(ModEntities.DOLL_MINI_ME, this.getLevel());
            CompoundTag dataTag = this.getDoll().getOrCreateTag();
            VaultDollItem.getPlayerGameProfile(dataTag).ifPresent(this.miniMeEntity::setGameProfile);
        }

        // Send update to the client
        this.triggerUpdate();
    }


    /**
     * This method sends update to client about changes in tile entity.
     */
    private void triggerUpdate()
    {
        this.setChanged();

        if (this.level instanceof ServerLevel)
        {
            this.level.sendBlockUpdated(this.getBlockPos(),
                    this.getBlockState(),
                    this.getBlockState(),
                    Block.UPDATE_CLIENTS);
        }
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
            if (!this.getDoll().isEmpty())
            {
                // Process item ejecting and energy consumption
                if (this.canOperate())
                {
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
     * @param capability The capability to check
     * @param side The Side to check from,
     * <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     * @param <T> Capability Type.
     * @return LazyOptional with capability.
     */
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side)
    {
        // Use VH method that blocks RS. Just because I do not like RS? Or not... who knows...
        if (IntegrationRefinedStorage.shouldPreventImportingCapability(this.getLevel(), this.getBlockPos(), side))
        {
            return super.getCapability(capability, side);
        }

        if (capability == CapabilityEnergy.ENERGY)
        {
            // Energy capability
            return this.energyCapability.cast();
        }
        else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            // Item extraction capability
            return this.extractionCapability.cast();
        }

        return super.getCapability(capability, side);
    }


// ---------------------------------------------------------------------
// Section: Private classes
// ---------------------------------------------------------------------


    /**
     * This class manages item extraction from DollLootData file.
     */
    private class DollItemExtractor extends ItemStackHandler
    {
        DollItemExtractor()
        {
            super(0);
            this.dollLootData = null;
        }


        /**
         * This method allows to change doll data file for item handler.
         * @param dollLootData New doll data item.
         */
        public void setDollLootData(DollLootData dollLootData)
        {
            this.dollLootData = dollLootData;
        }


        /**
         * Size of slots depends on loot amount inside doll data file.
         * @return amount of item stacks in doll loot data file.
         */
        @Override
        public int getSlots()
        {
            return this.dollLootData != null ? this.dollLootData.getLoot().size() : 0;
        }


        /**
         * This method returns item stack in given slot.
         * @param slot Slot to query
         * @return ItemStack in given slot inside doll loot data file.
         */
        @Override
        @NotNull
        public ItemStack getStackInSlot(int slot)
        {
            return this.dollLootData != null && this.getSlots() > slot ?
                    this.dollLootData.getLoot().get(slot) : ItemStack.EMPTY;
        }


        /**
         * This method handles item extraction from data file.
         * @param slot     Slot to extract from.
         * @param amount   Amount to extract (may be greater than the current stack's max limit)
         * @param simulate If true, the extraction is only simulated
         * @return Extracted item stack from data file.
         */
        @Override
        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (amount == 0)
            {
                return ItemStack.EMPTY;
            }

            if (!DollDismantlingTileEntity.this.getDoll().is(ModItems.VAULT_DOLL))
            {
                // Not a Vault Doll.
                return ItemStack.EMPTY;
            }

            if (!(DollDismantlingTileEntity.this.level instanceof ServerLevel))
            {
                // Only server side
                return ItemStack.EMPTY;
            }

            if (this.dollLootData == null)
            {
                // Doll data is not set
                return ItemStack.EMPTY;
            }

            List<ItemStack> loot = this.dollLootData.getLoot();

            if (loot.size() <= slot)
            {
                // Outside range.
                return ItemStack.EMPTY;
            }

            ItemStack stackInSlot = loot.get(slot);

            if (stackInSlot.isEmpty())
            {
                // Stack is empty
                return ItemStack.EMPTY;
            }

            if (simulate)
            {
                if (stackInSlot.getCount() < amount)
                {
                    return stackInSlot.copy();
                }
                else
                {
                    ItemStack copy = stackInSlot.copy();
                    copy.setCount(amount);
                    return copy;
                }
            }
            else
            {
                if (stackInSlot.getCount() < amount)
                {
                    loot.remove(slot);
                    DollDismantlingTileEntity.this.totalItemsInDoll -= stackInSlot.getCount();

                    this.dollLootData.setDirty();

                    if (loot.isEmpty())
                    {
                        // Remove doll item itself. It triggers update!
                        DollDismantlingTileEntity.this.removeVaultDoll();
                    }
                    else
                    {
                        DollDismantlingTileEntity.this.triggerUpdate();
                    }

                    return stackInSlot;
                }
                else
                {
                    ItemStack copy = stackInSlot.copy();
                    copy.setCount(amount);

                    stackInSlot.shrink(amount);
                    DollDismantlingTileEntity.this.totalItemsInDoll -= amount;

                    if (stackInSlot.isEmpty())
                    {
                        loot.remove(slot);
                    }

                    this.dollLootData.setDirty();

                    if (loot.isEmpty())
                    {
                        // Remove doll item itself. It triggers update!
                        DollDismantlingTileEntity.this.removeVaultDoll();
                    }
                    else
                    {
                        DollDismantlingTileEntity.this.triggerUpdate();
                    }

                    return copy;
                }
            }
        }


        /**
         * This method handles item transfer from current handler.
         * @param slot The slot from which item is transferred.
         * @param stackInSlot The stack in slot that is transferred.
         * @param remainingStack The remaining stack that should be kept inside inventory.
         * @return {@code true} if transfer happened, {@code false} otherwise.
         */
        public boolean transferStack(int slot, ItemStack stackInSlot, ItemStack remainingStack)
        {
            if (this.dollLootData == null || this.getSlots() <= slot)
            {
                return false;
            }

            if (remainingStack.isEmpty())
            {
                this.dollLootData.getLoot().remove(slot);
                DollDismantlingTileEntity.this.totalItemsInDoll -= stackInSlot.getCount();

                this.dollLootData.setDirty();

                if (this.dollLootData.getLoot().isEmpty())
                {
                    // Remove doll item itself.
                    DollDismantlingTileEntity.this.removeVaultDoll();
                }
                else
                {
                    DollDismantlingTileEntity.this.triggerUpdate();
                }

                return true;
            }
            else if (remainingStack.getCount() != stackInSlot.getCount())
            {
                this.dollLootData.getLoot().set(slot, remainingStack);
                this.dollLootData.setDirty();

                DollDismantlingTileEntity.this.totalItemsInDoll -= (stackInSlot.getCount() - remainingStack.getCount());

                DollDismantlingTileEntity.this.triggerUpdate();

                return true;
            }
            else
            {
                return false;
            }
        }


        /**
         * This variable stores vault doll data that is handled by current extractor.
         */
        protected DollLootData dollLootData;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This variable stores extraction handler that manages item extraction from vault doll data
     */
    private final DollItemExtractor extractionHandler;

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
    private int totalItemsInDoll = 0;

    /**
     * The entity that is displayed in the dismantler.
     */
    @Nullable
    private DollMiniMeEntity miniMeEntity;

    /**
     * This variable stores output inventory that contains doll item
     */
    private final ItemStackHandler inventory = new ItemStackHandler(1)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            DollDismantlingTileEntity.this.updateMiniMe();
        }
    };


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------

    /**
     * The parameter for data storage.
     */
    private static final String AMOUNT = "amount";

    /**
     * The parameter for data storage.
     */
    private static final String DOLL = "doll";

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
    private static final String MINI = "mini";

    /**
     * The parameter for data storage.
     */
    private static final String REWARD = "reward";

    /**
     * The parameter for data storage.
     */
    private static final String ENERGY = "energy";
}
