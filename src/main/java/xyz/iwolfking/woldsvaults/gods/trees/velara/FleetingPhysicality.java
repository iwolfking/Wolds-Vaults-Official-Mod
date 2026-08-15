package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fleeting Physicality's 30 second cycle: 10 seconds of full immunity followed by 20 seconds of
 * tripled damage taken.
 *
 * <p>The cycle is continuous rather than triggered -  the sheet gives no activation -  and its phase
 * is anchored per player at the moment the node first becomes live, so two Velara players do not
 * share a window just because they logged in together.
 */
public final class FleetingPhysicality {
    private static final Map<UUID, Long> ANCHORS = new ConcurrentHashMap<>();

    private FleetingPhysicality() {
    }

    public static boolean isImmune(ServerPlayer player) {
        return phase(player) < VelaraValues.FLEETING_IMMUNE_TICKS;
    }

    public static boolean isVulnerable(ServerPlayer player) {
        return phase(player) >= VelaraValues.FLEETING_IMMUNE_TICKS;
    }

    public static void clear(UUID playerId) {
        ANCHORS.remove(playerId);
    }

    private static long phase(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0L;
        }
        long now = server.getTickCount();
        long anchor = ANCHORS.computeIfAbsent(player.getUUID(), id -> now);
        long elapsed = now - anchor;
        if (elapsed < 0L) {
            ANCHORS.put(player.getUUID(), now);
            return 0L;
        }
        return elapsed % VelaraValues.FLEETING_CYCLE_TICKS;
    }
}
