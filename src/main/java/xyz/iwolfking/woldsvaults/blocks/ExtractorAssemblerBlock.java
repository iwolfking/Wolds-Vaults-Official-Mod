package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorAssemblerTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

/** The 3×3×3 Extractor Assembler: seven part slots, up to three attachment slots, one output. */
public class ExtractorAssemblerBlock extends ExtractorControllerBlock {
    public static final int WIDTH = 3;
    public static final int HEIGHT = 3;
    public static final int DEPTH = 3;

    public ExtractorAssemblerBlock() {
        super(WIDTH, HEIGHT, DEPTH);
    }

    @Override
    public ExtractorStructureBlock getPartBlock() {
        return ModBlocks.EXTRACTOR_ASSEMBLER_PART_BLOCK;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtractorAssemblerTileEntity(pos, state);
    }

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(type, ModBlocks.EXTRACTOR_ASSEMBLER_TILE_ENTITY_TYPE, ExtractorAssemblerTileEntity::tick);
    }
}
