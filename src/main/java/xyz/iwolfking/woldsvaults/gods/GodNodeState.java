package xyz.iwolfking.woldsvaults.gods;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The one scratch store for live god node state, keyed by {@code (playerId, effectId)} for
 * per-player state and by {@code (vaultId, effectId)} for state that belongs to a running vault.
 *
 * <p>Handler classes hold no static per-player maps of their own. That rule is what makes two
 * shipped bug classes unrepresentable: state that has no logout path and leaks for the process
 * lifetime, and a {@code VAULT_END} teardown that clears a whole static map, so any party
 * finishing a vault zeroes every other party's live state server-wide. Vault-scoped state is
 * cleared by vault id, never wholesale.
 *
 * <p>Teardown is wired once, in {@link xyz.iwolfking.woldsvaults.gods.node.GodNodeLifecycle} and
 * {@link xyz.iwolfking.woldsvaults.gods.event.GodEventHandlers}: logout and vault-listener leave
 * clear the player, vault end clears the vault.
 */
public final class GodNodeState {
    private static final Map<UUID, Map<String, Object>> PLAYERS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Object>> VAULTS = new ConcurrentHashMap<>();

    private GodNodeState() {
    }

    /**
     * The player's scratch for one effect, created by {@code factory} on first use. Every caller
     * for one effect id must agree on the scratch type; the cast is unchecked, so disagreeing
     * fails fast at the call site rather than silently replacing the state.
     */
    public static <T> T get(UUID playerId, String effectId, Supplier<T> factory) {
        return get(PLAYERS, playerId, effectId, factory);
    }

    /** The player's scratch for one effect if it exists, without creating it. */
    public static <T> Optional<T> peek(UUID playerId, String effectId) {
        return peek(PLAYERS, playerId, effectId);
    }

    public static void put(UUID playerId, String effectId, Object scratch) {
        PLAYERS.computeIfAbsent(playerId, id -> new ConcurrentHashMap<>()).put(effectId, scratch);
    }

    public static void clear(UUID playerId) {
        PLAYERS.remove(playerId);
    }

    public static void clear(UUID playerId, String effectId) {
        Map<String, Object> scratch = PLAYERS.get(playerId);
        if (scratch != null) {
            scratch.remove(effectId);
        }
    }

    /** The running vault's scratch for one effect, created by {@code factory} on first use. */
    public static <T> T getVault(UUID vaultId, String effectId, Supplier<T> factory) {
        return get(VAULTS, vaultId, effectId, factory);
    }

    public static <T> Optional<T> peekVault(UUID vaultId, String effectId) {
        return peek(VAULTS, vaultId, effectId);
    }

    public static void putVault(UUID vaultId, String effectId, Object scratch) {
        VAULTS.computeIfAbsent(vaultId, id -> new ConcurrentHashMap<>()).put(effectId, scratch);
    }

    public static void clearVault(UUID vaultId) {
        VAULTS.remove(vaultId);
    }

    /** Drops everything, for a full server shutdown or a debug reset. */
    public static void clearAll() {
        PLAYERS.clear();
        VAULTS.clear();
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Map<UUID, Map<String, Object>> store, UUID owner, String effectId, Supplier<T> factory) {
        return (T) store.computeIfAbsent(owner, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(effectId, id -> factory.get());
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> peek(Map<UUID, Map<String, Object>> store, UUID owner, String effectId) {
        Map<String, Object> scratch = store.get(owner);
        return scratch == null ? Optional.empty() : Optional.ofNullable((T) scratch.get(effectId));
    }
}
