package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

import java.util.List;

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
        return GodVaultUtil.runners(vault);
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
