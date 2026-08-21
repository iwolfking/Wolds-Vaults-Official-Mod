package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Results of the once-per-second aura scan run by {@link VelaraTicker}.
 *
 * <p>Presence and Sanitation are radius auras that other players emit, so the value a bus needs
 * is a property of the *recipient*, not of the caster. The scan writes those recipient-side
 * values into the shared {@link GodNodeState} scratch and every hot listener reads them back
 * instead of doing its own spatial query.
 *
 * <p>{@link #commit} writes or clears an entry for every online player rather than swapping a
 * whole map, so an aura that stopped covering someone drops on the same pass that stopped it, and
 * logout teardown is the shared one instead of this class owning a map of its own.
 */
public final class VelaraAuras {
    private VelaraAuras() {
    }

    /**
     * Number of *other* players applying Presence to this player. Presence never applies to its
     * own caster but does stack across casters, so this is a count rather than a flag.
     */
    public static int getPresenceStacks(ServerPlayer player) {
        return GodNodeState.<Integer>peek(player.getUUID(), VelaraNodes.PRESENCE).orElse(0);
    }

    /** Whether a Sanitation aura covers this player. Sanitation does not stack, so this is a flag. */
    public static boolean isSanitized(UUID playerId) {
        return GodNodeState.<Boolean>peek(playerId, VelaraNodes.SANITATION).orElse(Boolean.FALSE);
    }

    static void commit(List<ServerPlayer> players, Map<UUID, Integer> presence, Set<UUID> sanitized) {
        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            Integer stacks = presence.get(playerId);
            if (stacks == null || stacks <= 0) {
                GodNodeState.clear(playerId, VelaraNodes.PRESENCE);
            } else {
                GodNodeState.put(playerId, VelaraNodes.PRESENCE, stacks);
            }
            if (sanitized.contains(playerId)) {
                GodNodeState.put(playerId, VelaraNodes.SANITATION, Boolean.TRUE);
            } else {
                GodNodeState.clear(playerId, VelaraNodes.SANITATION);
            }
        }
    }
}
