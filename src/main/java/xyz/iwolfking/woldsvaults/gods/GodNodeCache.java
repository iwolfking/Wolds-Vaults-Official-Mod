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
 * The one gate cache, for every god: whether a node is live, resolved once per player and reused until
 * invalidated. Entries drop on charm and dimension change, login, logout and once a second on the ticker.
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

    /** The cached points and scale {@code player} holds in {@code effectId}; rules in {@link GodNodeGate}. */
    public static Gated resolve(ServerPlayer player, VaultGod god, String effectId) {
        return cacheFor(player)
                .computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>())
                .computeIfAbsent(key(god, effectId), key -> compute(player, god, effectId));
    }

    public static void refresh(ServerPlayer player) {
        cacheFor(player).remove(player.getUUID());
    }

    public static void invalidate(Player player) {
        cacheFor(player).remove(player.getUUID());
    }

    /** Drops both logical sides. */
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

    /** Full for the active charm's god, {@link GodCarryover#FOREIGN_TREE_SCALE} for the rest, 0 with none. */
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
        if ((type == GodNodeType.STAT || type == GodNodeType.ROOT) && scale > 0.0F) {
            return new Gated(points, scale);
        }
        return Gated.NONE;
    }
}
