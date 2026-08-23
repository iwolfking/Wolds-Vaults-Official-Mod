package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;

import java.util.Optional;

/**
 * Server-side activity checks for god tree nodes. Stat-node VALUES flow through the attribute
 * snapshot ({@link GodNodeAttributeSource}); this gate answers whether a node's FUNCTIONAL
 * behaviour (event handlers, hooks, ultimates) should run for a player right now.
 *
 * <p>Majors are strictly bound to the active tree. Minors also run while their own god carries
 * them in one of its minor-transfer slots, whichever god is active - including none - except
 * when that god is the active one, where the constellation already applies them.
 *
 * <p>{@link #gate} is the one entry point for registry-driven effects: it answers with both the
 * effective points and the scale they apply at, behind {@link GodNodeCache}. The uncached
 * {@code activePoints} and {@code minorPoints} readers below predate it and are what the
 * not-yet-ported per-god node content still calls.
 */
public final class GodNodeGate {
    private GodNodeGate() {
    }

    /**
     * The effective points and scale a player holds in one registered effect:
     *
     * <ul>
     *   <li>on the active tree - full points at scale 1.0</li>
     *   <li>a minor carried by one of its own god's transfer slots - full points at scale 1.0</li>
     *   <li>a stat node on a foreign tree - full points at the carryover scale</li>
     *   <li>anything else - nothing</li>
     * </ul>
     */
    public static GodNodeCache.Gated gate(ServerPlayer player, VaultGod god, String effectId) {
        return GodNodeCache.resolve(player, god, effectId);
    }

    /** Effective points in one registered effect, gated as {@link #gate} describes. */
    public static int points(ServerPlayer player, VaultGod god, String effectId) {
        return gate(player, god, effectId).points();
    }

    /** The scale the effect's values apply at, or zero when the effect is not live. */
    public static float scale(ServerPlayer player, VaultGod god, String effectId) {
        return gate(player, god, effectId).scale();
    }

    /**
     * The handler-facing view of a live effect, or empty when the player does not hold it.
     * Built per query, never cached, so a handler always reads current points, scale and piety.
     */
    public static Optional<GodNodeContext> context(ServerPlayer player, VaultGod god, String effectId) {
        GodNodeCache.Gated gated = gate(player, god, effectId);
        if (!gated.isActive()) {
            return Optional.empty();
        }
        GodEffect effect = GodNodeRegistry.effect(effectId).orElse(null);
        if (effect == null) {
            return Optional.empty();
        }
        return Optional.of(new GodNodeContext(player, god, effectId, gated.points(), effect.values(),
                gated.scale(), GodPiety.total(player, god)));
    }

    /** As {@link #context(ServerPlayer, VaultGod, String)}, for a caller that already has the effect. */
    public static Optional<GodNodeContext> context(ServerPlayer player, GodEffect effect) {
        return context(player, effect.god(), effect.id());
    }

    public static int activePoints(ServerPlayer player, VaultGod god, String nodeId) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        Optional<VaultGod> active = ActiveGodResolver.getActiveGod(player);
        if (active.isEmpty() || active.get() != god) {
            return 0;
        }
        return GodAlignmentData.get(server).getPointsIn(player.getUUID(), god, nodeId);
    }

    public static boolean isActiveMajor(ServerPlayer player, VaultGod god, String nodeId) {
        return activePoints(player, god, nodeId) > 0;
    }

    public static int minorPoints(ServerPlayer player, VaultGod god, String nodeId) {
        int strict = activePoints(player, god, nodeId);
        if (strict > 0) {
            return strict;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        GodAlignmentData data = GodAlignmentData.get(server);
        if (!data.getMinorTransfers(player.getUUID(), god).contains(nodeId)) {
            return 0;
        }
        return data.getPointsIn(player.getUUID(), god, nodeId);
    }

    public static boolean isActiveMinor(ServerPlayer player, VaultGod god, String nodeId) {
        return minorPoints(player, god, nodeId) > 0;
    }
}
