package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class GodConstellationLines {
    private static final int STRANDS = 7;
    private static final float SPREAD = 3.0F;
    private static final float WANDER = 0.35F;
    private static final float WANDER_LENGTH = 36.0F;
    private static final float WANDER_SPEED = 0.25F;
    private static final float SPARK_SPACING = 64.0F;
    private static final float SPARK_LENGTH = 9.0F;
    private static final float PIECE_PIXELS = 4.0F;
    private static final float TAU = (float) (Math.PI * 2.0D);

    private record Palette(int strand, int spark, float strandAlpha, float sparkStrength, float sparkSpeed,
                           boolean animated) {}

    private record Line(float x0, float y0, float x1, float y1, float length, int seed, Palette palette) {}

    private GodConstellationLines() {
    }

    public static void render(PoseStack poseStack, VaultGod god, Collection<GodTreeModel.Edge> edges,
                              Map<String, GodNodeWidget> widgets, float minX, float minY, float maxX, float maxY,
                              float viewportScale) {
        Palette lit = new Palette(GodTreeTheme.accent(god), towardWhite(GodTreeTheme.accent(god), 0.75F), 0.55F, 1.0F, 34.0F, true);
        Palette warm = new Palette(GodTreeTheme.accentDim(god), GodTreeTheme.accent(god), 0.45F, 0.9F, 26.0F, true);
        Palette faint = new Palette(GodTreeTheme.accentDeep(god), GodTreeTheme.accentDim(god), 0.4F, 0.0F, 0.0F, false);
        float reach = SPREAD / 2.0F + WANDER + 1.0F;
        List<Line> lines = new ArrayList<>();
        for (GodTreeModel.Edge edge : edges) {
            Line line = line(edge, widgets, lit, warm, faint);
            if (line != null && Math.max(line.x0(), line.x1()) + reach >= minX && Math.min(line.x0(), line.x1()) - reach <= maxX
                    && Math.max(line.y0(), line.y1()) + reach >= minY && Math.min(line.y0(), line.y1()) - reach <= maxY) {
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            return;
        }

        float screenPerUnit = viewportScale * (float) Minecraft.getInstance().getWindow().getGuiScale();
        float strandWidth = Math.max(0.45F, 1.0F / screenPerUnit);
        float pieceLength = Math.max(2.0F, PIECE_PIXELS / screenPerUnit);
        float seconds = (Util.getMillis() % 3_600_000L) / 1000.0F;
        Matrix4f pose = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, buf -> {
            for (Line line : lines) {
                shadow(buf, pose, line, pieceLength);
            }
        });
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, buf -> {
            for (Line line : lines) {
                for (int strand = 0; strand < STRANDS; strand++) {
                    strand(buf, pose, line, strand, strandWidth, pieceLength, seconds);
                }
            }
        });
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
    }

    @Nullable
    private static Line line(GodTreeModel.Edge edge, Map<String, GodNodeWidget> widgets, Palette lit, Palette warm, Palette faint) {
        GodNodeWidget from = widgets.get(edge.from());
        GodNodeWidget to = widgets.get(edge.to());
        if (from == null || to == null) {
            return null;
        }
        if (!from.isUnlocked() && to.isUnlocked()) {
            GodNodeWidget swap = from;
            from = to;
            to = swap;
        }
        float x0 = from.x + from.getWidth() / 2.0F;
        float y0 = from.y + from.getHeight() / 2.0F;
        float x1 = to.x + to.getWidth() / 2.0F;
        float y1 = to.y + to.getHeight() / 2.0F;
        float dx = x1 - x0;
        float dy = y1 - y0;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float trimFrom = from.getWidth() / 2.0F - 2.0F;
        float trimTo = to.getWidth() / 2.0F - 2.0F;
        if (length < 0.001F || trimFrom + trimTo >= length) {
            return null;
        }
        float ux = dx / length;
        float uy = dy / length;
        Palette palette = from.isUnlocked() ? (to.isUnlocked() ? lit : warm) : faint;
        return new Line(x0 + ux * trimFrom, y0 + uy * trimFrom, x1 - ux * trimTo, y1 - uy * trimTo,
                length - trimFrom - trimTo, edge.from().hashCode() * 31 + edge.to().hashCode(), palette);
    }

    private static float taper(float s) {
        return (float) Math.pow(Mth.sin((float) Math.PI * s), 0.6D);
    }

    private static void strand(BufferBuilder buf, Matrix4f pose, Line line, int strand, float width, float pieceLength,
                               float seconds) {
        Palette palette = line.palette();
        if (!palette.animated()) {
            seconds = 0.0F;
        }
        int pieces = Math.max(2, (int) Math.ceil(line.length() / pieceLength));
        float place = ((float) strand / (STRANDS - 1) - 0.5F) * SPREAD;
        float drift = hash(line.seed(), strand) * TAU;
        float sparkOffset = hash(line.seed(), strand + STRANDS);
        float twinkle = 0.75F + 0.25F * Mth.sin(seconds * 1.3F + hash(line.seed(), strand + 2 * STRANDS) * TAU);
        float travel = seconds * palette.sparkSpeed();
        float[] xs = new float[pieces + 1];
        float[] ys = new float[pieces + 1];
        int[] colours = new int[pieces + 1];
        for (int i = 0; i <= pieces; i++) {
            float s = (float) i / pieces;
            float along = s * line.length();
            float taper = taper(s);
            float wander = WANDER * Mth.sin(TAU * (along / WANDER_LENGTH + seconds * WANDER_SPEED) + drift);
            float offset = (place + wander) * taper;
            xs[i] = line.x0() + (line.x1() - line.x0()) * s - (line.y1() - line.y0()) / line.length() * offset;
            ys[i] = line.y0() + (line.y1() - line.y0()) * s + (line.x1() - line.x0()) / line.length() * offset;
            float within = (along - travel) / SPARK_SPACING + sparkOffset;
            within = (within - (float) Math.floor(within)) * SPARK_SPACING;
            float spark = within < SPARK_LENGTH ? Mth.sin((float) Math.PI * within / SPARK_LENGTH) : 0.0F;
            spark = spark * spark * palette.sparkStrength();
            float base = palette.strandAlpha() * twinkle * (0.35F + 0.65F * taper);
            float alpha = Math.min(1.0F, base + spark * (1.0F - base));
            int rgb = mix(palette.strand(), palette.spark(), spark);
            colours[i] = (int) (alpha * 255.0F) << 24 | rgb;
        }
        strip(buf, pose, xs, ys, colours, width / 2.0F, null);
    }

    private static void shadow(BufferBuilder buf, Matrix4f pose, Line line, float pieceLength) {
        int pieces = Math.max(2, (int) Math.ceil(line.length() / pieceLength));
        float[] xs = new float[pieces + 1];
        float[] ys = new float[pieces + 1];
        float[] halves = new float[pieces + 1];
        int[] colours = new int[pieces + 1];
        for (int i = 0; i <= pieces; i++) {
            float s = (float) i / pieces;
            float taper = taper(s);
            xs[i] = line.x0() + (line.x1() - line.x0()) * s;
            ys[i] = line.y0() + (line.y1() - line.y0()) * s;
            halves[i] = 0.8F + (SPREAD / 2.0F + WANDER) * taper;
            colours[i] = (int) (90.0F * (0.3F + 0.7F * taper)) << 24;
        }
        strip(buf, pose, xs, ys, colours, 0.0F, halves);
    }

    private static void strip(BufferBuilder buf, Matrix4f pose, float[] xs, float[] ys, int[] colours, float half,
                              @Nullable float[] halves) {
        int last = xs.length - 1;
        float[] nx = new float[xs.length];
        float[] ny = new float[xs.length];
        for (int i = 0; i <= last; i++) {
            float tx = xs[Math.min(last, i + 1)] - xs[Math.max(0, i - 1)];
            float ty = ys[Math.min(last, i + 1)] - ys[Math.max(0, i - 1)];
            float length = (float) Math.sqrt(tx * tx + ty * ty);
            float h = halves != null ? halves[i] : half;
            nx[i] = length < 0.0001F ? 0.0F : -ty / length * h;
            ny[i] = length < 0.0001F ? 0.0F : tx / length * h;
        }
        for (int i = 0; i < last; i++) {
            vertex(buf, pose, xs[i] + nx[i], ys[i] + ny[i], colours[i]);
            vertex(buf, pose, xs[i + 1] + nx[i + 1], ys[i + 1] + ny[i + 1], colours[i + 1]);
            vertex(buf, pose, xs[i + 1] - nx[i + 1], ys[i + 1] - ny[i + 1], colours[i + 1]);
            vertex(buf, pose, xs[i] - nx[i], ys[i] - ny[i], colours[i]);
        }
    }

    private static void vertex(BufferBuilder buf, Matrix4f pose, float x, float y, int argb) {
        buf.vertex(pose, x, y, 0.0F).color(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24).endVertex();
    }

    private static int mix(int from, int to, float t) {
        int r = (int) Mth.lerp(t, from >> 16 & 0xFF, to >> 16 & 0xFF);
        int g = (int) Mth.lerp(t, from >> 8 & 0xFF, to >> 8 & 0xFF);
        int b = (int) Mth.lerp(t, from & 0xFF, to & 0xFF);
        return r << 16 | g << 8 | b;
    }

    private static int towardWhite(int colour, float t) {
        return mix(colour & 0xFFFFFF, 0xFFFFFF, t);
    }

    private static float hash(int seed, int index) {
        int h = seed * 0x9E3779B9 + index * 0x85EBCA6B;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        return (h >>> 8) / 16777216.0F;
    }
}
