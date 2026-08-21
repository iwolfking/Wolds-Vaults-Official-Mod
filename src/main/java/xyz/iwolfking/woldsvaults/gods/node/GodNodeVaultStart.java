package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The one dispatcher for {@link VaultContributor}. Every god node that changes the vault it is
 * entering - vault modifiers, clock rate, loot, objective difficulty - is called from here, once
 * per runner per vault, and no node registers a vault listener of its own.
 *
 * <p>It rides {@code LISTENER_JOIN} rather than {@code VAULT_START}: the base mod declares
 * {@code CommonEvents.VAULT_START} but never invokes it, while {@code LISTENER_JOIN} fires from
 * {@code Listeners.add} after the listener has been initialised against the vault and before the
 * runner is handed control - which is both "the vault exists" and the per-runner granularity the
 * capability is specified at.
 *
 * <p>A player can be added to the same vault more than once (rebirth, and the vault command's
 * join), so the runners already dispatched for are recorded in the vault's own
 * {@link GodNodeState} scratch under a reserved key. That is what makes "once per runner per vault"
 * true without every handler hand-rolling its own guard, and it is scoped by vault id, so the
 * record dies with its vault at {@code VAULT_END} and one party's vault ending cannot re-arm
 * another's.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GodNodeVaultStart {
    private static final String DISPATCH_KEY = "woldsvaults:vault_start_dispatched";
    private static final Object LISTENER_REF = new Object();

    private GodNodeVaultStart() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CommonEvents.LISTENER_JOIN.register(LISTENER_REF,
                data -> onListenerJoin(data.getVault(), data.getListener())));
    }

    /**
     * Runs every live vault contributor for one runner. Public so the god core can replay the
     * pass for a player the join event could not resolve to a server player at the time.
     */
    public static void dispatch(Vault vault, ServerPlayer player) {
        List<GodEffect> effects = GodNodeRegistry.effectsWith(VaultContributor.class);
        for (GodEffect effect : effects) {
            VaultContributor handler = GodNodeRegistry.handler(effect.id(), VaultContributor.class);
            if (handler == null) {
                continue;
            }
            GodNodeContext context = GodNodeGate.context(player, effect).orElse(null);
            if (context == null) {
                continue;
            }
            try {
                handler.onVaultStart(context, vault);
            } catch (RuntimeException e) {
                WoldsVaults.LOGGER.error("God vault contributor {} threw for {}; its contribution to this vault was "
                        + "skipped.", effect.id(), player.getGameProfile().getName(), e);
            }
        }
    }

    private static void onListenerJoin(Vault vault, Listener listener) {
        if (GodNodeRegistry.effectsWith(VaultContributor.class).isEmpty()) {
            return;
        }
        ServerPlayer player = listener.getPlayer().orElse(null);
        if (player == null) {
            return;
        }
        if (!claim(vault, player.getUUID())) {
            return;
        }
        dispatch(vault, player);
    }

    private static boolean claim(Vault vault, UUID playerId) {
        UUID vaultId = vault.has(Vault.ID) ? vault.get(Vault.ID) : null;
        if (vaultId == null) {
            WoldsVaults.LOGGER.warn("A runner joined a vault with no id; god vault contributors ran for them "
                    + "without the once-per-vault guard.");
            return true;
        }
        Set<UUID> dispatched = GodNodeState.getVault(vaultId, DISPATCH_KEY, ConcurrentHashMap::newKeySet);
        return dispatched.add(playerId);
    }
}
