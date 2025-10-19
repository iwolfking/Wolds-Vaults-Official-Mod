package xyz.iwolfking.woldsvaults.mixins.blackmarkettweaks;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import dev.attackeight.black_market_tweaks.extension.BlackMarketInventory;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.block.entity.base.FilteredInputInventoryTileEntity;
import iskallia.vault.block.entity.base.InventoryRetainerTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.integration.IntegrationRefinedStorage;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "black_market_tweaks")
        }
)
@Mixin(value = BlackMarketTileEntity.class)
public class MixinBlackMarketTileEntity extends BlockEntity implements FilteredInputInventoryTileEntity, InventoryRetainerTileEntity, BlackMarketInventory {

    @Unique
    public final OverSizedInventory bmt$inventory = new OverSizedInventory.FilteredInsert(1, this, (slot, stack) -> stack.is(ModItems.SOUL_ICHOR));

    public MixinBlackMarketTileEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        this.bmt$inventory.load(tag);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        this.bmt$inventory.save(tag);
    }

    @Override
    public void storeInventoryContents(CompoundTag tag) {
        this.bmt$inventory.save("inventory", tag);
    }

    @Override
    public void loadInventoryContents(CompoundTag tag) {
        this.bmt$inventory.load("inventory", tag);
    }

    @Override
    public void clearInventoryContents() {
        this.bmt$inventory.clearContent();
    }

    @Override
    public OverSizedInventory bmt$get() {
        return this.bmt$inventory;
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
