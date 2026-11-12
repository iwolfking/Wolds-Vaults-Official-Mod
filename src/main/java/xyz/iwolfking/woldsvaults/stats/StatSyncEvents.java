package xyz.iwolfking.woldsvaults.stats;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/** Server-side upkeep for the Statistics tab sync. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class StatSyncEvents {
    private StatSyncEvents() {
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerboundStatSyncRequestMessage.forget(event.getPlayer().getUUID());
    }
}
