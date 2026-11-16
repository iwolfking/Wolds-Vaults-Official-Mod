package xyz.iwolfking.woldsvaults.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.arcane.LaserBeams;
import xyz.iwolfking.woldsvaults.client.arcane.PrismRenderer;
import xyz.iwolfking.woldsvaults.client.champion.ClientChampionHud;
import xyz.iwolfking.woldsvaults.client.lightning.ClientLightningForm;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatSyncClient;
import xyz.iwolfking.woldsvaults.client.spellcraft.keys.SpellBarKeys;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.ClientGodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.ClientVaultGodXp;
import xyz.iwolfking.woldsvaults.client.rampage.ClientRampageCdm;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;

/** Local player events on the client: drops every client-side mirror on disconnect and adjusts the field of view. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientPlayerEvents {

    private ClientPlayerEvents() {
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
        SpellBarKeys.reset();
        StatSyncClient.onLoggedOut(event);
        LaserBeams.clear();
        PrismRenderer.clear();
    }

    @SubscribeEvent
    public static void onFov(FOVModifierEvent event) {
        ClientLightningForm.onFov(event);
    }
}
