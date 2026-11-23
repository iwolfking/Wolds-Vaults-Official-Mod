package xyz.iwolfking.woldsvaults.loadingzone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.tiles.LoadingZoneMarkerTileEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every loaded loading zone marker by level, and the zones its posts form. Client and server each keep their own copy.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class LoadingZones {
    private static final int ROOM_SIZE = 47;
    private static final Map<Level, LevelZones> SERVER = new ConcurrentHashMap<>();
    private static final Map<Level, LevelZones> CLIENT = new ConcurrentHashMap<>();

    private LoadingZones() {
    }

    /** Drops an unloading level's markers; a deleted vault world never unloads its markers one by one. */
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof Level level) {
            (level.isClientSide ? CLIENT : SERVER).remove(level);
        }
    }

    public static void add(LoadingZoneMarkerTileEntity marker) {
        LevelZones zones = zones(marker.getLevel());
        zones.markers.put(marker.getBlockPos(), marker);
        zones.dirty = true;
    }

    public static void remove(LoadingZoneMarkerTileEntity marker) {
        LevelZones zones = zones(marker.getLevel());
        if (zones.markers.get(marker.getBlockPos()) == marker) {
            zones.markers.remove(marker.getBlockPos());
            zones.dirty = true;
        }
    }

    public static void changed(Level level) {
        zones(level).dirty = true;
    }

    public static List<LoadingZoneShape> zonesAt(Level level, BlockPos pos) {
        return zones(level).shapes(level).getOrDefault(group(level, pos), List.of());
    }

    public static List<LoadingZoneShape> allZones(Level level) {
        List<LoadingZoneShape> all = new ArrayList<>();
        zones(level).shapes(level).values().forEach(all::addAll);
        return all;
    }

    public static Collection<LoadingZoneMarkerTileEntity> markers(Level level) {
        return zones(level).markers.values();
    }

    public static Optional<LoadingZoneMarkerTileEntity> arrivalFor(Level level, LoadingZoneShape zone) {
        double x = zone.centreX();
        double z = zone.centreZ();
        long group = group(level, new BlockPos(x, zone.minY(), z));
        return zones(level).markers.values().stream()
                .filter(marker -> marker.isArrival() && marker.getLink().equals(zone.link()) && group(level, marker.getBlockPos()) == group)
                .min(Comparator.comparingDouble(marker -> marker.getBlockPos().distToCenterSqr(x, zone.minY(), z)));
    }

    /** A pocket is one structure however big, so its posts pair up anywhere inside it */
    static long group(Level level, BlockPos pos) {
        if (!PocketTerritory.isVaultWorld(level)) {
            return 0L;
        }
        if (PocketTerritory.inTerritory(pos.getX())) {
            return Long.MIN_VALUE + PocketTerritory.slotOf(pos.getX());
        }
        return ChunkPos.asLong(Math.floorDiv(pos.getX(), ROOM_SIZE), Math.floorDiv(pos.getZ(), ROOM_SIZE));
    }

    private static LevelZones zones(Level level) {
        return (level.isClientSide ? CLIENT : SERVER).computeIfAbsent(level, key -> new LevelZones());
    }

    private static final class LevelZones {
        private final Map<BlockPos, LoadingZoneMarkerTileEntity> markers = new HashMap<>();
        private Map<Long, List<LoadingZoneShape>> shapes = Map.of();
        private boolean dirty;

        private Map<Long, List<LoadingZoneShape>> shapes(Level level) {
            if (this.dirty) {
                Map<Long, Map<String, List<LoadingZoneMarkerTileEntity>>> posts = new HashMap<>();
                for (LoadingZoneMarkerTileEntity marker : this.markers.values()) {
                    if (!marker.isArrival() && !marker.getLink().isEmpty()) {
                        posts.computeIfAbsent(group(level, marker.getBlockPos()), key -> new HashMap<>()).computeIfAbsent(marker.getLink(), key -> new ArrayList<>()).add(marker);
                    }
                }
                Map<Long, List<LoadingZoneShape>> built = new HashMap<>();
                posts.forEach((group, links) -> links.forEach((link, markers) -> {
                    if (markers.size() >= 2) {
                        built.computeIfAbsent(group, key -> new ArrayList<>()).add(shape(link, markers));
                    }
                }));
                this.shapes = built;
                this.dirty = false;
            }
            return this.shapes;
        }

        /** Any one post can say where the zone leads; the rest keep the default */
        private static LoadingZoneShape shape(String link, List<LoadingZoneMarkerTileEntity> markers) {
            List<LoadingZoneShape.Corner> corners = new ArrayList<>();
            LoadingZoneShape.Leads leads = LoadingZoneShape.Leads.ARRIVAL;
            String pocket = "";
            for (LoadingZoneMarkerTileEntity marker : markers) {
                BlockPos pos = marker.getBlockPos();
                corners.add(new LoadingZoneShape.Corner(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, marker.getHeight(), marker.getOrder()));
                if (leads == LoadingZoneShape.Leads.ARRIVAL && marker.getLeads() != LoadingZoneShape.Leads.ARRIVAL) {
                    leads = marker.getLeads();
                    pocket = marker.getPocket();
                }
            }
            return LoadingZoneShape.of(link, corners, leads, pocket);
        }
    }
}
