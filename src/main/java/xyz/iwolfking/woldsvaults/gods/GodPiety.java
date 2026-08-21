package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.world.entity.player.Player;

/**
 * Piety as the god charm's scaling currency. Ten piety is worth exactly one point of the old
 * god reputation, so every charm formula keeps its shape - the reputation reads inside the charm
 * item and its roll helper are redirected here, and everything downstream (stored scale units,
 * live rescaling, roll generation) is untouched. The stats screen's own god reputation display
 * is deliberately not part of this: reputation remains its own visible stat.
 */
public final class GodPiety {
    public static final int PIETY_PER_UNIT = 10;

    private GodPiety() {
    }

    /** Total piety with a god - reputation, god level and tree bonuses combined. */
    public static int total(Player player, VaultGod god) {
        return GodAlignmentData.piety(player, god);
    }

    /** Piety expressed in charm scale units, the drop-in replacement for a reputation read. */
    public static int scaleUnits(Player player, VaultGod god) {
        if (god == null) {
            return 0;
        }
        return total(player, god) / PIETY_PER_UNIT;
    }
}
