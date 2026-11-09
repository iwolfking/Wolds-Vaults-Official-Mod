package xyz.iwolfking.woldsvaults.integration.vaultmapper;

import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.proto.CellType;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.HashMap;
import java.util.Map;

/**
 * Teaches VaultMapper about multi-cell rooms. A multi-cell room is discovered once, at its anchor (minimum corner)
 * cell; when VaultMapper records that room, every other cell of the footprint is added as the same room so the map
 * draws one solid block, and only the centre cell keeps the room icon.
 */
public final class VaultMapperMultiCell {
    private static final Map<Long, Long> MEMBER_TO_CENTRE = new HashMap<>();
    private static boolean expanding;

    private VaultMapperMultiCell() {
    }

    public static void expand(VaultCell cell) {
        if (expanding || cell.cellType != CellType.CELLTYPE_ROOM || cell.roomName == null || cell.roomName.isEmpty()) {
            return;
        }
        if (MEMBER_TO_CENTRE.containsKey(key(cell.x, cell.z))) {
            return;
        }
        int span = ModConfigs.MULTI_CELL_ROOMS.spanOfTemplate(ResourceLocation.tryParse(cell.roomName));
        if (span <= 1) {
            return;
        }
        long centre = key(cell.x + span / 2, cell.z + span / 2);
        expanding = true;
        try {
            for (int dx = 0; dx < span; dx++) {
                for (int dz = 0; dz < span; dz++) {
                    MEMBER_TO_CENTRE.put(key(cell.x + dx, cell.z + dz), centre);
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    VaultCell member = new VaultCell(cell.x + dx, cell.z + dz, CellType.CELLTYPE_ROOM, cell.roomType);
                    member.roomName = cell.roomName;
                    member.explored = cell.explored;
                    VaultMap.addOrReplaceCell(member);
                }
            }
        } finally {
            expanding = false;
        }
    }

    public static boolean hidesIcon(VaultCell cell) {
        Long centre = MEMBER_TO_CENTRE.get(key(cell.x, cell.z));
        return centre != null && centre != key(cell.x, cell.z);
    }

    public static void clear() {
        MEMBER_TO_CENTRE.clear();
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }
}
