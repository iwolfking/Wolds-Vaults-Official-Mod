package xyz.iwolfking.woldsvaults.blocks;

import com.blakebr0.cucumber.block.BaseTileEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.blocks.tiles.VaultFabricatorTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

/** The 1×1 Vault Fabricator: builds extractor components from a Fabrication Pattern, items, fluid and FE. */
public class VaultFabricatorBlock extends BaseTileEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public VaultFabricatorBlock() {
        super(Material.METAL, SoundType.METAL, 5.0F, 10.0F, true);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VaultFabricatorTileEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VaultFabricatorTileEntity tile) {
            NetworkHooks.openGui((ServerPlayer) player, tile, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof VaultFabricatorTileEntity tile) {
            Containers.dropContents(level, pos, tile.getInventory().getStacks());
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(type, ModBlocks.VAULT_FABRICATOR_TILE_ENTITY_TYPE, VaultFabricatorTileEntity::tick);
    }
}
