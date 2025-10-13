package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.base.InventoryRetainerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.blocks.tiles.ModBoxWorkstationTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.WeavingStationTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class WeavingStationBlock extends Block implements EntityBlock, InventoryRetainerBlock<ModBoxWorkstationTileEntity> {
    public static final DirectionProperty FACING;
    public static final VoxelShape shape;

    public WeavingStationBlock() {
        super(Properties.of(Material.STONE).strength(1.5F, 6.0F).noOcclusion());
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        this.addInventoryTooltip(stack, tooltip, WeavingStationTileEntity::addInventoryTooltip);
     }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    static {
        VoxelShape[] shapes = new VoxelShape[5];

        shapes[0] = Block.box(0, 12, 0, 16, 16, 16);
        shapes[1] = Block.box(0, 0, 0, 4, 12, 4);
        shapes[2] = Block.box(12, 0, 0, 16, 12, 4);
        shapes[3] = Block.box(0, 0, 12, 4, 12, 16);
        shapes[4] = Block.box(12, 0, 12, 16, 12, 16);

        shape = Shapes.or(shapes[0], shapes[1], shapes[2], shapes[3], shapes[4]);
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return shape;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            if (world.getBlockEntity(pos) instanceof WeavingStationTileEntity entity) {
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openGui(serverPlayer, entity, buffer -> buffer.writeBlockPos(pos));
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.SUCCESS;
                }
            } else {
                return InteractionResult.SUCCESS;
            }
        }
    }

    @Override
    public void onRemove(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
        this.onInventoryBlockDestroy(world, pos, state, newState);
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.onInventoryBlockPlace(level, pos, stack);
    }

    @Override
    public boolean useShapeForLightOcclusion(@Nonnull BlockState state) {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nonnull
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Nonnull
    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @ParametersAreNonnullByDefault
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlocks.WEAVING_STATION_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(pos, state);
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
    }
}
