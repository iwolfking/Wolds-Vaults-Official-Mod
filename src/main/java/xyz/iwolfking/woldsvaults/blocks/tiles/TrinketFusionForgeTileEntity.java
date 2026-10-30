package xyz.iwolfking.woldsvaults.blocks.tiles;

import iskallia.vault.container.TrinketForgeContainer;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.item.gear.VaultUsesHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.containers.TrinketFusionContainer;
import xyz.iwolfking.woldsvaults.effect.trinkets.SpeedLimitTrinketEffect;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModFluids;
import xyz.iwolfking.woldsvaults.items.CombinedTrinketItem;

import java.util.ArrayList;
import java.util.List;

public class TrinketFusionForgeTileEntity extends BlockEntity implements MenuProvider {
    private static final int FLUID_COST = 1000;
    public static final int MAX_FLUID = 4000;

    public final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }
    };

    // slots:
    // 0 = trinket A
    // 1 = trinket B
    // 2 = output
    private final FluidTank tank = new FluidTank(4000) {

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == ModFluids.PRISMATIC_GLUE.get();
        }

        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private int progress = 0;
    private final int maxProgress = 100;

    private boolean crafting = false;

    private ItemStack storedA = ItemStack.EMPTY;
    private ItemStack storedB = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;

    public TrinketFusionForgeTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TRINKET_FUSION_FORGE_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrinketFusionForgeTileEntity be) {
        if (level.isClientSide) return;

        if (!be.crafting) {
            if (be.canCraft()) {
                be.startCraft();
            }
            return;
        }

        be.progress++;

        if (be.progress >= be.maxProgress) {
            be.finishCraft();
        }

        be.sync();
    }

    private void startCraft() {
        if (crafting) return;
        if (!canCraft()) return;

        ItemStack a = items.getStackInSlot(0);
        ItemStack b = items.getStackInSlot(1);

        this.storedA = a.copy();
        this.storedB = b.copy();

        items.setStackInSlot(0, ItemStack.EMPTY);
        items.setStackInSlot(1, ItemStack.EMPTY);

        tank.drain(FLUID_COST, IFluidHandler.FluidAction.EXECUTE);

        List<TrinketEffect<?>> effects = new ArrayList<>();
        TrinketItem.getTrinket(storedA).ifPresent(effects::add);
        TrinketItem.getTrinket(storedB).ifPresent(effects::add);



        this.result = CombinedTrinketItem.createCombined(
                effects,
                VaultUsesHelper.getUses(storedA) + VaultUsesHelper.getUses(storedB)
        );

        int capPercent = Math.max(SpeedLimitTrinketEffect.getCapPercent(storedA), SpeedLimitTrinketEffect.getCapPercent(storedB));
        if (capPercent > 0) {
            SpeedLimitTrinketEffect.setCapPercent(this.result, capPercent);
        }

        this.progress = 0;
        this.crafting = true;
        sync();
    }

    private boolean canCraft() {
        ItemStack a = items.getStackInSlot(0);
        ItemStack b = items.getStackInSlot(1);

        if (!(a.getItem().equals(ModItems.TRINKET))) return false;
        if (!(b.getItem().equals(ModItems.TRINKET))) return false;

        if(TrinketItem.getTrinket(a).equals(TrinketItem.getTrinket(b))) {
            return false;
        }

        if (tank.getFluidAmount() < FLUID_COST) return false;

        return items.getStackInSlot(2).isEmpty();
    }

    private void finishCraft() {
        items.setStackInSlot(2, result.copy());

        this.result = ItemStack.EMPTY;
        this.storedA = ItemStack.EMPTY;
        this.storedB = ItemStack.EMPTY;

        this.crafting = false;
        this.progress = 0;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return LazyOptional.of(() -> items).cast();
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return LazyOptional.of(() -> tank).cast();
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) return LazyOptional.of(() -> tank).cast();
        return super.getCapability(cap, side);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;

        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);

            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        level,
                        worldPosition.getX(),
                        worldPosition.getY(),
                        worldPosition.getZ(),
                        stack
                );
            }
        }

        if (crafting && !result.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), result);
        }
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("tile.woldsvaults.trinket_fusion_forge");
    }


    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new TrinketFusionContainer(pContainerId, pPlayerInventory, this);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    public int getMaxFluid() {
        return tank.getCapacity();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("progress", progress);
        tag.putBoolean("crafting", crafting);

        if (!result.isEmpty()) {
            tag.put("result", result.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        tank.readFromNBT(tag.getCompound("tank"));
        progress = tag.getInt("progress");
        crafting = tag.getBoolean("crafting");
        if (tag.contains("result")) {
            result = ItemStack.of(tag.getCompound("result"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        load(tag);
    }

    private void sync() {
        setChanged();

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    3
            );
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}