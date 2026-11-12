package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/** Immediate-mode drawing the gods tab shares. Everything draws in the caller's current pose. */
public final class GodDrawing {
    private static final int CIRCLE_SEGMENTS = 28;

    private GodDrawing() {
    }

    public static void line(PoseStack poseStack, float x1, float y1, float x2, float y2, float width, int argb) {
        colorQuads(poseStack, (buf, pose) -> lineQuad(buf, pose, x1, y1, x2, y2, width, argb));
    }

    public static void ring(PoseStack poseStack, float cx, float cy, float outerRadius, float innerRadius, int argb) {
        colorQuads(poseStack, (buf, pose) -> {
            for (int k = 0; k < CIRCLE_SEGMENTS; k++) {
                double a0 = Math.PI * 2.0D * k / CIRCLE_SEGMENTS;
                double a1 = Math.PI * 2.0D * (k + 1) / CIRCLE_SEGMENTS;
                vertex(buf, pose, cx + (float) (Math.cos(a0) * innerRadius), cy + (float) (Math.sin(a0) * innerRadius), argb);
                vertex(buf, pose, cx + (float) (Math.cos(a1) * innerRadius), cy + (float) (Math.sin(a1) * innerRadius), argb);
                vertex(buf, pose, cx + (float) (Math.cos(a1) * outerRadius), cy + (float) (Math.sin(a1) * outerRadius), argb);
                vertex(buf, pose, cx + (float) (Math.cos(a0) * outerRadius), cy + (float) (Math.sin(a0) * outerRadius), argb);
            }
        });
    }

    public static void disc(PoseStack poseStack, float cx, float cy, float radius, int argb) {
        colorQuads(poseStack, (buf, pose) -> {
            for (int k = 0; k < CIRCLE_SEGMENTS; k++) {
                double a0 = Math.PI * 2.0D * k / CIRCLE_SEGMENTS;
                double a1 = Math.PI * 2.0D * (k + 1) / CIRCLE_SEGMENTS;
                vertex(buf, pose, cx, cy, argb);
                vertex(buf, pose, cx + (float) (Math.cos(a1) * radius), cy + (float) (Math.sin(a1) * radius), argb);
                vertex(buf, pose, cx + (float) (Math.cos(a0) * radius), cy + (float) (Math.sin(a0) * radius), argb);
                vertex(buf, pose, cx, cy, argb);
            }
        });
    }

    public static void outline(PoseStack poseStack, int x, int y, int width, int height, int argb) {
        GuiComponent.fill(poseStack, x, y, x + width, y + 1, argb);
        GuiComponent.fill(poseStack, x, y + height - 1, x + width, y + height, argb);
        GuiComponent.fill(poseStack, x, y + 1, x + 1, y + height - 1, argb);
        GuiComponent.fill(poseStack, x + width - 1, y + 1, x + width, y + height - 1, argb);
    }

    public static void check(PoseStack poseStack, float x, float y, float size, int argb) {
        float width = Math.max(1.0F, size / 5.0F);
        line(poseStack, x, y + size * 0.55F, x + size * 0.38F, y + size, width, argb);
        line(poseStack, x + size * 0.38F, y + size, x + size, y + size * 0.08F, width, argb);
    }

    public static void cross(PoseStack poseStack, float x, float y, float size, int argb) {
        float width = Math.max(1.0F, size / 5.0F);
        line(poseStack, x, y, x + size, y + size, width, argb);
        line(poseStack, x + size, y, x, y + size, width, argb);
    }

    /** One cell of a star-frame sheet scaled to {@code size}; column and row constants live on {@link GodTreeTheme}. */
    public static void starFrame(PoseStack poseStack, ResourceLocation sheet, int column, int row, int x, int y, int size, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.setShaderTexture(0, sheet);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GuiComponent.blit(poseStack, x, y, size, size,
                (float) (column * GodTreeTheme.STAR_CELL), (float) (row * GodTreeTheme.STAR_CELL),
                GodTreeTheme.STAR_CELL, GodTreeTheme.STAR_CELL,
                GodTreeTheme.STAR_SHEET_SIZE, GodTreeTheme.STAR_SHEET_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** A whole 16x16 texture drawn at {@code size}. */
    public static void texture(PoseStack poseStack, ResourceLocation texture, int x, int y, int size, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GuiComponent.blit(poseStack, x, y, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private interface QuadSink {
        void accept(BufferBuilder buf, Matrix4f pose);
    }

    private static void colorQuads(PoseStack poseStack, QuadSink sink) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        Matrix4f pose = poseStack.last().pose();
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, buf -> {
            sink.accept(buf, pose);
        });
        RenderSystem.enableTexture();
    }

    private static void lineQuad(BufferBuilder buf, Matrix4f pose, float x1, float y1, float x2, float y2, float width, int argb) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001F) {
            return;
        }
        float px = -dy / length * width / 2.0F;
        float py = dx / length * width / 2.0F;
        vertex(buf, pose, x1 + px, y1 + py, argb);
        vertex(buf, pose, x2 + px, y2 + py, argb);
        vertex(buf, pose, x2 - px, y2 - py, argb);
        vertex(buf, pose, x1 - px, y1 - py, argb);
    }

    private static void vertex(BufferBuilder buf, Matrix4f pose, float x, float y, int argb) {
        buf.vertex(pose, x, y, 0.0F)
                .color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF)
                .endVertex();
    }
}
