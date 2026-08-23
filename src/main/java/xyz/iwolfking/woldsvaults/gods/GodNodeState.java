package xyz.iwolfking.woldsvaults.gods;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The one scratch store for live god node state, keyed by {@code (playerId, effectId)} and by
 * {@code (vaultId, effectId)}. Both are transient - wiped on logout and vault-listener leave, and at
 * vault end - so state that must outlive a vault or a relog belongs in {@link #persistent}.
 */
public final class GodNodeState {
    private static final String PERSISTENT_ROOT = "woldsvaults_god_nodes";

    private static final Map<UUID, Map<String, Object>> PLAYERS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Object>> VAULTS = new ConcurrentHashMap<>();

    private GodNodeState() {
    }

    /** The player's scratch for one effect, created by {@code factory} on first use; the cast is unchecked. */
    public static <T> T get(UUID playerId, String effectId, Supplier<T> factory) {
        return get(PLAYERS, playerId, effectId, factory);
    }

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

    /** Drops one effect's slice of a vault's scratch, leaving every other effect's alone. */
    public static void clearVault(UUID vaultId, String effectId) {
        Map<String, Object> scratch = VAULTS.get(vaultId);
        if (scratch != null) {
            scratch.remove(effectId);
        }
    }

    public static void clearAll() {
        PLAYERS.clear();
        VAULTS.clear();
    }

    /**
     * The player's durable scratch for one effect: a compound under the persisted NBT, surviving logout,
     * death and restart, cleared only by {@link #clearPersistent}. Stamp wall-clock time, not game time.
     */
    public static CompoundTag persistent(ServerPlayer player, String effectId) {
        CompoundTag persisted = child(player.getPersistentData(), Player.PERSISTED_NBT_TAG);
        return child(child(persisted, PERSISTENT_ROOT), effectId);
    }

    public static boolean hasPersistent(ServerPlayer player, String effectId) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            return false;
        }
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        return persisted.contains(PERSISTENT_ROOT, Tag.TAG_COMPOUND)
                && persisted.getCompound(PERSISTENT_ROOT).contains(effectId, Tag.TAG_COMPOUND);
    }

    public static void clearPersistent(ServerPlayer player, String effectId) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(PERSISTENT_ROOT, Tag.TAG_COMPOUND)) {
            persisted.getCompound(PERSISTENT_ROOT).remove(effectId);
        }
    }

    private static CompoundTag child(CompoundTag parent, String key) {
        if (!parent.contains(key, Tag.TAG_COMPOUND)) {
            parent.put(key, new CompoundTag());
        }
        return parent.getCompound(key);
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
