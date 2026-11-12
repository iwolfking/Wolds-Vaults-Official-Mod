package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;

import java.util.Optional;

/**
 * Whether a node's functional behaviour (handlers, hooks, ultimates) should run for a player right now.
 * Majors are bound to the active tree; minors also run from their own god's minor-transfer slots.
 */
public final class GodNodeGate {
    private GodNodeGate() {
    }

    /**
     * The effective points and scale a player holds in one registered effect: scale 1.0 on the active
     * tree or for a minor in its own god's slots, the carryover scale for a foreign stat node, else none.
     */
    public static GodNodeCache.Gated gate(ServerPlayer player, VaultGod god, String effectId) {
        return GodNodeCache.resolve(player, god, effectId);
    }

    public static int points(ServerPlayer player, VaultGod god, String effectId) {
        return gate(player, god, effectId).points();
    }

    /** The scale the effect's values apply at, or zero when the effect is not live. */
    public static float scale(ServerPlayer player, VaultGod god, String effectId) {
        return gate(player, god, effectId).scale();
    }

    /** The handler-facing view of a live effect, built per query, or empty when the player lacks it. */
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
