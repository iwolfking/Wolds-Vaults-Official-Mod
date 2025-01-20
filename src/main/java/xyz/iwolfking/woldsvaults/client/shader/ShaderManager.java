package xyz.iwolfking.woldsvaults.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.util.VaultUtil;

@Mod.EventBusSubscriber
public class ShaderManager {
    public static final ResourceLocation SHADER = new ResourceLocation("shaders/post/sobel.json");
    public static boolean queuedRefresh = true;


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderWorldStage(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        GameRenderer render = mc.gameRenderer;

        if (VaultUtil.isVaultCorrupted) {
            if (queuedRefresh || render.currentEffect() == null) {
                render.loadEffect(SHADER);
                queuedRefresh = false;
            }

        } else {
            if (render.currentEffect() != null) { // Check if any shader is active
                render.shutdownEffect(); // Unload the current shader
            }
        }
    }

}
