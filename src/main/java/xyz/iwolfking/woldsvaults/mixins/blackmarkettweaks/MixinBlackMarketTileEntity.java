package xyz.iwolfking.woldsvaults.mixins.blackmarkettweaks;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import dev.attackeight.black_market_tweaks.extension.BlackMarketInventory;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.block.entity.base.FilteredInputInventoryTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.integration.IntegrationRefinedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(value = BlackMarketTileEntity.class)
public class MixinBlackMarketTileEntity extends BlockEntity implements FilteredInputInventoryTileEntity {
    public MixinBlackMarketTileEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (IntegrationRefinedStorage.shouldPreventImportingCapability(this.getLevel(), this.getBlockPos(), side) || level == null) {
            return super.getCapability(cap, side);
        }

        if (level.getBlockEntity(this.worldPosition) instanceof BlackMarketTileEntity be) {
            try {
                OverSizedInventory container = ((BlackMarketInventory)be).bmt$get();
                return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.getFilteredInputCapability(side, new Container[]{container}) : super.getCapability(cap, side);
            } catch (Exception e) {
                BlackMarketTweaks.LOGGER.error(e.toString());
            }
        }

       return super.getCapability(cap, side);
    }

    @Override
    public boolean isInventorySideAccessible(@org.jetbrains.annotations.Nullable Direction direction) {
        return true;
    }
}
