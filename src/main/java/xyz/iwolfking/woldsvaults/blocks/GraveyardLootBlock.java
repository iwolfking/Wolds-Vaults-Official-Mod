package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.base.LootableBlock;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.util.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.blocks.tiles.GraveyardLootTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GraveyardLootBlock extends LootableBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public GraveyardLootBlock() {
        super(Properties.copy(Blocks.DEEPSLATE).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level world, BlockState state, BlockEntityType<A> type) {
        return !world.isClientSide() ? null : BlockHelper.getTicker(type, ModBlocks.GRAVEYARD_LOOT_BLOCK_BLOCK_ENTITY_TYPE, GraveyardLootTileEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return ModBlocks.GRAVEYARD_LOOT_BLOCK_BLOCK_ENTITY_TYPE.create(pos, state);
    }

    // Make the block face the player when placed
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    // Handle rotation (e.g., structure rotation)
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    // Handle mirroring (e.g., structure mirroring)
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // Add the FACING property to the blockstate definition
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide() && VaultUtils.getVault(level).isPresent()) {
            VaultEventSystem.triggerEvent(WoldsVaults.id("tombstone_event"), pos, (ServerPlayer) player, VaultUtils.getVault(level).get());
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}