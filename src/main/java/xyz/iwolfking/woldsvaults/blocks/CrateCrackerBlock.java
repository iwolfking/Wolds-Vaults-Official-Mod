package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.util.VHSmpUtil;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.tiles.CrateCrackerTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.Objects;


public class CrateCrackerBlock extends HorizontalDirectionalBlock implements EntityBlock
{
    /**
     * The shape crate cracker table.
     */
    public static final VoxelShape CRATE_CRACKER_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(2, 4, 2, 14, 5, 14),
            Block.box(1, 5, 1, 15, 16, 15)
    );

    /**
     * Instantiates a new Crate Cracker table block.
     */
    public CrateCrackerBlock(Properties properties, VoxelShape shape)
    {
        super(properties.noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().
                setValue(FACING, Direction.NORTH));
        SHAPE = shape;
    }


    /**
     * Create block state definition
     * @param builder The definition builder.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }


    /**
     * This method allows to rotate block opposite to player.
     * @param context The placement context.
     * @return The new block state.
     */
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        return Objects.requireNonNull(super.getStateForPlacement(context)).
                setValue(FACING, context.getHorizontalDirection().getOpposite());
    }


    /**
     * This method returns the shape of current table.
     * @param state The block state.
     * @param level The level where block is located.
     * @param pos The position of the block.
     * @param context The collision content.
     * @return The VoxelShape of current table.
     */
    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state,
                               @NotNull BlockGetter level,
                               @NotNull BlockPos pos,
                               @NotNull CollisionContext context)
    {
        return SHAPE;
    }


    /**
     * The interaction that happens when player click on block.
     * @param state The block state.
     * @param level The level where block is located.
     * @param pos The position of the block.
     * @param player The player who clicks on block.
     * @param hand The hand with which player clicks on block.
     * @param hit The Hit result.
     * @return Interaction result outcome.
     */
    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state,
                                 Level level,
                                 @NotNull BlockPos pos,
                                 @NotNull Player player,
                                 @NotNull InteractionHand hand,
                                 @NotNull BlockHitResult hit)
    {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer)
        {
            BlockEntity tile = level.getBlockEntity(pos);

            if (tile instanceof CrateCrackerTileEntity table)
            {
                ItemStack stack = serverPlayer.getMainHandItem();

                // This is the same logic as vault doll placement on ground.

                if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof VaultCrateBlock crateBlock &&
                        ServerVaults.get(serverPlayer.getLevel()).isEmpty() &&
                        !VHSmpUtil.isArenaWorld(player) &&
                        table.playerCanInsertCrate())
                {
                    if (!table.getCrate().isEmpty())
                    {
                        Containers.dropItemStack(level,
                                pos.getX(),
                                pos.getY(),
                                pos.getZ(),
                                table.getInventory().getStackInSlot(0));
                        table.getInventory().setStackInSlot(0, ItemStack.EMPTY);
                    }

                    ItemStack copy = serverPlayer.getMainHandItem().copy();
                    copy.setCount(1);
                    serverPlayer.getMainHandItem().shrink(1);

                    table.updateCrate(copy);

                    return InteractionResult.SUCCESS;
                }
                else
                {
                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.SUCCESS;
    }


    /**
     * This method indicates if entities can path find over this block.
     * @param state The block state.
     * @param level Level where block is located.
     * @param pos Position of the block.
     * @param type The path finder type.
     * @return {@code false} always
     */
    @Override
    public boolean isPathfindable(@NotNull BlockState state,
                                  @NotNull BlockGetter level,
                                  @NotNull BlockPos pos,
                                  @NotNull PathComputationType type)
    {
        return false;
    }


    /**
     * This method drops all items from container when block is broken.
     * @param state The BlockState.
     * @param level Level where block is broken.
     * @param pos Position of broken block.
     * @param newState New block state.
     * @param isMoving Boolean if block is moving.
     */
    @Override
    public void onRemove(BlockState state,
                         @NotNull Level level,
                         @NotNull BlockPos pos,
                         BlockState newState,
                         boolean isMoving)
    {
        if (!state.is(newState.getBlock()))
        {
            BlockEntity tile = level.getBlockEntity(pos);

            if (tile instanceof CrateCrackerTileEntity table)
            {
                Containers.dropItemStack(level,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        table.getInventory().getStackInSlot(0));
                table.getInventory().setStackInSlot(0, ItemStack.EMPTY);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }


    /**
     * This method creates a new block entity.
     * @param pos Position for block.
     * @param state Block state.
     * @return New block entity.
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return ModBlocks.CRATE_CRACKER_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(pos, state);
    }


    /**
     * This method manages entity ticking for this block.
     * @param level The level where block is ticking
     * @param state The block state.
     * @param type The block type.
     * @return Ticked block entity.
     * @param <T> Block entity type.
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,
                                                                  @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type)
    {
        return createTickerHelper(type,
                ModBlocks.CRATE_CRACKER_TILE_ENTITY_BLOCK_ENTITY_TYPE,
                (world, pos, blockState, tileEntity) -> tileEntity.tick());
    }


    /**
     * This method creates requested tick block.
     */
    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type,
            BlockEntityType<E> expectedType,
            BlockEntityTicker<? super E> ticker)
    {
        return type == expectedType ? (BlockEntityTicker<A>) ticker : null;
    }



    /**
     * The constant SHAPE.
     */
    private final VoxelShape SHAPE;
}
