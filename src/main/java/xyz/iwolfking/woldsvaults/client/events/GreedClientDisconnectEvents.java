package xyz.iwolfking.woldsvaults.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.champion.ClientChampionHud;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.ClientGodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.ClientVaultGodXp;
import xyz.iwolfking.woldsvaults.client.rampage.ClientRampageCdm;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;

/** Drops every client-side greed mirror when the local player disconnects. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class GreedClientDisconnectEvents {

    private GreedClientDisconnectEvents() {
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientGodAlignmentData.clear();
        ClientGodNodePreviews.clear();
        ClientMilestoneData.clear();
        ClientVaultGodXp.clear();
        ClientRampageCdm.clear();
        ClientChampionHud.clear();
        ActiveGodResolver.invalidateAll();
    }
}
