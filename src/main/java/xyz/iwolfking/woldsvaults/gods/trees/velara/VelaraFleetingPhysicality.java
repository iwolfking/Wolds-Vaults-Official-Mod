package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

/**
 * Fleeting Physicality's 30 second cycle: 10 seconds of full immunity followed by 20 seconds of
 * tripled damage taken.
 *
 * <p>The cycle is continuous rather than triggered - the sheet gives no activation - and its phase
 * is anchored per player at the moment the node first becomes live, so two Velara players do not
 * share a window just because they logged in together. The anchor lives in the shared
 * {@link GodNodeState} scratch and is dropped by the same logout and vault-leave teardown as every
 * other node's state.
 */
public final class VelaraFleetingPhysicality {
    private VelaraFleetingPhysicality() {
    }

    public static boolean isImmune(ServerPlayer player) {
        return phase(player) < VelaraValues.fleetingImmuneTicks();
    }

    public static boolean isVulnerable(ServerPlayer player) {
        return phase(player) >= VelaraValues.fleetingImmuneTicks();
    }

    private static long phase(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0L;
        }
        long now = server.getTickCount();
        Long anchor = GodNodeState.<Long>peek(player.getUUID(), VelaraNodes.FLEETING_PHYSICALITY).orElse(null);
        if (anchor == null || now - anchor < 0L) {
            GodNodeState.put(player.getUUID(), VelaraNodes.FLEETING_PHYSICALITY, now);
            return 0L;
        }
        return (now - anchor) % VelaraValues.fleetingCycleTicks();
    }
}
