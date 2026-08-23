package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Everything a handler may know about one player's hold on one effect: who, which god, the points,
 * the per-point table, the gate scale and their piety. Built per query, never cached, and
 * {@code scale} already carries foreign-tree carryover.
 */
public record GodNodeContext(ServerPlayer player, VaultGod god, String effectId, int points,
                             float[] values, float scale, int piety) {
    public UUID playerId() {
        return this.player.getUUID();
    }

    public boolean isActive() {
        return this.points > 0;
    }

    /** The scaled table value for the points held; {@link #rawValue()} is the unscaled reading. */
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
