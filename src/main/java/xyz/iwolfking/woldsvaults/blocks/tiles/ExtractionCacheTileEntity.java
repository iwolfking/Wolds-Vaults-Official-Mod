package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class ExtractionCacheTileEntity extends BlockEntity {
    public ExtractionCacheTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EXTRACTION_CACHE_TILE_ENTITY_TYPE, pos, state);
    }
}
