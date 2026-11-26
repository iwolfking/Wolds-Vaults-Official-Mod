package xyz.iwolfking.woldsvaults.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.arcane.LaserBeams;
import xyz.iwolfking.woldsvaults.client.arcane.PrismRenderer;
import xyz.iwolfking.woldsvaults.client.bullet.ClientBulletBill;
import xyz.iwolfking.woldsvaults.client.fx.RiptideFx;
import xyz.iwolfking.woldsvaults.client.lightning.LightningDashRenderer;
import xyz.iwolfking.woldsvaults.client.lightning.LightningLashRenderer;
import xyz.iwolfking.woldsvaults.client.loadingzone.LoadingZoneOutlines;
import xyz.iwolfking.woldsvaults.client.magatsuhi.ClientMagatsuhi;
import xyz.iwolfking.woldsvaults.client.simon.BlastFeedback;

/** Hands each level render stage, the end of the level render and the camera setup to the client world effects. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class RenderLevelEvents {

    private RenderLevelEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        LightningLashRenderer.onRenderLevel(event);
        LightningDashRenderer.onRenderLevel(event);
        RiptideFx.onRenderStage(event);
        LaserBeams.onRenderLevel(event);
        PrismRenderer.onRenderLevel(event);
        ClientMagatsuhi.onRenderLevel(event);
    }

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelLastEvent event) {
        LoadingZoneOutlines.onRenderLevel(event);
    }

    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        ClientBulletBill.onCameraSetup(event);
        BlastFeedback.onCameraSetup(event);
    }
}
