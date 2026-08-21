package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.Vault;

/**
 * A node that changes the vault it is entering: vault modifiers, clock rate, loot and objective
 * difficulty. Called once per runner per vault, after the vault exists and before it starts.
 */
public interface VaultContributor extends GodNodeHandler {
    void onVaultStart(GodNodeContext context, Vault vault);
}
