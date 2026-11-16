package xyz.iwolfking.woldsvaults.objectives.extraction;

import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Keeps a site room's chunks entity-ticking while a rig stands in it, thumpers are staged there or its wave mobs are
 * still alive, so mobs keep attacking rigs anywhere in the room wherever the players are. Chunk tickets are not saved;
 * they are re-added from the site state after a restart and dropped when the room goes quiet or the vault unloads.
 */
public final class SiteRoomLoader {
    private static final TicketType<ChunkPos> ROOM_TICKET = TicketType.create("woldsvaults_extraction_room", Comparator.comparingLong(ChunkPos::toLong));
    private static final int ROOM_TICKET_DISTANCE = 2;

    private final Map<Integer, List<ChunkPos>> loadedRooms = new HashMap<>();

    public void update(VirtualWorld world, ExtractionSite[] sites, Function<ExtractionSite, AABB> bounds) {
        for (int i = 0; i < sites.length; i++) {
            boolean loaded = this.loadedRooms.containsKey(i);
            boolean staging = !sites[i].isComplete() && sites[i].thumpersPlaced() > 0;
            boolean active = sites[i].isOccupied() || staging || (loaded && hasLiveWaveMobs(world, bounds.apply(sites[i])));
            if (active && !loaded) {
                this.load(world, i, bounds.apply(sites[i]));
            } else if (!active && loaded) {
                this.unload(world, i);
            }
        }
    }

    public void releaseAll(VirtualWorld world) {
        for (Integer siteIndex : new ArrayList<>(this.loadedRooms.keySet())) {
            this.unload(world, siteIndex);
        }
    }

    private static boolean hasLiveWaveMobs(VirtualWorld world, AABB room) {
        return !world.getEntitiesOfClass(Mob.class, room, mob -> mob.isAlive() && mob.getTags().contains(ExtractionSpawnManager.TAG_WAVE)).isEmpty();
    }

    private void load(VirtualWorld world, int siteIndex, AABB bounds) {
        List<ChunkPos> chunks = new ArrayList<>();
        for (int cx = ((int) Math.floor(bounds.minX)) >> 4; cx <= ((int) Math.ceil(bounds.maxX) - 1) >> 4; cx++) {
            for (int cz = ((int) Math.floor(bounds.minZ)) >> 4; cz <= ((int) Math.ceil(bounds.maxZ) - 1) >> 4; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                world.getChunkSource().addRegionTicket(ROOM_TICKET, chunk, ROOM_TICKET_DISTANCE, chunk);
                chunks.add(chunk);
            }
        }
        this.loadedRooms.put(siteIndex, chunks);
        WoldLogger.debug("Extraction site {} is active; holding its {} room chunks loaded.", siteIndex, chunks.size());
    }

    private void unload(VirtualWorld world, int siteIndex) {
        List<ChunkPos> chunks = this.loadedRooms.remove(siteIndex);
        if (chunks == null) {
            return;
        }
        for (ChunkPos chunk : chunks) {
            world.getChunkSource().removeRegionTicket(ROOM_TICKET, chunk, ROOM_TICKET_DISTANCE, chunk);
        }
        WoldLogger.debug("Extraction site {} went quiet; released its {} room chunks.", siteIndex, chunks.size());
    }
}
