package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.item.gear.VaultCharmItem;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which god tree is active for a player: the god of the charm in the curios {@code charm} slot, empty
 * for a broken charm. Cached per player and per logical side until invalidated.
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

    /** Whether a curio slot going from {@code from} to {@code to} can have changed the active god. */
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
