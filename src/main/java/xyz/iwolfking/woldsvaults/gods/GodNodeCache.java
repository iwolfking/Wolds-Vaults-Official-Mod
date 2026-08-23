package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeType;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The one gate cache, for every god. Resolving whether a node is live walks the curios handler
 * and the alignment saved data, which is far too expensive for the buses god nodes sit on - the
 * damage buses alone fire on every hit - so each answer is resolved once and reused until it is
 * invalidated.
 *
 * <p>Cached entries are dropped on charm change, dimension change, login and logout, and by the
 * shared ticker once a second, which is what covers node purchases, refunds and piety changes.
 *
 * <p>The cache is partitioned by logical side. On an integrated server both sides share the JVM
 * and the player UUID, so a single map keyed by UUID lets a client-side lookup answer a
 * server-side question with the wrong tree.
 */
public final class GodNodeCache {
    /** Effective points a player holds in an effect, and the scale its values apply at. */
    public record Gated(int points, float scale) {
        public static final Gated NONE = new Gated(0, 0.0F);

        public boolean isActive() {
            return this.points > 0;
        }
    }

    private static final Map<UUID, Map<String, Gated>> SERVER = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Gated>> CLIENT = new ConcurrentHashMap<>();

    private GodNodeCache() {
    }

    /**
     * The effective points and scale {@code player} holds in {@code effectId} of {@code god},
     * resolved from cache. See {@link GodNodeGate} for the gating rules themselves.
     */
    public static Gated resolve(ServerPlayer player, VaultGod god, String effectId) {
        return cacheFor(player)
                .computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>())
                .computeIfAbsent(key(god, effectId), key -> compute(player, god, effectId));
    }

    /** Drops the player's resolved entries so the next read recomputes them. */
    public static void refresh(ServerPlayer player) {
        cacheFor(player).remove(player.getUUID());
    }

    public static void invalidate(Player player) {
        cacheFor(player).remove(player.getUUID());
    }

    /** Drops both sides, for the call sites that only know a player id. */
    public static void invalidate(UUID playerId) {
        SERVER.remove(playerId);
        CLIENT.remove(playerId);
    }

    public static void invalidateAll() {
        SERVER.clear();
        CLIENT.clear();
    }

    private static Map<UUID, Map<String, Gated>> cacheFor(Player player) {
        return player.level != null && player.level.isClientSide() ? CLIENT : SERVER;
    }

    private static String key(VaultGod god, String effectId) {
        return god.name() + "/" + effectId;
    }

    /**
     * The one rule for how much of a tree a player receives outside the node gate: full value for
     * the active charm's god, the foreign-tree quarter for every other god while some charm is
     * equipped, and nothing at all while no charm is equipped. The snapshot fold, the piety sources
     * and the imbuement reader all ask here rather than re-deriving it, and {@link #compute}'s stat
     * branch is this same rule applied to one effect.
     */
    public static float treeScale(ServerPlayer player, VaultGod god) {
        Optional<VaultGod> active = ActiveGodResolver.getActiveGod(player);
        if (active.isEmpty()) {
            return 0.0F;
        }
        return active.get() == god ? 1.0F : GodCarryover.FOREIGN_TREE_SCALE;
    }

    private static Gated compute(ServerPlayer player, VaultGod god, String effectId) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Gated.NONE;
        }
        GodAlignmentData data = GodAlignmentData.get(server);
        int points = data.getPointsIn(player.getUUID(), god, effectId);
        if (points <= 0) {
            return Gated.NONE;
        }
        float scale = treeScale(player, god);
        if (scale >= 1.0F) {
            return new Gated(points, 1.0F);
        }
        GodNodeType type = GodNodeRegistry.effectType(effectId);
        if (type == GodNodeType.MINOR && data.getMinorTransfers(player.getUUID(), god).contains(effectId)) {
            return new Gated(points, 1.0F);
        }
        if (type == GodNodeType.STAT && scale > 0.0F) {
            return new Gated(points, scale);
        }
        return Gated.NONE;
    }
}
