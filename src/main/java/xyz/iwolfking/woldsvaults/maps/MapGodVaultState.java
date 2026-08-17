package xyz.iwolfking.woldsvaults.maps;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The god binding of every running mapped vault, stashed when a map-imprinted crystal configures its
 * vault and read when a player leaves it completed. Mirrors {@code VaultMapTierCache}: in-memory
 * only, so a vault that outlives a server restart pays no god experience — the crystal keeps the
 * binding, only the live registration is lost.
 *
 * <p>The vault's difficulty multiplier is captured here at build time rather than read at award
 * time, because a greed medallion's contribution to it is released when the vault ends and the award
 * must not depend on which teardown listener runs first.</p>
 */
public final class MapGodVaultState {
    private static final Map<UUID, Binding> ACTIVE = new ConcurrentHashMap<>();

    private MapGodVaultState() {
    }

    public static void set(UUID vaultId, VaultGod god, int bonusPercent, double difficultyMultiplier) {
        if (vaultId != null && god != null) {
            ACTIVE.put(vaultId, new Binding(god, bonusPercent, difficultyMultiplier));
        }
    }

    public static Optional<Binding> get(UUID vaultId) {
        return vaultId == null ? Optional.empty() : Optional.ofNullable(ACTIVE.get(vaultId));
    }

    public static Optional<Binding> get(Vault vault) {
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

    public record Binding(VaultGod god, int bonusPercent, double difficultyMultiplier) {
    }
}
