package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;

/** Perserverence: a longer bleed-out timer for the downed player themselves. */
public final class VelaraPerserverence {
    private VelaraPerserverence() {
    }

    public static int adjustBleedOutTicks(ServerPlayer player, int bleedOutTicks) {
        if (!VelaraNodes.isActive(player, VelaraNodes.PERSERVERENCE)) {
            return bleedOutTicks;
        }
        return Math.round(bleedOutTicks * (1.0F + VelaraValues.perserverenceTimerBonus()));
    }
}
