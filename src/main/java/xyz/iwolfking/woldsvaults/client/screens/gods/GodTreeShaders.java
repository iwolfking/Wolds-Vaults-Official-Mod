package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.IOException;

/**
 * Shaders the gods tab loads with the game's own. They are null until the first resource load finishes.
 * backdropGrade is position_tex plus a brightness/contrast/gamma/vibrance grade for the backdrop's textured layers;
 * idolCloud is the animated cloud round the God's Mastery idols (see GodIdolCloud); nodeEffectAccum and
 * nodeEffectTone are the star light's accumulation and tone passes (see GodNodeEffectsGpu).
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GodTreeShaders {
    public static ShaderInstance backdropGrade;
    public static ShaderInstance idolCloud;
    public static ShaderInstance nodeEffectAccum;
    public static ShaderInstance nodeEffectTone;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id("backdrop_grade"),
                DefaultVertexFormat.POSITION_TEX), shader -> backdropGrade = shader);
        event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id("idol_cloud"),
                DefaultVertexFormat.POSITION_TEX), shader -> idolCloud = shader);
        nodeEffectAccum = null;
        nodeEffectTone = null;
        try {
            event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id("node_effect_accum"),
                    GodNodeEffectsGpu.ACCUM_FORMAT), shader -> nodeEffectAccum = shader);
            event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id("node_effect_tone"),
                    DefaultVertexFormat.POSITION_COLOR_TEX), shader -> nodeEffectTone = shader);
        } catch (IOException | RuntimeException e) {
            WoldsVaults.LOGGER.error("God node effect shaders could not be loaded; star light falls back to the CPU path", e);
        }
    }
}
