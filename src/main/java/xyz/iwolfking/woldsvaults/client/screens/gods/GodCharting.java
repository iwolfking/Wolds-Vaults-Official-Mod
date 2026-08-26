package xyz.iwolfking.woldsvaults.client.screens.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;

/**
 * Whether a god's constellation is visible on the gods tab. It stays uncharted until the player holds the
 * reputation {@code god_levels.json} asks for, which the alignment sync mirrors client side.
 */
public final class GodCharting {
    private GodCharting() {
    }

    public static int reputationRequired() {
        return GodLevels.chartingReputation();
    }

    public static boolean isCharted(VaultGod god) {
        return ClientGodAlignmentData.getReputation(god) >= reputationRequired();
    }
}
