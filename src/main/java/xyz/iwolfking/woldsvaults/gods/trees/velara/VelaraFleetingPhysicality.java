package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;

/** Fleeting Physicality's cycle, phased on world game time and shared by everyone in that world. */
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
