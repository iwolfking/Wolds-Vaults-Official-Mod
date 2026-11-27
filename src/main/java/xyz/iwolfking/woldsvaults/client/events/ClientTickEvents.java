package xyz.iwolfking.woldsvaults.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.arcane.LaserBeams;
import xyz.iwolfking.woldsvaults.client.arcane.PrismRenderer;
import xyz.iwolfking.woldsvaults.client.bullet.ClientBulletBill;
import xyz.iwolfking.woldsvaults.client.fx.RiptideFx;
import xyz.iwolfking.woldsvaults.client.lightning.AnimatedArmorTexture;
import xyz.iwolfking.woldsvaults.client.lightning.ClientLightningForm;
import xyz.iwolfking.woldsvaults.client.lightning.LightningDashRenderer;
import xyz.iwolfking.woldsvaults.client.lightning.LightningLashRenderer;
import xyz.iwolfking.woldsvaults.client.lightning.LightningSpeedLines;
import xyz.iwolfking.woldsvaults.client.magatsuhi.ClientMagatsuhi;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatSyncClient;
import xyz.iwolfking.woldsvaults.client.sfx.LoopSoundHandler;
import xyz.iwolfking.woldsvaults.client.simon.BlastFeedback;
import xyz.iwolfking.woldsvaults.client.simon.ClientSimon;
import xyz.iwolfking.woldsvaults.client.spellcraft.keys.SpellBarKeys;

/** Hands the client tick and the player tick to the client features that poll on them. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientTickEvents {

    private ClientTickEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        SpellBarKeys.onClientTick(event);
        LoopSoundHandler.onTick(event);
        StatSyncClient.onClientTick(event);
        LightningSpeedLines.onClientTick(event);
        LightningLashRenderer.onClientTick(event);
        LightningDashRenderer.onClientTick(event);
        LaserBeams.onClientTick(event);
        PrismRenderer.onClientTick(event);
        ClientLightningForm.onClientTick(event);
        AnimatedArmorTexture.onClientTick(event);
        RiptideFx.onClientTick(event);
        ClientBulletBill.onClientTick(event);
        ClientMagatsuhi.onClientTick(event);
        BlastFeedback.onClientTick(event);
        ClientSimon.onClientTick(event);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        ClientLightningForm.onPlayerTick(event);
        ClientBulletBill.onPlayerTick(event);
    }
}
