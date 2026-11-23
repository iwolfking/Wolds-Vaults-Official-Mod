package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

// Where a loading zone with the same link name sends players. It keeps the way its placer faced in 16 steps, like a sign,
// so it turns with the room when a vault rotates or mirrors it.
public class LoadingZoneArrivalBlock extends LoadingZoneMarkerBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    public LoadingZoneArrivalBlock() {
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    public static float yaw(BlockState state) {
        return state.getValue(ROTATION) * 22.5F;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ROTATION, Mth.floor(context.getRotation() * 16.0F / 360.0F + 0.5) & 15);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }
}
