package xyz.iwolfking.woldsvaults.api.core.layout.multicell;

import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.VaultGenerator;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import iskallia.vault.core.world.generator.layout.VaultLayout;
import iskallia.vault.core.world.template.PlacementSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Per-vault registry of multi-cell rooms. Footprints are registered while the vault initialises (objectives,
 * inscriptions, layouts), before any chunk generates, and are re-registered identically on every server start;
 * from then on every layout query for a covered cell answers ROOM and every template lookup resolves to the anchor.
 */
public final class MultiCellRooms {
    public static final int CELL_SIZE = 47;

    private static final Map<UUID, List<MultiCellFootprint>> FOOTPRINTS = new ConcurrentHashMap<>();
    private static final ThreadLocal<int[]> LAYOUT_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private MultiCellRooms() {
    }

    /** The footprint covering this cell, or null. Safe from any thread. */
    public static MultiCellFootprint at(Vault vault, int cellX, int cellZ) {
        if (FOOTPRINTS.isEmpty() || vault == null) {
            return null;
        }
        List<MultiCellFootprint> footprints = FOOTPRINTS.get(vault.get(Vault.ID));
        if (footprints == null) {
            return null;
        }
        for (MultiCellFootprint footprint : footprints) {
            if (footprint.contains(cellX, cellZ)) {
                return footprint;
            }
        }
        return null;
    }

    public static List<MultiCellFootprint> all(Vault vault) {
        List<MultiCellFootprint> footprints = FOOTPRINTS.get(vault.get(Vault.ID));
        return footprints == null ? List.of() : List.copyOf(footprints);
    }

    /**
     * Wraps a layout's own piece-type query. Only the outermost call on a thread sees footprints: layouts ask
     * themselves and their parent class about neighbouring cells to prune tunnels, and those nested answers must
     * stay the layout's raw grid so the fit test and the pruning never feed back into each other.
     */
    public static VaultLayout.PieceType overlay(Vault vault, RegionPos region, Supplier<VaultLayout.PieceType> raw) {
        int[] depth = LAYOUT_DEPTH.get();
        if (depth[0] > 0) {
            return raw.get();
        }
        VaultLayout.PieceType type;
        depth[0]++;
        try {
            type = raw.get();
        } finally {
            depth[0]--;
        }
        return at(vault, region.getX(), region.getZ()) != null ? VaultLayout.PieceType.ROOM : type;
    }

    /** The layout's piece type for this cell ignoring every multi-cell room. */
    public static VaultLayout.PieceType rawType(VaultGridLayout layout, Vault vault, RegionPos region) {
        int[] depth = LAYOUT_DEPTH.get();
        depth[0]++;
        try {
            return layout.getType(vault, region);
        } finally {
            depth[0]--;
        }
    }

    /**
     * Whether a {@code span} x {@code span} room anchored at this cell fits the vault's own layout: room cells at
     * every even offset, the tunnels joining them, empty cells between tunnels, nothing that belongs to the start,
     * and no overlap with a room already registered.
     */
    public static boolean fits(Vault vault, VaultGridLayout layout, int anchorX, int anchorZ, int span) {
        if (span < 3 || span % 2 == 0 || cellSize(vault) != CELL_SIZE) {
            return false;
        }
        for (MultiCellFootprint existing : all(vault)) {
            if (existing.overlaps(anchorX, anchorZ, span)) {
                return false;
            }
        }
        for (int dx = 0; dx < span; dx++) {
            for (int dz = 0; dz < span; dz++) {
                VaultLayout.PieceType expected;
                if (dx % 2 == 0 && dz % 2 == 0) {
                    expected = VaultLayout.PieceType.ROOM;
                } else if (dx % 2 == 1 && dz % 2 == 1) {
                    expected = VaultLayout.PieceType.NONE;
                } else {
                    expected = dx % 2 == 1 ? VaultLayout.PieceType.TUNNEL_X : VaultLayout.PieceType.TUNNEL_Z;
                }
                RegionPos region = RegionPos.of(anchorX + dx, anchorZ + dz, CELL_SIZE, CELL_SIZE);
                if (rawType(layout, vault, region) != expected) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Registers a multi-cell room for this vault. Re-registering the same room (a server restart re-running vault
     * initialisation) returns the existing footprint. Returns empty, with an error log, if the room does not fit.
     * The orientation is a pure function of the vault seed and the anchor.
     */
    public static Optional<MultiCellFootprint> force(Vault vault, VaultGridLayout layout, int anchorX, int anchorZ, int span,
                                                     ResourceLocation pool, Consumer<PlacementSettings> configurator, String source) {
        for (MultiCellFootprint existing : all(vault)) {
            if (existing.anchorX() == anchorX && existing.anchorZ() == anchorZ && existing.span() == span && existing.pool().equals(pool)) {
                return Optional.of(existing);
            }
        }
        if (!fits(vault, layout, anchorX, anchorZ, span)) {
            WoldLogger.error("Multi-cell room {}x{} {} from {} does not fit at cell ({}, {}) in vault {}; it was not placed.",
                    span, span, pool, source, anchorX, anchorZ, vault.get(Vault.ID));
            return Optional.empty();
        }
        long hash = mix(vault.get(Vault.SEED), anchorX, anchorZ);
        Rotation rotation = Rotation.values()[(int) (hash & 3L)];
        Mirror mirror = ((hash >>> 2) & 1L) == 0L ? Mirror.NONE : Mirror.FRONT_BACK;
        MultiCellFootprint footprint = new MultiCellFootprint(anchorX, anchorZ, span, pool, rotation, mirror, configurator, source);
        FOOTPRINTS.computeIfAbsent(vault.get(Vault.ID), id -> new CopyOnWriteArrayList<>()).add(footprint);
        WoldLogger.info("Registered multi-cell room {} in vault {}", footprint, vault.get(Vault.ID));
        return Optional.of(footprint);
    }

    public static void release(UUID vaultId) {
        FOOTPRINTS.remove(vaultId);
    }

    public static int cellSize(Vault vault) {
        VaultGenerator generator = vault.get(Vault.WORLD).get(WorldManager.GENERATOR);
        if (!(generator instanceof GridGenerator grid)) {
            return -1;
        }
        int cellX = grid.get(GridGenerator.CELL_X);
        int cellZ = grid.get(GridGenerator.CELL_Z);
        return cellX == cellZ ? cellX : -1;
    }

    private static long mix(long seed, int x, int z) {
        long h = seed ^ 0x4D554C5449434C4CL;
        h ^= (long) x * 0x9E3779B97F4A7C15L;
        h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return h;
    }
}
