package xyz.iwolfking.woldsvaults.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.bullet.BulletBillRenderer;
import xyz.iwolfking.woldsvaults.client.bullet.ClientBulletBill;
import xyz.iwolfking.woldsvaults.client.fx.RiptideFx;
import xyz.iwolfking.woldsvaults.client.lightning.LightningDashRenderer;
import xyz.iwolfking.woldsvaults.client.loadingzone.ClientLoadingZone;

/** Hands player body and first-person hand rendering to the client features that hide or pose them. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class RenderPlayerEvents {

    private RenderPlayerEvents() {
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        LightningDashRenderer.onRenderPlayer(event);
        if (!event.isCanceled()) {
            BulletBillRenderer.onRenderPlayer(event);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        LightningDashRenderer.onRenderPlayerPost(event);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LightningDashRenderer.onRenderHand(event);
        if (!event.isCanceled()) {
            RiptideFx.hideOffhandWhileRiptiding(event);
        }
        if (!event.isCanceled()) {
            ClientBulletBill.onRenderHand(event);
        }
        if (!event.isCanceled()) {
            ClientLoadingZone.onRenderHand(event);
        }
    }
}
