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
 *
 * <p>The cache is partitioned by logical side. On an integrated server both sides share the JVM
 * and the player UUID, and the tree screens resolve on the client, so one map keyed by UUID would
 * let a client lookup answer a server question. Invalidation drops both sides, because a dropped
 * entry only costs a re-resolve.
 */
public final class ActiveGodResolver {
    private static final Map<UUID, Optional<VaultGod>> SERVER = new ConcurrentHashMap<>();
    private static final Map<UUID, Optional<VaultGod>> CLIENT = new ConcurrentHashMap<>();

    private ActiveGodResolver() {
    }

    public static Optional<VaultGod> getActiveGod(Player player) {
        return cacheFor(player).computeIfAbsent(player.getUUID(), id -> resolve(player));
    }

    public static boolean isActive(Player player, VaultGod god) {
        return getActiveGod(player).filter(active -> active == god).isPresent();
    }

    /**
     * Whether a curio slot going from {@code from} to {@code to} can have changed the active god.
     * Curios fires its change event on any NBT delta, and a charm's NBT moves every second while a
     * blessing counts down and every ten ticks while a mythic charm rescales, so the listeners that
     * re-resolve the god, rebuild the snapshot, reconcile the ticker and re-sync the alignment on a
     * swap ask this first: same item resolving to the same god is not a swap.
     */
    public static boolean mayChangeActiveGod(net.minecraft.world.item.ItemStack from, net.minecraft.world.item.ItemStack to) {
        if (from.getItem() != to.getItem()) {
            return true;
        }
        return !VaultCharmItem.getGod(from).equals(VaultCharmItem.getGod(to));
    }

    public static void invalidate(Player player) {
        invalidate(player.getUUID());
    }

    public static void invalidate(UUID playerId) {
        SERVER.remove(playerId);
        CLIENT.remove(playerId);
    }

    public static void invalidateAll() {
        SERVER.clear();
        CLIENT.clear();
    }

    private static Map<UUID, Optional<VaultGod>> cacheFor(Player player) {
        return player.level != null && player.level.isClientSide() ? CLIENT : SERVER;
    }

    private static Optional<VaultGod> resolve(Player player) {
        return VaultCharmItem.getCharm(player).flatMap(VaultCharmItem::getGod);
    }
}
