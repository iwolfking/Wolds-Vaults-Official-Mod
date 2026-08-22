package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.player.Runner;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * The two lookups every god tree needs and none of them owns: who is running a vault, and how many
 * points a player has put into one effect.
 *
 * <p>Both existed once per tree before. They are the same question regardless of which god is
 * asking, so a tree that copied them could only ever drift from the others.
 */
public final class GodVaultUtil {

    private GodVaultUtil() {
    }

    /** The players currently running {@code vault}; empty for a null vault or one with no runners. */
    public static List<ServerPlayer> runners(Vault vault) {
        List<ServerPlayer> players = new ArrayList<>();
        if (vault == null) {
            return players;
        }
        for (Runner runner : vault.get(Vault.LISTENERS).getAll(Runner.class)) {
            runner.getPlayer().ifPresent(players::add);
        }
        return players;
    }

    /**
     * The points {@code player} has banked under {@code effectId} in {@code god}'s tree. Zero when
     * the player has no server, which is the case on the logical client.
     */
    public static int investedPoints(ServerPlayer player, VaultGod god, String effectId) {
        MinecraftServer server = player == null ? null : player.getServer();
        if (server == null) {
            return 0;
        }
        return GodAlignmentData.get(server).getPointsIn(player.getUUID(), god, effectId);
    }
}
