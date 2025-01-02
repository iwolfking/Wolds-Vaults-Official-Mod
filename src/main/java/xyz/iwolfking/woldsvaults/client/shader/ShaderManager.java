package xyz.iwolfking.woldsvaults.client.shader;

import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.antlr.v4.codegen.model.ModelElement;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

import java.io.IOException;

@Mod.EventBusSubscriber
public class ShaderManager {
    public static final ResourceLocation CORRUPT = new ResourceLocation(WoldsVaults.MOD_ID, "shaders/post/corrupted.json");
    public static PostChain corruptShader;
    public static PostChain lastShader = null;
    public static int lastWidth = 0;
    public static int lastHeight = 0;

    private static PostChain createShaderGroup(ResourceLocation location) {
        try {
            Minecraft mc = Minecraft.getInstance();
            return new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), location);
        } catch (IOException e1) {
            WoldsVaults.LOGGER.warn("Failed to load shader: {}", location, e1);
        } catch (JsonSyntaxException e2) {
            WoldsVaults.LOGGER.warn("Failed to parse shader: {}", location, e2);
        }
        return null;
    }

    private static void makeShaders() {
        if (corruptShader == null) {
            corruptShader = createShaderGroup(CORRUPT);
        }
    }

    public static void updateShaderGroupSize(PostChain shaderGroup) {
        if (shaderGroup != null) {
            Minecraft mc = Minecraft.getInstance();
            int width = mc.getWindow().getWidth();
            int height = mc.getWindow().getHeight();
            if (width != lastWidth || height != lastHeight) {
                lastWidth = width;
                lastHeight = height;
                shaderGroup.resize(width, height);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderWorldStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            if (VaultModifierUtils.isVaultCorrupted) {
                makeShaders();
                if (corruptShader != null) {
                    if (lastShader != corruptShader) {
                        lastShader = corruptShader;
                        lastWidth = 0;
                        lastHeight = 0;
                    }
                    updateShaderGroupSize(corruptShader);
                    corruptShader.process(event.getPartialTick());
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                }
            }
        }
    }
}
