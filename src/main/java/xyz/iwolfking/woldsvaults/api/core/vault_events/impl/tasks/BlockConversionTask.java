package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BlockConversionTask implements VaultEventTask {

    private final Predicate<Block> blockPredicate;

    private final WeightedList<BlockState> replacementBlocks;

    private final int searchRadius;

    public BlockConversionTask(Predicate<Block> blockPredicate, WeightedList<BlockState> replacementBlocks, int searchRadius) {
        this.blockPredicate = blockPredicate;
        this.replacementBlocks = replacementBlocks;
        this.searchRadius = searchRadius;
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        ServerLevel level = player.getLevel();

        List<BlockPos> foundBlocks = new ArrayList<>();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos currentPos = pos.get().offset(x, y, z);
                    BlockState state = level.getBlockState(currentPos);

                    if (blockPredicate.test(state.getBlock())) {
                        foundBlocks.add(currentPos);
                    }
                }
            }
        }

        for(BlockPos foundBlockPos : foundBlocks) {
            BlockState replacementBlock = replacementBlocks.getRandom().orElse(Blocks.AIR.defaultBlockState());

            level.setBlock(foundBlockPos, replacementBlocks.getRandom().orElse(Blocks.AIR.defaultBlockState()), 3);

            if(replacementBlock.getBlock() instanceof VaultChestBlock) {
                if(level.getBlockEntity(foundBlockPos) instanceof VaultChestTileEntity vaultChestTileEntity) {
                    vaultChestTileEntity.setVaultChest(true);
                    vaultChestTileEntity.generateChestLoot(player, true);
                }
            }

            if(replacementBlock.getBlock() instanceof VaultOreBlock) {
                BlockState oreState = level.getBlockState(foundBlockPos);
                oreState.setValue(VaultOreBlock.GENERATED, true);
                oreState.setValue(VaultOreBlock.TYPE, VaultOreBlock.Type.VAULT_STONE);
            }
        }
    }

    public static class Builder {
        private final WeightedList<BlockState> blocks = new WeightedList<>();
        private int searchRadius = 5;

        public Builder replacementBlock(BlockState state, double weight) {
            blocks.add(state, weight);
            return this;
        }

        public Builder searchRadius(int searchRadius) {
            this.searchRadius = searchRadius;
            return this;
        }

        public BlockConversionTask build(Predicate<Block> blockPredicate) {
            if(blocks.isEmpty()) {
                blocks.add(Blocks.AIR.defaultBlockState(), 1.0);
            }

            return new BlockConversionTask(blockPredicate, blocks, searchRadius);
        }
    }
}
