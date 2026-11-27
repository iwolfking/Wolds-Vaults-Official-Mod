package xyz.iwolfking.woldsvaults.client.init;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.IOException;
import java.util.function.Consumer;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodNodeEffectsGpu;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Shaders the gods tab loads with the game's own. They are null until the first resource load finishes.
 * backdropGrade is position_tex plus a brightness/contrast/gamma/vibrance grade for the backdrop's textured layers;
 * idolCloud is the animated cloud round the God's Mastery idols (see GodIdolCloud); nodeEffectAccum and
 * nodeEffectTone are the star light's accumulation and tone passes (see GodNodeEffectsGpu); milestoneFrame draws the greed
 * milestone rows' rarity frames and flourishes (see MilestoneFrame); greedFlourish draws the greed Main tab's Take Trial
 * embers and molten reputation bar (see GreedFlourish).
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModShaders {
    public static ShaderInstance backdropGrade;
    public static ShaderInstance idolCloud;
    public static ShaderInstance milestoneFrame;
    public static ShaderInstance greedFlourish;
    public static ShaderInstance nodeEffectAccum;
    public static ShaderInstance nodeEffectTone;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id("backdrop_grade"),
                DefaultVertexFormat.POSITION_TEX), shader -> backdropGrade = shader);
        event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id("idol_cloud"),
                DefaultVertexFormat.POSITION_TEX), shader -> idolCloud = shader);
        milestoneFrame = null;
        greedFlourish = null;
        register(event, "milestone_frame", DefaultVertexFormat.POSITION_COLOR_TEX, shader -> milestoneFrame = shader);
        register(event, "greed_flourish", DefaultVertexFormat.POSITION_TEX, shader -> greedFlourish = shader);
        nodeEffectAccum = null;
        nodeEffectTone = null;
        try {
            event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id("node_effect_accum"),
                    GodNodeEffectsGpu.ACCUM_FORMAT), shader -> nodeEffectAccum = shader);
            event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id("node_effect_tone"),
                    DefaultVertexFormat.POSITION_COLOR_TEX), shader -> nodeEffectTone = shader);
        } catch (IOException | RuntimeException e) {
            WoldLogger.error("God node effect shaders could not be loaded; star light falls back to the CPU path", e);
        }
    }

    /** Registers one optional shader; a shader that fails to load stays null and its users draw their fallback. */
    private static void register(RegisterShadersEvent event, String name, VertexFormat format, Consumer<ShaderInstance> onLoad) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceManager(), WoldsVaults.id(name), format), onLoad);
        } catch (IOException | RuntimeException e) {
            WoldLogger.error("Shader " + name + " could not be loaded; its screen falls back to plain drawing", e);
        }
    }
}
