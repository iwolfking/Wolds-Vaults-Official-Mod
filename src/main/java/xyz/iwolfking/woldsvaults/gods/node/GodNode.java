package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;

import javax.annotation.Nullable;

/**
 * One placement in a god's constellation. Node identity is two-layered: {@code id} is unique per
 * tree position and is what the purchase ledger's tree-node set stores, while {@code effect}
 * names the shared mechanic the placement feeds - many placements share one effect, and the
 * effect is the key god points are banked under.
 */
public record GodNode(String id, VaultGod god, GodNodeType type, @Nullable String effect, int cost, boolean enabled) {
    /** The key this node's god points are banked under - its effect, or its own id for roots. */
    public String ledgerKey() {
        return this.effect != null ? this.effect : this.id;
    }

    public boolean isRoot() {
        return this.type == GodNodeType.ROOT;
    }
}
