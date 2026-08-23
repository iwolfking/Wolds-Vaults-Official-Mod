package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.Vault;

/** A node that changes the vault being entered. Called once per runner per vault, before it starts. */
public interface VaultContributor extends GodNodeHandler {
    void onVaultStart(GodNodeContext context, Vault vault);
}
