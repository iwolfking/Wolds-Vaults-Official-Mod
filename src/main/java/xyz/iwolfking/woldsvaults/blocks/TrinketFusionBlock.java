package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.blocks.tiles.TrinketFusionForgeTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nullable;

public class TrinketFusionBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public TrinketFusionBlock(Properties props) {
        super(props.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrinketFusionForgeTileEntity(pos, state);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide ? null :
                createTickerHelper(type,
                        ModBlocks.TRINKET_FUSION_FORGE_TILE_ENTITY_BLOCK_ENTITY_TYPE,
                        TrinketFusionForgeTileEntity::tick);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrinketFusionForgeTileEntity tile)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()) {
            if(!held.getItem().equals(Items.BUCKET)) {
                return InteractionResult.PASS;
            }

            held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
                tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(tank -> {

                    FluidActionResult result = FluidUtil.tryEmptyContainer(
                            held,
                            tank,
                            1000,
                            player,
                            true
                    );

                    if (result.isSuccess()) {
                        player.setItemInHand(hand, result.getResult());
                    }
                    FluidActionResult fill = FluidUtil.tryFillContainer(
                            held,
                            tank,
                            1000,
                            player,
                            true
                    );

                    if (fill.isSuccess()) {
                        player.setItemInHand(hand, fill.getResult());
                    }
                });
            });

            return InteractionResult.CONSUME;
        }

        if (be instanceof TrinketFusionForgeTileEntity fusion) {
            NetworkHooks.openGui((ServerPlayer) player, fusion, pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {

        if (state.getBlock() != newState.getBlock()) {

            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof TrinketFusionForgeTileEntity tile) {
                tile.dropContents();
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}