package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Results of {@link VelaraTicker}'s aura scan, per recipient. {@link #commit} covers every player. */
public final class VelaraAuras {
    private VelaraAuras() {
    }

    /** Number of other players applying Presence to this player; it never applies to its caster. */
    public static int getPresenceStacks(ServerPlayer player) {
        return GodNodeState.<Integer>peek(player.getUUID(), VelaraNodes.PRESENCE).orElse(0);
    }

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
