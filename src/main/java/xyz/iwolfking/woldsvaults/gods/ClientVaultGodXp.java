package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;

import java.util.Optional;
import java.util.UUID;

/**
 * Client-side note of the god experience the local player earned from their most recent mapped
 * vault, keyed by vault id so a stale award can never leak onto another vault's end screen. Fed
 * by {@code ClientboundVaultGodXpMessage} just before the exit screen opens; read by the
 * end-screen mixin when it builds the experience label.
 */
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
}
