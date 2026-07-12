package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class DecoVelaraAltarTileEntity extends BlockEntity {

    public DecoVelaraAltarTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DECO_VELARA_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }
}