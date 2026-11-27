package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.mana.Mana;
import net.minecraft.world.entity.player.Player;

/**
 * The "regeneration covers the drain" rule for mana-sustained toggles.
 */
public final class ManaSustain {
    private ManaSustain() {
    }

    /**
     * True when one tick of the player's mana regeneration pays for one tick of the given drain, so a toggle
     * draining {@code costPerTick} can stay on even when the bar itself cannot pay this tick.
     */
    public static boolean shouldStayOn(Player player, float costPerTick) {
        return Mana.getRegenPerSecond(player) / 20.0F >= costPerTick;
    }
}
