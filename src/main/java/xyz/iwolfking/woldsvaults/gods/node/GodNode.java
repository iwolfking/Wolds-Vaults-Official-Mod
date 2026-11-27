package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;

import javax.annotation.Nullable;

/**
 * One placement in a god's constellation. {@code id} is unique per tree position and is what the
 * purchase ledger stores; {@code effect} names the shared mechanic many placements can feed.
 */
public record GodNode(String id, VaultGod god, String name, GodNodeType type, @Nullable String effect, int cost,
                      boolean enabled) {
    /** The key this node's god points are banked under - its effect, or its own id for roots. */
    public String ledgerKey() {
        return this.effect != null ? this.effect : this.id;
    }

    public boolean isRoot() {
        return this.type == GodNodeType.ROOT;
    }
}
