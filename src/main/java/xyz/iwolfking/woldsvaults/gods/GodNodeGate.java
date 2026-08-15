package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Server-side activity checks for god tree nodes. Stat-node VALUES flow through the attribute
 * snapshot ({@link GodNodeAttributeSource}); this gate answers whether a node's FUNCTIONAL
 * behaviour (event handlers, hooks, ultimates) should run for a player right now.
 *
 * <p>Majors are strictly bound to the active tree. Minors also run when the node is selected in
 * the ACTIVE god's minor-transfer slots, regardless of which tree owns it.
 */
public final class GodNodeGate {
    private GodNodeGate() {
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
        Optional<VaultGod> active = ActiveGodResolver.getActiveGod(player);
        if (active.isEmpty()) {
            return 0;
        }
        GodAlignmentData data = GodAlignmentData.get(server);
        if (!data.getMinorTransfers(player.getUUID(), active.get()).contains(nodeId)) {
            return 0;
        }
        return data.getPointsIn(player.getUUID(), god, nodeId);
    }

    public static boolean isActiveMinor(ServerPlayer player, VaultGod god, String nodeId) {
        return minorPoints(player, god, nodeId) > 0;
    }
}
