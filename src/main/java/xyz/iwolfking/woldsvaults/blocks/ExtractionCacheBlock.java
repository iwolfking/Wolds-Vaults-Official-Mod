package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.base.IAllowZone;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.storage.WorldZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractionCacheTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

public class ExtractionCacheBlock extends Block implements EntityBlock, IAllowZone {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public ExtractionCacheBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(0.6F, 5.0F).sound(SoundType.HONEY_BLOCK).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new ExtractionCacheTileEntity(pos, state);
    }

    @Override
    public boolean isAllowed(WorldZone zone) {
        return true;
    }

    /**
     * Fires only on a real player break: vanilla routes player mining (survival and creative) through
     * this method, while the objective's own {@code removeBlock}/{@code setBlock} calls never do. The
     * objective is told only after the block was actually removed.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (!removed || level.isClientSide()) {
            return removed;
        }
        if (!(level instanceof VirtualWorld world)) {
            WoldLogger.error("Extraction cache at {} was broken by {} outside a vault world ({}); no power cell rolled.", pos, player.getName().getString(), level.dimension().location());
            return removed;
        }
        VaultUtils.getVault(level).flatMap(ExtractionObjective::get).ifPresentOrElse(
                objective -> objective.onCacheBroken(world, pos, player),
                () -> WoldLogger.error("Extraction cache at {} was broken by {} in a vault without an extraction objective; no power cell rolled.", pos, player.getName().getString()));
        return removed;
    }
}
