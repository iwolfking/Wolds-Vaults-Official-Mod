package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;

/**
 * Perserverence: a 50% longer bleed-out timer while downed.
 *
 * <p>The base mod computes the timer once in {@code DownedPlayerManager.enterDownedState} and
 * stores it in a final field, so the only place to change it is at that computation -  see the
 * mixin under {@code mixins/vaulthunters/gods/velara}. Read as the player's own timer, not as a
 * bonus to anyone they revive.
 */
public final class Perserverence {
    private Perserverence() {
    }

    public static int adjustBleedOutTicks(ServerPlayer player, int bleedOutTicks) {
        if (!VelaraNodeState.isActive(player, VelaraNode.PERSERVERENCE)) {
            return bleedOutTicks;
        }
        return Math.round(bleedOutTicks * (1.0F + VelaraValues.PERSERVERENCE_TIMER_BONUS));
    }
}
