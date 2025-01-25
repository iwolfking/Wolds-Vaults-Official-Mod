package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.artisan;

import iskallia.vault.block.entity.VaultArtisanStationTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModItems;

@Mixin(value = VaultArtisanStationTileEntity.class, remap = false)
public abstract class MixinArtisanStationTileEntity extends BlockEntity {
    @Shadow @Final @Mutable
    private OverSizedInventory inventory = new OverSizedInventory.FilteredInsert(17, this, this::canInsertInput);

    public MixinArtisanStationTileEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Inject(method = "canInsertInput", at = @At("HEAD"),cancellable = true)
    private void addNewFoci(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(slot == 16) {
            cir.setReturnValue(stack.is(ModItems.ECCENTRIC_FOCUS));
        }
//        if(slot == 17) {
//            cir.setReturnValue(stack.is(ModItems.WEAPON_TYPE_FOCUS));
//        }
    }

    @Shadow public abstract boolean canInsertInput(int slot, @NotNull ItemStack stack);
}
