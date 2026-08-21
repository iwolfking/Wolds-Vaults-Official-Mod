package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.world.data.DownedPlayerManager;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Per-vault flock manager for Sacrifice.
 *
 * <p>A flock is the set of allies whose damage one Sacrifice user absorbs. The sheet requires that
 * multiple Sacrifice users in the same party split the flock evenly rather than overlapping, and
 * that a Sacrifice user going down hands their flock to the survivors, so the assignment is a
 * partition recomputed from scratch rather than a per-player flag: partitions cannot drift, and
 * "redistribute on death" is just another rebuild.
 *
 * <p>Grouping is by {@code /party}, since a shepherd can only protect party members. Players under
 * Immortal are excluded from every flock - the sheet states Sacrifice cannot affect them.
 *
 * <p>Both halves of the assignment live in the shared {@link GodNodeState} scratch: each protected
 * player's shepherd under their own player scratch, and the roster of protected players under the
 * running vault's scratch. Vault-scoped state is replaced by vault id and never wholesale, so one
 * party finishing a vault cannot clear another party's flock.
 */
public final class VelaraSacrificeFlocks {
    private VelaraSacrificeFlocks() {
    }

    /** The player currently absorbing damage for {@code protectedPlayer}, or null. */
    public static ServerPlayer getShepherd(ServerPlayer protectedPlayer) {
        UUID shepherdId = GodNodeState.<UUID>peek(protectedPlayer.getUUID(), VelaraNodes.SACRIFICE).orElse(null);
        if (shepherdId == null) {
            return null;
        }
        MinecraftServer server = protectedPlayer.getServer();
        if (server == null) {
            return null;
        }
        ServerPlayer shepherd = server.getPlayerList().getPlayer(shepherdId);
        if (shepherd == null || shepherd == protectedPlayer || shepherd.isDeadOrDying()
                || shepherd.getLevel() != protectedPlayer.getLevel()
                || DownedPlayerManager.isPlayerDowned(shepherd)) {
            return null;
        }
        return shepherd;
    }

    public static void rebuildAll(MinecraftServer server) {
        for (Vault vault : ServerVaults.getAll()) {
            rebuild(server, vault);
        }
    }

    /** Rebuilds the partition for the vault a player is in, used when a shepherd drops out. */
    public static void rebuildFor(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        ServerVaults.get(player.level).ifPresent(vault -> rebuild(server, vault));
    }

    public static void rebuild(MinecraftServer server, Vault vault) {
        if (!vault.has(Vault.ID) || !vault.has(Vault.LISTENERS)) {
            return;
        }
        UUID vaultId = vault.get(Vault.ID);
        clearVault(vaultId);

        List<ServerPlayer> roster = new ArrayList<>();
        for (Runner runner : vault.get(Vault.LISTENERS).getAll(Runner.class)) {
            runner.getPlayer().ifPresent(roster::add);
        }
        if (roster.size() < 2) {
            return;
        }

        Map<UUID, List<ServerPlayer>> groups = new LinkedHashMap<>();
        for (ServerPlayer player : roster) {
            groups.computeIfAbsent(VelaraParty.groupKey(player), key -> new ArrayList<>()).add(player);
        }

        Map<UUID, UUID> assigned = new HashMap<>();
        for (List<ServerPlayer> group : groups.values()) {
            partition(group, assigned);
        }
        if (assigned.isEmpty()) {
            return;
        }
        assigned.forEach((protectedId, shepherdId) ->
                GodNodeState.put(protectedId, VelaraNodes.SACRIFICE, shepherdId));
        GodNodeState.putVault(vaultId, VelaraNodes.SACRIFICE, new ArrayList<>(assigned.keySet()));
    }

    private static void partition(List<ServerPlayer> group, Map<UUID, UUID> assigned) {
        List<ServerPlayer> shepherds = new ArrayList<>();
        List<ServerPlayer> flock = new ArrayList<>();
        for (ServerPlayer player : group) {
            if (VelaraNodes.isActive(player, VelaraNodes.SACRIFICE) && !DownedPlayerManager.isPlayerDowned(player)) {
                shepherds.add(player);
            } else if (!VelaraNodes.isActive(player, VelaraNodes.IMMORTAL) && !DownedPlayerManager.isPlayerDowned(player)) {
                flock.add(player);
            }
        }
        if (shepherds.isEmpty() || flock.isEmpty()) {
            return;
        }
        shepherds.sort(Comparator.comparing(ServerPlayer::getUUID));
        flock.sort(Comparator.comparing(ServerPlayer::getUUID));
        for (int i = 0; i < flock.size(); i++) {
            ServerPlayer shepherd = shepherds.get(i % shepherds.size());
            assigned.put(flock.get(i).getUUID(), shepherd.getUUID());
        }
    }

    /**
     * Drops one vault's partition. The roster is replaced with an empty list rather than cleared
     * outright, because the vault scratch is shared with every other node that stores per-vault
     * state and clearing it by vault id alone would take theirs with it.
     */
    public static void clearVault(UUID vaultId) {
        List<UUID> tracked = GodNodeState.<List<UUID>>peekVault(vaultId, VelaraNodes.SACRIFICE).orElse(null);
        if (tracked == null) {
            return;
        }
        tracked.forEach(playerId -> GodNodeState.clear(playerId, VelaraNodes.SACRIFICE));
        GodNodeState.putVault(vaultId, VelaraNodes.SACRIFICE, new ArrayList<UUID>());
    }
}
