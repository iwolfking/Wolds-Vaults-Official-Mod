package xyz.iwolfking.woldsvaults.api.core.layout.multicell;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.List;

/**
 * Multi-cell rooms taller than an ordinary cell. The vault's bedrock fill is opened above the normal room box over
 * such a room, and a vault bedrock shell {@link #SHELL_THICKNESS} blocks thick is laid around its sides above the
 * room box and over its top, so the room stays sealed like every other part of the vault.
 */
public final class TallRooms {
    public static final int ROOM_HEIGHT = 47;
    public static final int SHELL_THICKNESS = 3;
    public static final int MAX_HEIGHT = WoldConstants.VAULT_WORLD_HEIGHT - MultiCellFootprint.ROOM_Y - SHELL_THICKNESS;

    private TallRooms() {
    }

    /** The configured height of a multi-cell room's pool, or the ordinary room height when it has none. */
    public static int height(MultiCellFootprint footprint) {
        return ModConfigs.MULTI_CELL_ROOMS == null ? ROOM_HEIGHT : ModConfigs.MULTI_CELL_ROOMS.heightOfPool(footprint.pool());
    }

    /** Whether the bedrock fill must leave this position empty, because a tall room rises through it. */
    public static boolean opensBedrock(Vault vault, BlockPos pos) {
        if (pos.getY() <= WoldConstants.VAULT_ROOM_BOX_TOP) {
            return false;
        }
        int cellX = Math.floorDiv(pos.getX(), MultiCellRooms.CELL_SIZE);
        int cellZ = Math.floorDiv(pos.getZ(), MultiCellRooms.CELL_SIZE);
        MultiCellFootprint footprint = MultiCellRooms.at(vault, cellX, cellZ);
        return footprint != null && pos.getY() <= top(footprint);
    }

    /**
     * Lays the vault bedrock shell of every tall room that reaches into this chunk: a ring {@link #SHELL_THICKNESS}
     * blocks thick outside its sides from just above the room box to the top of the cap, and a cap of the same
     * thickness over the room and the ring. Positions inside any multi-cell room are left alone.
     */
    public static void placeShell(Vault vault, ServerLevelAccessor world, ChunkPos chunk) {
        List<MultiCellFootprint> footprints = MultiCellRooms.all(vault);
        if (footprints.isEmpty()) {
            return;
        }
        BlockState bedrock = ModBlocks.VAULT_BEDROCK.defaultBlockState();
        int chunkMinX = chunk.getMinBlockX();
        int chunkMinZ = chunk.getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (MultiCellFootprint footprint : footprints) {
            if (height(footprint) <= ROOM_HEIGHT || !overlaps(footprint, chunkMinX, chunkMinZ, SHELL_THICKNESS)) {
                continue;
            }
            int roomTop = top(footprint);
            int shellTop = roomTop + SHELL_THICKNESS;
            for (int x = chunkMinX; x < chunkMinX + 16; x++) {
                for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                    boolean inside = inBox(footprint, x, z, 0);
                    if (!inside && (!inBox(footprint, x, z, SHELL_THICKNESS) || insideAnyRoom(footprints, x, z))) {
                        continue;
                    }
                    int fromY = inside ? roomTop + 1 : WoldConstants.VAULT_ROOM_BOX_TOP + 1;
                    for (int y = fromY; y <= shellTop; y++) {
                        world.setBlock(pos.set(x, y, z), bedrock, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static int top(MultiCellFootprint footprint) {
        return MultiCellFootprint.ROOM_Y + height(footprint) - 1;
    }

    private static boolean insideAnyRoom(List<MultiCellFootprint> footprints, int x, int z) {
        for (MultiCellFootprint footprint : footprints) {
            if (inBox(footprint, x, z, 0)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inBox(MultiCellFootprint footprint, int x, int z, int margin) {
        int minX = footprint.anchorX() * MultiCellRooms.CELL_SIZE - margin;
        int minZ = footprint.anchorZ() * MultiCellRooms.CELL_SIZE - margin;
        int size = footprint.span() * MultiCellRooms.CELL_SIZE + 2 * margin;
        return x >= minX && x < minX + size && z >= minZ && z < minZ + size;
    }

    private static boolean overlaps(MultiCellFootprint footprint, int chunkMinX, int chunkMinZ, int margin) {
        int minX = footprint.anchorX() * MultiCellRooms.CELL_SIZE - margin;
        int minZ = footprint.anchorZ() * MultiCellRooms.CELL_SIZE - margin;
        int size = footprint.span() * MultiCellRooms.CELL_SIZE + 2 * margin;
        return chunkMinX < minX + size && minX < chunkMinX + 16 && chunkMinZ < minZ + size && minZ < chunkMinZ + 16;
    }
}
