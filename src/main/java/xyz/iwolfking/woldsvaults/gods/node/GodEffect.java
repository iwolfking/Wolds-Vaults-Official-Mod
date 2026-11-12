package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;

import javax.annotation.Nullable;

/**
 * One shared mechanic a god tree can grant, bound to a registered handler type. {@code values} is
 * the per-point table; {@code params} holds the handler's own typed configuration.
 */
public record GodEffect(String id, VaultGod god, String handler, float[] values, @Nullable GodEffectParams params) {
    /** The table entry for {@code index}, clamped to the last; a negative index or empty table reads zero. */
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

    /** The typed parameters, throwing {@link GodTreeConfigException} if they are not a {@code type}. */
    public <T extends GodEffectParams> T params(Class<T> type) {
        if (!type.isInstance(this.params)) {
            throw GodTreeConfigException.fail("God effect '" + this.id + "' (handler '" + this.handler
                    + "') has params " + (this.params == null ? "none" : this.params.getClass().getName())
                    + " but its handler expects " + type.getName());
        }
        return type.cast(this.params);
    }
}
