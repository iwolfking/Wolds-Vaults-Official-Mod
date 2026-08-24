package xyz.iwolfking.woldsvaults.client.screens.gods;

import iskallia.vault.client.data.ClientStatisticsData;
import iskallia.vault.core.vault.influence.VaultGod;

/**
 * Whether a god's constellation is visible on the gods tab. It stays uncharted until the player has
 * maxed base reputation with that god, which the vault statistics packet mirrors client side.
 */
public final class GodCharting {
    public static final int REPUTATION_REQUIRED = 50;

    private GodCharting() {
    }

    public static boolean isCharted(VaultGod god) {
        return ClientStatisticsData.getReputation(god) >= REPUTATION_REQUIRED;
    }
}
