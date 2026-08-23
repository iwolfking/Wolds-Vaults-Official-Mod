package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.world.data.ServerVaults;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class TenosVaultUtil {
    private static final Set<ResourceLocation> MISSING_MODIFIERS = ConcurrentHashMap.newKeySet();

    private TenosVaultUtil() {
    }

    /** The vault modifier, or null when {@code vault_modifiers.json} does not declare it. */
    public static VaultModifier<?> resolveModifier(ResourceLocation modifier, String node) {
        VaultModifier<?> resolved = VaultModifierRegistry.get(modifier);
        if (resolved == null && MISSING_MODIFIERS.add(modifier)) {
            WoldsVaults.LOGGER.error("{} could not find vault modifier {}; the node does nothing. "
                    + "Check vault_modifiers.json.", node, modifier);
        }
        return resolved;
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

    public static boolean anyRunnerHas(Vault vault, String effectId) {
        for (ServerPlayer player : runners(vault)) {
            if (TenosNodes.isActive(player, effectId)) {
                return true;
            }
        }
        return false;
    }
}
