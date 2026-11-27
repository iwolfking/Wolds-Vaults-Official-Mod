package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import xyz.iwolfking.woldsvaults.blocks.MobileFluidTankBlock;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModFluids;
import xyz.iwolfking.woldsvaults.items.MobileFluidTankItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** The placed Mobile Extractor Fluid Tank: one Prismatic Glue tank, open to pipes on every face, shown as a fill level. */
public class MobileFluidTankTileEntity extends BlockEntity {
    private final FluidTank tank = new FluidTank(MobileFluidTankItem.CAPACITY, stack -> stack.getFluid() == ModFluids.PRISMATIC_GLUE.get()) {
        @Override
        protected void onContentsChanged() {
            MobileFluidTankTileEntity.this.onTankChanged();
        }
    };
    private final LazyOptional<IFluidHandler> tankCapability = LazyOptional.of(() -> this.tank);

    public MobileFluidTankTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MOBILE_FLUID_TANK_TILE_ENTITY_TYPE, pos, state);
    }

    public FluidTank getTank() {
        return this.tank;
    }

    private void onTankChanged() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        BlockState state = this.getBlockState();
        int fill = MobileFluidTankItem.fillLevel(this.tank.getFluidAmount());
        if (state.hasProperty(MobileFluidTankBlock.LEVEL) && state.getValue(MobileFluidTankBlock.LEVEL) != fill) {
            this.level.setBlock(this.worldPosition, state.setValue(MobileFluidTankBlock.LEVEL, fill), 3);
        }
    }

    /** Takes over the fluid a tank item carried when it was placed. */
    public void readFromItem(ItemStack stack) {
        this.tank.setFluid(MobileFluidTankItem.getFluid(stack));
        this.onTankChanged();
    }

    /** The tank as an item, carrying whatever is inside. */
    public ItemStack toItem() {
        ItemStack stack = new ItemStack(ModBlocks.MOBILE_FLUID_TANK);
        MobileFluidTankItem.setFluid(stack, this.tank.getFluid());
        return stack;
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        this.tank.readFromNBT(tag.getCompound("Tank"));
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", this.tank.writeToNBT(new CompoundTag()));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!this.isRemoved() && cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, this.tankCapability);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.tankCapability.invalidate();
    }

    public FluidStack getFluid() {
        return this.tank.getFluid();
    }
}
