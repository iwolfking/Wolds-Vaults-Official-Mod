package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.item.gear.VaultCharmItem;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves which god tree is active for a player: the god of the vault god charm equipped in the
 * curios {@code charm} slot. A charm with no uses left is already filtered out by
 * {@link VaultCharmItem#getCharm}, so a broken charm resolves to empty and disables its tree.
 *
 * <p>Resolution walks the curios handler and reads gear NBT, so results are cached per player and
 * invalidated on equipment and dimension changes by {@link xyz.iwolfking.woldsvaults.gods.event.GodEventHandlers}.
 * Charms cannot be swapped inside a vault by design; if one ever is, the cache is simply re-filled
 * on the next lookup.
 */
public final class ActiveGodResolver {
    private static final Map<UUID, Optional<VaultGod>> CACHE = new ConcurrentHashMap<>();

    private ActiveGodResolver() {
    }

    public static Optional<VaultGod> getActiveGod(Player player) {
        return CACHE.computeIfAbsent(player.getUUID(), id -> resolve(player));
    }

    public static boolean isActive(Player player, VaultGod god) {
        return getActiveGod(player).filter(active -> active == god).isPresent();
    }

    public static void invalidate(Player player) {
        CACHE.remove(player.getUUID());
    }

    public static void invalidate(UUID playerId) {
        CACHE.remove(playerId);
    }

    public static void invalidateAll() {
        CACHE.clear();
    }

    private static Optional<VaultGod> resolve(Player player) {
        return VaultCharmItem.getCharm(player).flatMap(VaultCharmItem::getGod);
    }
}
