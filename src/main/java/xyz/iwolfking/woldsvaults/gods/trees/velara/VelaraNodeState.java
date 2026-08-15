package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached per-player view of which Velara nodes are live and at how many points.
 *
 * <p>{@link GodNodeGate} walks curios and world saved data on every call, which is far too
 * expensive for the buses Velara sits on -  {@code PLAYER_STAT} alone fires on every hit and every
 * heal. The whole tree is therefore resolved once per player per second by {@link VelaraTicker}
 * and read from this array afterwards.
 */
public final class VelaraNodeState {
    private static final Map<UUID, int[]> CACHE = new ConcurrentHashMap<>();
    private static final int[] EMPTY = new int[VelaraNode.values().length];

    private VelaraNodeState() {
    }

    public static int points(ServerPlayer player, VelaraNode node) {
        int[] resolved = CACHE.get(player.getUUID());
        if (resolved == null) {
            resolved = resolve(player);
            CACHE.put(player.getUUID(), resolved);
        }
        return resolved[node.ordinal()];
    }

    public static boolean isActive(ServerPlayer player, VelaraNode node) {
        return points(player, node) > 0;
    }

    /** Node lookup for the damage buses, which see a {@link LivingEntity} rather than a player. */
    public static boolean isActive(Entity entity, VelaraNode node) {
        return entity instanceof ServerPlayer player && isActive(player, node);
    }

    public static void refresh(ServerPlayer player) {
        CACHE.put(player.getUUID(), resolve(player));
    }

    public static void invalidate(UUID playerId) {
        CACHE.remove(playerId);
    }

    /** Raw ledger read, ignoring which god is active -  the attribute provider's view of a node. */
    public static int investedPoints(ServerPlayer player, VelaraNode node) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        return GodAlignmentData.get(server).getPointsIn(player.getUUID(), VaultGod.VELARA, node.getId());
    }

    private static int[] resolve(ServerPlayer player) {
        if (player.getServer() == null) {
            return EMPTY;
        }
        int[] resolved = new int[VelaraNode.values().length];
        for (VelaraNode node : VelaraNode.values()) {
            resolved[node.ordinal()] = node.getKind() == VelaraNode.Kind.MINOR
                    ? GodNodeGate.minorPoints(player, VaultGod.VELARA, node.getId())
                    : GodNodeGate.activePoints(player, VaultGod.VELARA, node.getId());
        }
        return resolved;
    }
}
