package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.base.LootableBlock;
import iskallia.vault.util.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.tiles.GraveyardLootTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.HellishSandTileEntity;
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
}
