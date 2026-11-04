package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorForgeTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

/**
 * The 5×5×5 Extraction Forge: fabricates parts from identified blueprints. Using a filled fluid container on any
 * cell pays it into a staged blueprint's fluid lines; anything else opens the menu.
 */
public class ExtractorForgeBlock extends ExtractorControllerBlock {
    public static final int WIDTH = 5;
    public static final int HEIGHT = 5;
    public static final int DEPTH = 5;

    public ExtractorForgeBlock() {
        super(WIDTH, HEIGHT, DEPTH);
    }

    @Override
    public ExtractorStructureBlock getPartBlock() {
        return ModBlocks.EXTRACTION_FORGE_PART_BLOCK;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtractorForgeTileEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ExtractorForgeTileEntity forge && forge.isStaged()
                && FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()
                && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(type, ModBlocks.EXTRACTOR_FORGE_TILE_ENTITY_TYPE, ExtractorForgeTileEntity::tick);
    }
}
