package xyz.iwolfking.woldsvaults.client.screens.greed;

import xyz.iwolfking.woldsvaults.client.screens.ScreenAnimationClock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.ShaderInstance;
import xyz.iwolfking.woldsvaults.client.init.ModShaders;

/**
 * The animated touches on the greed Main tab, drawn on the GPU by the greed_flourish shader: the Take Trial button's
 * heated plate with embers rising out of it, and the reputation bar's molten fill. Each is one quad whose UVs are GUI
 * pixel positions local to what it decorates, so the shader's effects land on whole pixels. Like the milestone frames,
 * each draws at its element's own depth so Vault Hunters' depth-tested screen doesn't hide it, and falls back to the
 * plain look if the shader didn't load.
 */
public final class GreedFlourish {
    // how far above the Take Trial button its embers can rise
    public static final int EMBER_RISE = 7;
    private static final float MODE_TRIAL = 0.0F;
    private static final float MODE_MOLTEN = 1.0F;
    // how far the bar's leading edge lights the empty track ahead of it
    private static final int MOLTEN_GLOW = 3;

    private GreedFlourish() {
    }

    /**
     * The Take Trial button's plate and embers over (x, y, width, height); the label box (local to the button) is kept
     * clear of embers. The caller draws the label and badge on top.
     */
    public static void drawTrial(PoseStack poseStack, int x, int y, int z, int width, int height, boolean hover,
                                 int textX0, int textY0, int textX1, int textY1) {
        ShaderInstance shader = ModShaders.greedFlourish;
        if (shader == null) {
            atDepth(poseStack, z, () -> {
                GuiComponent.fill(poseStack, x, y, x + width, y + height, GreedTheme.GOLD);
                GuiComponent.fill(poseStack, x + 1, y + 1, x + width - 1, y + height - 1,
                        hover ? GreedTheme.GOLD_DIM : GreedTheme.GOLD_DEEP);
            });
            return;
        }
        shader.safeGetUniform("Area").set((float) width, (float) height, MODE_TRIAL, seconds());
        shader.safeGetUniform("Params").set(hover ? 1.0F : 0.0F, 0.0F, 0.0F, 0.0F);
        shader.safeGetUniform("Text").set((float) textX0, (float) textY0, (float) textX1, (float) textY1);
        quad(poseStack, shader, z, x, y - EMBER_RISE, width, height + EMBER_RISE, 0.0F, -EMBER_RISE);
    }

    /** The bar's molten fill, {@code filled} pixels of a track {@code trackWidth} wide, starting at (x, y). */
    public static void drawMolten(PoseStack poseStack, int x, int y, int z, int filled, int trackWidth, int height) {
        if (filled <= 0) {
            return;
        }
        ShaderInstance shader = ModShaders.greedFlourish;
        if (shader == null) {
            atDepth(poseStack, z, () -> GuiComponent.fill(poseStack, x, y, x + filled, y + height, GreedTheme.GOLD_DIM));
            return;
        }
        shader.safeGetUniform("Area").set((float) trackWidth, (float) height, MODE_MOLTEN, seconds());
        shader.safeGetUniform("Params").set((float) filled, (float) trackWidth, 0.0F, 0.0F);
        quad(poseStack, shader, z, x, y, Math.min(trackWidth, filled + MOLTEN_GLOW), height, 0.0F, 0.0F);
    }

    private static float seconds() {
        return ScreenAnimationClock.seconds();
    }

    private static void atDepth(PoseStack poseStack, int z, Runnable draw) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, z);
        draw.run();
        poseStack.popPose();
    }

    // a quad at (x, y) whose UVs run from (u0, v0) in GUI pixels
    private static void quad(PoseStack poseStack, ShaderInstance shader, int z, int x, int y, int width, int height,
                             float u0, float v0) {
        atDepth(poseStack, z, () -> {
            RenderSystem.setShader(() -> shader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Matrix4f pose = poseStack.last().pose();
            ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, buf -> {
                buf.vertex(pose, x, y + height, 0.0F).uv(u0, v0 + height).endVertex();
                buf.vertex(pose, x + width, y + height, 0.0F).uv(u0 + width, v0 + height).endVertex();
                buf.vertex(pose, x + width, y, 0.0F).uv(u0 + width, v0).endVertex();
                buf.vertex(pose, x, y, 0.0F).uv(u0, v0).endVertex();
            });
        });
    }
}
