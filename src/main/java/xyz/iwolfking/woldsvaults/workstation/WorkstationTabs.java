package xyz.iwolfking.woldsvaults.workstation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkstationTabs {

    public static final TagKey<Block> TAG = TagKey.create(Registry.BLOCK_REGISTRY, ResourceLocation.fromNamespaceAndPath("woldsvaults", "workstation_blocks"));
    public static final int NEARBY_RANGE = 8;
    private static final Set<Block> EXTRA = new HashSet<>();

    public static void register(Block block) {
        if (block != null) {
            EXTRA.add(block);
        }
    }

    public static boolean isWorkstation(Block block) {
        if (block == null) {
            return false;
        }
        if (EXTRA.contains(block)) {
            return true;
        }
        if (!WorkstationGroup.groupsOf(block).isEmpty()) {
            return true;
        }
        try {
            return block.defaultBlockState().is(TAG);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isWorkstation(BlockState state) {
        return state != null && isWorkstation(state.getBlock());
    }

    private static boolean sharesGroup(Block block, BlockPos pos, BlockPos start, Set<TagKey<Block>> startTags, boolean grouped) {
        if (pos.equals(start)) {
            return true;
        }
        if (!grouped) {
            return false;
        }
        List<WorkstationGroup> blockGroups = WorkstationGroup.groupsOf(block);
        if (blockGroups.isEmpty()) {
            return false;
        }
        for (WorkstationGroup g : blockGroups) {
            if (startTags.contains(g.tag)) {
                return true;
            }
        }
        return false;
    }

    public static List<BlockPos> detect(Level level, BlockPos start) {
        List<BlockPos> result = new ArrayList<>();
        if (level == null || start == null) {
            return result;
        }
        Block startBlock = level.getBlockState(start).getBlock();
        Set<TagKey<Block>> startTags = new HashSet<>();
        for (WorkstationGroup g : WorkstationGroup.groupsOf(startBlock)) {
            startTags.add(g.tag);
        }
        boolean grouped = !startTags.isEmpty();

        for (int dx = -NEARBY_RANGE; dx <= NEARBY_RANGE; dx++) {
            for (int dy = -NEARBY_RANGE; dy <= NEARBY_RANGE; dy++) {
                for (int dz = -NEARBY_RANGE; dz <= NEARBY_RANGE; dz++) {
                    BlockPos pos = start.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (!isWorkstation(state)) {
                        continue;
                    }
                    if (!sharesGroup(state.getBlock(), pos, start, startTags, grouped)) {
                        continue;
                    }
                    result.add(pos.immutable());
                }
            }
        }

        final BlockPos center = start;
        result.sort(Comparator.comparingDouble((BlockPos p) -> p.distSqr(center))
                .thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
        if (result.size() > 12) {
            result = new ArrayList<>(result.subList(0, 12));
        }
        return result;
    }

    static {
        WoldLogger.debug(WorkstationGroups.CRYSTAL.name);
    }
}