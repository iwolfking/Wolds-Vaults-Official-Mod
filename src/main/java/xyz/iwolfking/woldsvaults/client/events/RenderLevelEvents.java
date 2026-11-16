package xyz.iwolfking.woldsvaults.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.arcane.LaserBeams;
import xyz.iwolfking.woldsvaults.client.arcane.PrismRenderer;
import xyz.iwolfking.woldsvaults.client.fx.RiptideFx;
import xyz.iwolfking.woldsvaults.client.lightning.LightningDashRenderer;
import xyz.iwolfking.woldsvaults.client.lightning.LightningLashRenderer;

/** Hands each level render stage to the client effects drawn in the world. */
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
    }
}
