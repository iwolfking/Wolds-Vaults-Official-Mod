package xyz.iwolfking.woldsvaults.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.lightning.LightningFormHud;
import xyz.iwolfking.woldsvaults.client.lightning.LightningSpeedLines;

/** Hands HUD overlay rendering to the client features that draw over or replace HUD layers. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class OverlayEvents {

    private OverlayEvents() {
    }

    @SubscribeEvent
    public static void onOverlayPre(RenderGameOverlayEvent.Pre event) {
        LightningSpeedLines.onOverlay(event);
    }

    @SubscribeEvent
    public static void onOverlayPreLayer(RenderGameOverlayEvent.PreLayer event) {
        LightningFormHud.onHudLayer(event);
    }
}
