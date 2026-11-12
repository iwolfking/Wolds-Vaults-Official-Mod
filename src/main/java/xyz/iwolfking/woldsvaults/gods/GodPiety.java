package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.world.entity.player.Player;

/** Piety as the god charm's scaling currency, at {@link #PIETY_PER_UNIT} piety per charm scale unit. */
public final class GodPiety {
    public static final int PIETY_PER_UNIT = 10;

    private GodPiety() {
    }

    /** Total piety with a god - reputation, god level and tree bonuses combined. */
    public static int total(Player player, VaultGod god) {
        return GodAlignmentData.piety(player, god);
    }

    public static int scaleUnits(Player player, VaultGod god) {
        if (god == null) {
            return 0;
        }
        return total(player, god) / PIETY_PER_UNIT;
    }
}
