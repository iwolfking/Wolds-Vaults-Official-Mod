package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.vault.Vault;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-vault registry of the active greed medallion, mirroring the in-memory pattern of
 * {@code VaultClockRate}: populated when a vault is built from a medallion-bearing crystal,
 * consulted by every medallion effect hook and the assassin spawner, and released when the vault
 * ends. Nothing here persists; a vault that outlives a server restart simply loses its medallion
 * effects, which matches how the clock-rate carrier behaves.
 */
public final class GreedMedallionVaultState {
    private static final Map<UUID, GreedMedallionTier> ACTIVE = new ConcurrentHashMap<>();

    private GreedMedallionVaultState() {
    }

    public static void set(UUID vaultId, GreedMedallionTier tier) {
        if (vaultId != null && tier != null) {
            ACTIVE.put(vaultId, tier);
        }
    }

    public static Optional<GreedMedallionTier> get(UUID vaultId) {
        return vaultId == null ? Optional.empty() : Optional.ofNullable(ACTIVE.get(vaultId));
    }

    public static Optional<GreedMedallionTier> get(Vault vault) {
        if (vault == null || !vault.has(Vault.ID)) {
            return Optional.empty();
        }
        return get(vault.get(Vault.ID));
    }

    public static void release(UUID vaultId) {
        if (vaultId != null) {
            ACTIVE.remove(vaultId);
        }
    }

    public static void clearAll() {
        ACTIVE.clear();
    }
}
