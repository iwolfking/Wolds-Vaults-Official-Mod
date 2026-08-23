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

    /**
     * The vault modifier a node attaches, or null when {@code vault_modifiers.json} does not declare
     * it. Nodes look their modifier up rather than registering it: an id missing from
     * {@link VaultModifierRegistry} is silently dropped from {@code Modifiers.getModifiers()}, so a
     * node that registered its own id would stop counting its own contribution the moment a config
     * reload cleared the registry, and would attach a second copy. The complaint is made once per
     * id, because the callers run on the shared one-second ticker.
     */
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
