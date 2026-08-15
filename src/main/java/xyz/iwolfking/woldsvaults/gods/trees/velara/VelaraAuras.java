package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Results of the once-per-second aura scan run by {@link VelaraTicker}.
 *
 * <p>Presence and Sanitation are radius auras that other players emit, so the value a bus needs
 * is a property of the *recipient*, not of the caster. The scan writes those recipient-side
 * values here and every hot listener reads a plain map instead of doing its own spatial query.
 */
public final class VelaraAuras {
    private static volatile Map<UUID, Integer> presenceStacks = Map.of();
    private static volatile Set<UUID> sanitized = Set.of();
    private static final Map<UUID, Integer> DEFENDER_COUNTS = new ConcurrentHashMap<>();

    private VelaraAuras() {
    }

    /**
     * Number of *other* players applying Presence to this player. Presence never applies to its
     * own caster but does stack across casters, so this is a count rather than a flag.
     */
    public static int getPresenceStacks(ServerPlayer player) {
        return presenceStacks.getOrDefault(player.getUUID(), 0);
    }

    /** Whether a Sanitation aura covers this player. Sanitation does not stack, so this is a flag. */
    public static boolean isSanitized(UUID playerId) {
        return sanitized.contains(playerId);
    }

    /** Distinct gods worn by god-charm users in this player's vault party, self included. */
    public static int getDefenderCharmCount(ServerPlayer player) {
        return DEFENDER_COUNTS.getOrDefault(player.getUUID(), 0);
    }

    static void commit(Map<UUID, Integer> newPresence, Set<UUID> newSanitized) {
        presenceStacks = Map.copyOf(newPresence);
        sanitized = Set.copyOf(newSanitized);
    }

    static void setDefenderCharmCount(UUID playerId, int count) {
        if (count <= 0) {
            DEFENDER_COUNTS.remove(playerId);
        } else {
            DEFENDER_COUNTS.put(playerId, count);
        }
    }

    static void clear(UUID playerId) {
        DEFENDER_COUNTS.remove(playerId);
    }
}
