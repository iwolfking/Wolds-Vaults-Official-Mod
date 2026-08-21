package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;

import javax.annotation.Nullable;

/**
 * One shared mechanic a god tree can grant, bound to a registered handler type. {@code values} is
 * the per-point table that replaces the old per-god Java constant blocks; {@code params} holds
 * the handler's own typed configuration.
 */
public record GodEffect(String id, VaultGod god, String handler, float[] values, @Nullable GodEffectParams params) {
    /**
     * The table entry for {@code index}, clamped to the last entry so a table shorter than a
     * player's invested points keeps paying its final value instead of falling off the end.
     * An empty table reads as zero, which is correct for handlers whose whole configuration
     * lives in {@link #params()}.
     */
    public float value(int index) {
        if (this.values == null || this.values.length == 0) {
            return 0.0F;
        }
        if (index < 0) {
            return 0.0F;
        }
        return this.values[Math.min(index, this.values.length - 1)];
    }

    public int rankCount() {
        return this.values == null ? 0 : this.values.length;
    }

    /**
     * The typed parameters of this effect, asserting they are the record the caller's handler
     * declared. A mismatch means the handler registry and the handler implementation disagree,
     * which is a programming error rather than a config error.
     */
    public <T extends GodEffectParams> T params(Class<T> type) {
        if (!type.isInstance(this.params)) {
            throw GodTreeConfigException.fail("God effect '" + this.id + "' (handler '" + this.handler
                    + "') has params " + (this.params == null ? "none" : this.params.getClass().getName())
                    + " but its handler expects " + type.getName());
        }
        return type.cast(this.params);
    }
}
