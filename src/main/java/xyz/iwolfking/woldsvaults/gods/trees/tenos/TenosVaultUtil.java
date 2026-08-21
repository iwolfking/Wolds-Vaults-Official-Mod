package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Small vault lookups shared by the Tenos handlers. */
public final class TenosVaultUtil {
    private TenosVaultUtil() {
    }

    public static Vault vaultOf(Level level) {
        return ServerVaults.get(level).orElse(null);
    }

    public static Vault vaultOf(ServerPlayer player) {
        return player == null ? null : vaultOf(player.getLevel());
    }

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
     * Whether any runner in the vault holds a live Tenos effect. Only the nodes whose effect is
     * inherently vault wide ask this; a node with a per-player effect is dispatched per player by
     * the god core instead.
     */
    public static boolean anyRunnerHas(Vault vault, String effectId) {
        for (ServerPlayer player : runners(vault)) {
            if (TenosNodes.isActive(player, effectId)) {
                return true;
            }
        }
        return false;
    }
}
