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
 * The one dispatcher for {@link VaultContributor}: every god node that changes the vault being
 * entered runs from here on {@code LISTENER_JOIN}, once per runner per vault. The guard against a
 * player being added to the same vault twice lives in that vault's {@link GodNodeState} scratch.
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

    /** Runs every live vault contributor for one runner; public so the pass can be replayed. */
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
            WoldsVaults.LOGGER.warn("A listener joined a vault with no resolvable player; god vault "
                    + "contributors did not run for them, so any vault-start node they own is inert this run.");
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
