package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Everything a handler is allowed to know about one player's hold on one effect: who, which god,
 * how many points, the effect's per-point table, the gate scale and the player's piety with that
 * god. Handlers read live state through {@code player} and never cache a value - contexts are
 * built per query so a node whose value depends on current health, stacks or another stat
 * recomputes inside a vault without a relog.
 *
 * <p>{@code scale} is how foreign-tree carryover stops being re-implemented per god: a stat node
 * seen from another tree arrives here already scaled, so a handler never asks which tree it is
 * on.
 */
public record GodNodeContext(ServerPlayer player, VaultGod god, String effectId, int points,
                             float[] values, float scale, int piety) {
    public UUID playerId() {
        return this.player.getUUID();
    }

    public boolean isActive() {
        return this.points > 0;
    }

    /**
     * The scaled table value for the points held, clamped to the table's last entry. This is the
     * value a handler applies; {@link #rawValue()} is the unscaled reading for a handler that
     * must do its own scaling.
     */
    public float value() {
        return this.rawValue() * this.scale;
    }

    /** The scaled table value at an explicit index, for effects whose ranks are not point counts. */
    public float value(int index) {
        return this.rawEntry(index) * this.scale;
    }

    public float rawValue() {
        return this.rawEntry(this.points - 1);
    }

    private float rawEntry(int index) {
        if (this.values == null || this.values.length == 0 || index < 0) {
            return 0.0F;
        }
        return this.values[Math.min(index, this.values.length - 1)];
    }
}
