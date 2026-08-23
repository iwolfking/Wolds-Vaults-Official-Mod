package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;

/**
 * Fleeting Physicality's 30 second cycle: 10 seconds of full immunity followed by 20 seconds of
 * tripled damage taken.
 *
 * <p>The cycle is continuous rather than triggered - the sheet gives no activation - and its phase
 * is the world's own game time modulo the cycle length, shared by everyone in that world. It holds
 * no per-player state at all, and that is the point: an anchor stamped when the node first became
 * live sat in the transient node scratch, which is wiped on logout and on every vault leave, so
 * relogging restarted the cycle at tick zero and handed the player the immune window on demand.
 * Game time only moves forward and is saved with the world, so the windows cannot be rerolled.
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
        return player.getLevel().getGameTime() % VelaraValues.fleetingCycleTicks();
    }
}
