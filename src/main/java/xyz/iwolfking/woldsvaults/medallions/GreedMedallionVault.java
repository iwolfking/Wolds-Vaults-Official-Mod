package xyz.iwolfking.woldsvaults.medallions;

/**
 * Implemented on the base mod's {@code Vault} by mixin so a vault saves the rank of the medallion it was opened with
 * (0 = none) with the rest of its data. Read it through {@link GreedMedallionVaultState}.
 */
public interface GreedMedallionVault {
    int woldsvaults$getMedallionRank();

    void woldsvaults$setMedallionRank(int rank);
}
