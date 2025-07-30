package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.init.ModItems;
import iskallia.vault.util.VHSmpUtil;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import xyz.iwolfking.woldsvaults.blocks.tiles.DollDismantlingTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.Objects;

/**
 * The Doll Dissecting table block.
 * Absolutely full credit to BONNe for the original code, this is a port out of the abandoned More Vault Tables Mod that was never released to Curseforge.
 * Original licensing is GPLv3.
 */
public class DollDismantlingBlock extends HorizontalDirectionalBlock implements EntityBlock
{
    /**
     * The shape of doll dismantling table.
     */
    public static VoxelShape DOLL_DISMANTLING_SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0),
            Block.box(4.0, 8.0, 4.0, 12.0, 10.0, 12.0),
            Block.box(3.0, 10.0, 3.0, 13.0, 20.0, 13.0),
            Block.box(2.0, 20.0, 2.0, 14.0, 21.0, 14.0),
            Block.box(3.0, 21.0, 3.0, 13.0, 23.0, 13.0),
            Block.box(6.0, 23.0, 6.0, 10.0, 24.0, 10.0));
    /**
     * Instantiates a new Doll Dissecting table block.
     */
    public DollDismantlingBlock(Properties properties, VoxelShape shape)
    {
        super(properties);
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

            if (tile instanceof DollDismantlingTileEntity table)
            {
                ItemStack stack = serverPlayer.getMainHandItem();

                // This is the same logic as vault doll placement on ground.

                if (stack.is(ModItems.VAULT_DOLL) &&
                        ServerVaults.get(serverPlayer.getLevel()).isEmpty() &&
                        !VHSmpUtil.isArenaWorld(player) &&
                        stack.getOrCreateTag().contains("vaultUUID") &&
                        table.playerCanInsertDoll(stack, player))
                {
                    if (!table.getDoll().isEmpty())
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

                    table.updateDoll(copy, serverPlayer);

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

            if (tile instanceof DollDismantlingTileEntity table)
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
        return ModBlocks.DOLL_DISMANTLING_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(pos, state);
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
                ModBlocks.DOLL_DISMANTLING_TILE_ENTITY_BLOCK_ENTITY_TYPE,
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
