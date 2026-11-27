package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorStructureTileEntity;

import javax.annotation.Nullable;

/**
 * Filler cell of an extractor station. {@code facing} + {@code idx} select the baked tile model for
 * that cell (see the art's blockstate files); every interaction and break routes to the controller.
 */
public abstract class ExtractorStructureBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected ExtractorStructureBlock() {
        super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(5.0F, 10.0F).requiresCorrectToolForDrops().noOcclusion()
                .lightLevel(state -> ExtractorControllerBlock.LIGHT_LEVEL));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(this.idxProperty(), 0));
    }

    /** Static per subclass so it exists while the superclass constructor builds the state definition. */
    public abstract IntegerProperty idxProperty();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, this.idxProperty());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtractorStructureTileEntity(pos, state);
    }

    @Nullable
    public static BlockPos controllerOf(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ExtractorStructureTileEntity tile ? tile.getControllerPos() : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos controller = controllerOf(level, pos);
        if (controller == null) {
            return InteractionResult.PASS;
        }
        BlockState controllerState = level.getBlockState(controller);
        if (!(controllerState.getBlock() instanceof ExtractorControllerBlock)) {
            return InteractionResult.PASS;
        }
        return controllerState.use(level, player, hand, hit.withPosition(controller));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos controller = controllerOf(level, pos);
        return controller == null ? ItemStack.EMPTY : new ItemStack(level.getBlockState(controller).getBlock());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockPos controller = controllerOf(level, pos);
            if (controller != null && level.getBlockState(controller).getBlock() instanceof ExtractorControllerBlock) {
                level.destroyBlock(controller, true);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static class AssemblerPart extends ExtractorStructureBlock {
        public static final IntegerProperty IDX = IntegerProperty.create("idx", 0, ExtractorAssemblerBlock.WIDTH * ExtractorAssemblerBlock.HEIGHT * ExtractorAssemblerBlock.DEPTH - 1);

        @Override
        public IntegerProperty idxProperty() {
            return IDX;
        }
    }

    public static class ForgePart extends ExtractorStructureBlock {
        public static final IntegerProperty IDX = IntegerProperty.create("idx", 0, ExtractorForgeBlock.WIDTH * ExtractorForgeBlock.HEIGHT * ExtractorForgeBlock.DEPTH - 1);

        @Override
        public IntegerProperty idxProperty() {
            return IDX;
        }
    }
}
