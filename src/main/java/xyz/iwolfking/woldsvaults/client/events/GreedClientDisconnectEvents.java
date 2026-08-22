package xyz.iwolfking.woldsvaults.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.ClientVaultGodXp;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;

/**
 * Drops every client-side greed mirror when the local player disconnects.
 *
 * <p>Each mirror is a static that survives leaving a world, so without this the god levels,
 * purchased nodes, rank, reputation and milestone counters of the previous server are what the
 * screens read from world-join until the login sync lands. The login sync does replace all of it,
 * so this closes a window rather than a permanent leak - but the window is exactly when a player
 * opens the greed screen right after joining.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class GreedClientDisconnectEvents {

    private GreedClientDisconnectEvents() {
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientGodAlignmentData.clear();
        ClientMilestoneData.clear();
        ClientVaultGodXp.clear();
    }
}
