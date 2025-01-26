package xyz.iwolfking.woldsvaults.mixins;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase extends StateHolder<Block, BlockState> {
    protected MixinBlockStateBase(Block pOwner, ImmutableMap<Property<?>, Comparable<?>> pValues, MapCodec<BlockState> pPropertiesCodec) {
        super(pOwner, pValues, pPropertiesCodec);
    }

    @Shadow
    public abstract Block getBlock();

    @Inject(
            method = {"getDestroySpeed"},
            at = {@At("RETURN")},
            cancellable = true
    )
    public void changeChestBreakSpeed(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (this.getBlock() instanceof VaultChestBlock) {
            if (level.getBlockEntity(pos) instanceof VaultChestTileEntity chest) {
                if (chest.getBlockState().getBlock().equals(ModBlocks.TREASURE_CHEST) ) {
                    cir.setReturnValue(40.0F);
                }
            }
        }
    }
}
