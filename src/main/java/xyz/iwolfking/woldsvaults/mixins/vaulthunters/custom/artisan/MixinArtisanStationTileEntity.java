package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.artisan;

import iskallia.vault.block.entity.VaultArtisanStationTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = VaultArtisanStationTileEntity.class, remap = false)
public abstract class MixinArtisanStationTileEntity extends BlockEntity {
    @Shadow @Final @Mutable
    private OverSizedInventory inventory = new OverSizedInventory(23, this);

    public MixinArtisanStationTileEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }
}
