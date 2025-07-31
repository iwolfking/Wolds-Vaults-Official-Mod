package xyz.iwolfking.woldsvaults.blocks.tiles;

import com.google.common.collect.Lists;
import iskallia.vault.block.entity.VaultRecyclerTileEntity;
import iskallia.vault.config.VaultRecyclerConfig;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.item.gear.RecyclableItem;
import iskallia.vault.network.message.RecyclerParticleMessage;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.vhapi.api.data.api.CustomRecyclerOutputs;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultSalvagerContainer;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.lib.SimpleOversizedSidedContainer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class VaultSalvagerTileEntity extends BlockEntity implements MenuProvider {

    private static final Random rand = new Random();
    private final VaultSalvagerTileEntity.SalvagerInventory inventory = new VaultSalvagerTileEntity.SalvagerInventory(10);
    private UUID gearIdProcessing = null;
    private int processTick = 0;
    private LazyOptional<? extends IItemHandler>[] handlers;

    public VaultSalvagerTileEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlocks.VAULT_SALVAGER_ENTITY, pWorldPosition, pBlockState);
        this.handlers = SidedInvWrapper.create(this.inventory, Direction.UP, Direction.DOWN);
    }

    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this && this.inventory.stillValid(player);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, VaultSalvagerTileEntity tile) {
        if (!world.isClientSide() && tile.gearIdProcessing != null) {
            if (!tile.canCraft()) {
                tile.resetProcess(world);
            } else {
                ++tile.processTick;
                if (tile.processTick >= 2) {
                    tile.finishCraft();
                }

                tile.setChanged();
                if (world instanceof ServerLevel serverWorld) {
                    serverWorld.sendBlockUpdated(pos, state, state, 3);
                }

            }
        }
    }

    private void finishCraft() {
        VaultRecyclerConfig.RecyclerOutput output = this.getRecipeOutput();
        if (output != null) {
            if (this.level != null) {
                this.level.playSound(null, this.getBlockPos(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F + new Random().nextFloat() * 0.25F, 0.75F + new Random().nextFloat() * 0.25F);
            }

            ItemStack input = this.inventory.getItem(0).copy();
            float additionalChance = 0.0F;
            if (input.getItem() instanceof VaultGearItem) {
                VaultGearRarity rarity = VaultGearData.read(input).getRarity();
                additionalChance = ModConfigs.VAULT_RECYCLER.getAdditionalOutputRarityChance(rarity);
            }

            this.inventory.removeItem(0, 1);
            if (input.getItem() instanceof VaultGearItem) {
                VaultGearData data = VaultGearData.read(input);
                VaultGearRarity rarity = data.getRarity();
                boolean isCrafted = data.hasAttribute(ModGearAttributes.CRAFTED_BY) || data.getFirstValue(ModGearAttributes.CRAFTED_BY).isPresent();
                additionalChance = ModConfigs.VAULT_RECYCLER.getAdditionalOutputRarityChance(rarity);
                this.inventory.removeItem(0, 1);

                MiscUtils.addStackToSlot(this.inventory, 1, this.getUseRelatedOutput(input, output.generateMainOutput(additionalChance)));
                MiscUtils.addStackToSlot(this.inventory, 2, this.getUseRelatedOutput(input, output.generateExtraOutput1(additionalChance, rarity, isCrafted)));
                if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 3, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 3, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance, rarity, isCrafted)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 4, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 4, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance, rarity, isCrafted)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 5, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 5, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance, rarity, isCrafted)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 6, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 6, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance, rarity, isCrafted)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 7, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 7, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance, rarity, isCrafted)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 8, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 8, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance, rarity, isCrafted)));
                }
                else {
                    MiscUtils.addStackToSlot(this.inventory, 9, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance, rarity, isCrafted)));
                }
            }
            else {
                MiscUtils.addStackToSlot(this.inventory, 1, this.getUseRelatedOutput(input, output.generateMainOutput(additionalChance)));
                MiscUtils.addStackToSlot(this.inventory, 2, this.getUseRelatedOutput(input, output.generateExtraOutput1(additionalChance)));


                if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 3, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 3, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 4, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 4, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 5, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 5, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 6, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 6, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 7, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 7, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance)));
                }
                else if(MiscUtils.canFullyMergeIntoSlot(this.inventory, 8, output.getExtraOutput2Matching())) {
                    MiscUtils.addStackToSlot(this.inventory, 8, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance)));
                }
                else {
                    MiscUtils.addStackToSlot(this.inventory, 9, this.getUseRelatedOutput(input, output.generateExtraOutput2(additionalChance)));
                }
            }


            ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new RecyclerParticleMessage(this.getBlockPos()));
        }
    }

    private ItemStack getUseRelatedOutput(ItemStack input, ItemStack output) {
        float chance = this.getResultPercentage(input);
        float out = output.getCount() * chance;
        int resultCount = Mth.floor(out);
        if (resultCount < 1 && out > 0.0F && rand.nextFloat() < out) {
            ++resultCount;
        }

        ItemStack copyOut = output.copy();
        copyOut.setCount(resultCount);
        return copyOut;
    }

    private boolean canCraft() {
        VaultRecyclerConfig.RecyclerOutput output = this.getRecipeOutput();
        if (output == null) {
            return false;
        }

        if(!MiscUtils.canFullyMergeIntoSlot(this.inventory, 1, output.getMainOutputMatching())) {
            return false;
        }
        else if(!MiscUtils.canFullyMergeIntoSlot(this.inventory, 2, output.getExtraOutput1Matching())) {
            return false;
        }
        else if(!MiscUtils.canFullyMergeIntoSlot(this.inventory, 3, output.getExtraOutput2Matching()) && !MiscUtils.canFullyMergeIntoSlot(this.inventory, 4, output.getExtraOutput2Matching()) && !MiscUtils.canFullyMergeIntoSlot(this.inventory, 5, output.getExtraOutput2Matching())  && !MiscUtils.canFullyMergeIntoSlot(this.inventory, 6, output.getExtraOutput2Matching()) && !MiscUtils.canFullyMergeIntoSlot(this.inventory, 7, output.getExtraOutput2Matching()) && !MiscUtils.canFullyMergeIntoSlot(this.inventory, 8, output.getExtraOutput2Matching())) {
            return  MiscUtils.canFullyMergeIntoSlot(this.inventory, 9, output.getExtraOutput2Matching());
        }
        return  MiscUtils.canFullyMergeIntoSlot(this.inventory, 9, output.getExtraOutput2Matching());

    }

    @Nullable
    private VaultRecyclerConfig.RecyclerOutput getRecipeOutput() {
        ItemStack input = this.inventory.getItem(0);
        if (!this.isValidInput(input)) {
            return null;
        } else {
            if (input.getItem() instanceof RecyclableItem recyclableItem) {
                return recyclableItem.getOutput(input);
            }
            else return CustomRecyclerOutputs.CUSTOM_OUTPUTS.getOrDefault(input.getItem().getRegistryName(), null);
        }
    }

    private void triggerItemChange() {
        ItemStack input = this.inventory.getItem(0);
        if (!this.isValidInput(input)) {
            this.resetProcess(null);
        } else {
            if (input.getItem() instanceof RecyclableItem recyclableItem) {
                recyclableItem.getUuid(input).ifPresent(itemId -> {
                    if ((this.gearIdProcessing == null || !this.gearIdProcessing.equals(itemId)) && this.canCraft()) {
                        this.startProcess(null, itemId);
                    }

                });
            }
            else {
                UUID uuid = UUID.randomUUID();
                if ((this.gearIdProcessing == null || !this.gearIdProcessing.equals(uuid)) && this.canCraft()) {
                    this.startProcess(null, uuid);
                }
            }
        }
    }

    public boolean isValidInput(ItemStack input) {
        if (input.getItem() instanceof RecyclableItem recyclableItem) {
            return recyclableItem.isValidInput(input);
        }
        else return CustomRecyclerOutputs.CUSTOM_OUTPUTS.containsKey(input.getItem().getRegistryName());
    }

    public float getResultPercentage(ItemStack input) {
        if (input.getItem() instanceof RecyclableItem recyclableItem) {
            return recyclableItem.getResultPercentage(input);
        } else {
            if(CustomRecyclerOutputs.CUSTOM_OUTPUTS.containsKey(input.getItem().getRegistryName())) {
                return 1.0F;
            }
            else {
                return 0.0F;
            }

        }
    }

    private void resetProcess(@Nullable Level world) {
        this.startProcess(world, null);
    }

    private void startProcess(@Nullable Level world, UUID id) {
        int prevTick = this.processTick;
        this.gearIdProcessing = id;
        this.processTick = 0;
        this.setChanged();
        if (prevTick != this.processTick && world instanceof ServerLevel serverWorld) {
            serverWorld.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

    }

    public float getProgressPercent() {
        return (float)this.processTick / 2;
    }

    public VaultSalvagerTileEntity.SalvagerInventory getInventory() {
        return this.inventory;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        NBTHelper.deserializeSimpleContainer( this.inventory, tag.getList("inventory", 10));
        this.processTick = tag.getInt("processTick");
        if (tag.hasUUID("gearIdProcessing")) {
            this.gearIdProcessing = tag.getUUID("gearIdProcessing");
        }

    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", NBTHelper.serializeSimpleContainer(this.inventory));
        tag.putInt("processTick", this.processTick);
        if (this.gearIdProcessing != null) {
            tag.putUUID("gearIdProcessing", this.gearIdProcessing);
        }

    }

    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player player) {
        return this.getLevel() == null ? null : new VaultSalvagerContainer(containerId, this.getLevel(), this.getBlockPos(), inv);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == Direction.UP) {
                return this.handlers[0].cast();
            }

            if (facing == Direction.DOWN) {
                return this.handlers[1].cast();
            }
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        Arrays.stream(this.handlers).forEach(LazyOptional::invalidate);
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.handlers = SidedInvWrapper.create(this.inventory, Direction.UP, Direction.DOWN);
    }


    public class SalvagerInventory extends SimpleOversizedSidedContainer {

        public SalvagerInventory(int size) {
            super(size);
        }

        public List<Direction> getAccessibleSlots(int slot) {
            return slot == 0 ? Lists.newArrayList(Direction.UP) : Lists.newArrayList(Direction.DOWN);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            } else {
                if (slot == 0) {
                    if (stack.getItem() instanceof RecyclableItem recyclableItem) {
                        return recyclableItem.isValidInput(stack);
                    }
                    else {
                        return CustomRecyclerOutputs.CUSTOM_OUTPUTS.containsKey(stack.getItem().getRegistryName());
                    }
                }

                return false;
            }
        }

        @Override
        public void setChanged() {
            super.setChanged();
            VaultSalvagerTileEntity.this.setChanged();
            VaultSalvagerTileEntity.this.triggerItemChange();
        }
    }
}
