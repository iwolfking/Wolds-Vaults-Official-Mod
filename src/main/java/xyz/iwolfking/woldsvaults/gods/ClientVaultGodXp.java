package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;

import java.util.Optional;
import java.util.UUID;

/** God experience from the local player's most recent mapped vault, keyed by vault id, expiring in 2 min. */
public final class ClientVaultGodXp {
    public record Award(UUID vaultId, VaultGod god, long amount) {
    }

    private static final long TTL_MILLIS = 120_000L;

    private static Award award;
    private static long expiresAt;

    private ClientVaultGodXp() {
    }

    public static void set(UUID vaultId, VaultGod god, long amount) {
        award = new Award(vaultId, god, amount);
        expiresAt = System.currentTimeMillis() + TTL_MILLIS;
    }

    public static Optional<Award> peek(UUID vaultId) {
        if (award == null || vaultId == null || System.currentTimeMillis() > expiresAt) {
            return Optional.empty();
        }
        if (!award.vaultId().equals(vaultId)) {
            return Optional.empty();
        }
        return Optional.of(award);
    }

    public static void clear() {
        award = null;
        expiresAt = 0L;
    }
}
