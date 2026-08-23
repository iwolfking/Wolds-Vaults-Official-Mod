package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.init.ModConfigs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Vault tile-group membership tests, memoised per block. */
public class MilestoneGroups {
    private static final Map<Block, Boolean> ORE_CACHE = new ConcurrentHashMap<>();

    private MilestoneGroups() {
    }

    public static boolean isOre(BlockState state) {
        return ORE_CACHE.computeIfAbsent(state.getBlock(), block -> ModConfigs.TILE_GROUPS.isInGroup(
                MilestoneDispatcher.getOreGroup(),
                PartialTile.of(PartialBlockState.of(block.defaultBlockState()), PartialCompoundNbt.empty())));
    }

    public static void invalidate() {
        ORE_CACHE.clear();
    }
}
