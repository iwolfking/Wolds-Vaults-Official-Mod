package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeCache;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.UUID;

/**
 * The single teardown contract for god node state: leaving a vault drops that player's scratch, and
 * a vault ending drops that vault's. Logout teardown lives in {@code GodEventHandlers}.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GodNodeLifecycle {
    private static final Object LISTENER_REF = new Object();

    private GodNodeLifecycle() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CommonEvents.LISTENER_LEAVE.register(LISTENER_REF, data -> {
                UUID listenerId = data.getListener().get(Listener.ID);
                if (listenerId != null) {
                    GodNodeState.clear(listenerId);
                    GodNodeCache.invalidate(listenerId);
                }
            });
            CommonEvents.VAULT_END.register(LISTENER_REF, data -> {
                if (data.getVault().has(Vault.ID)) {
                    GodNodeState.clearVault(data.getVault().get(Vault.ID));
                }
            });
        });
    }
}
