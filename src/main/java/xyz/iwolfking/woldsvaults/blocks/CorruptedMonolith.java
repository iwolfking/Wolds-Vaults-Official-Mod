package xyz.iwolfking.woldsvaults.blocks;


import iskallia.vault.util.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.tiles.CorruptedMonolithTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;


public class CorruptedMonolith extends Block implements EntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 32, 14); // temp
    private static final VoxelShape SHAPE_TOP = SHAPE.move(0, -1, 0);

    public CorruptedMonolith() {
        super(Properties.of(Material.STONE).sound(SoundType.METAL).strength(-1.0F, 3600000.0F).noDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF).add(FILLED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? SHAPE_TOP : SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        if(blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return ModBlocks.CORRUPTED_MONOLITH_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(blockPos, blockState);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return BlockHelper.getTicker(type, ModBlocks.CORRUPTED_MONOLITH_TILE_ENTITY_BLOCK_ENTITY_TYPE, CorruptedMonolithTileEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, world, pos, newState, isMoving);
        if (!state.is(newState.getBlock())) {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                BlockState otherState = world.getBlockState(pos.below());
                if (otherState.is(state.getBlock())) {
                    world.removeBlock(pos.below(), isMoving);
                }
            } else {
                BlockState otherState = world.getBlockState(pos.above());
                if (otherState.is(state.getBlock())) {
                    world.removeBlock(pos.above(), isMoving);
                }
            }
        }
    }
}
