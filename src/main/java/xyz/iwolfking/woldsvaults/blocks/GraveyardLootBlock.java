package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.base.LootableBlock;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.util.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.blocks.tiles.GraveyardLootTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GraveyardLootBlock extends LootableBlock {
    public GraveyardLootBlock() {
        super(Properties.copy(Blocks.DEEPSLATE));
    }

    @Nullable
    @Override
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level world, BlockState state, BlockEntityType<A> type) {
        return !world.isClientSide() ? null : BlockHelper.getTicker(type, ModBlocks.GRAVEYARD_LOOT_BLOCK_BLOCK_ENTITY_TYPE, GraveyardLootTileEntity::tick);
    }

    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return ModBlocks.GRAVEYARD_LOOT_BLOCK_BLOCK_ENTITY_TYPE.create(pos, state);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if(!level.isClientSide() && VaultUtils.getVault(level).isPresent()) {
            VaultEventSystem.triggerEvent(WoldsVaults.id("tombstone_0"), pos, (ServerPlayer) player, VaultUtils.getVault(level).get());
            //EnchantedEventsRegistry.BUNFUNGUS_EVENT.triggerEvent(pos, (ServerPlayer) player, VaultUtils.getVault(level).get());
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}
