package xyz.iwolfking.woldsvaults.objectives.extraction;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.core.vault.ClientVaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws a translucent box over the cave of every active lane of the vault's extraction sites, read from each site's
 * synced lane caves, with depth testing off so it shows through walls.
 */
@OnlyIn(Dist.CLIENT)
public final class ExtractionLaneHighlights {
    private ExtractionLaneHighlights() {
    }

    public static void onRenderLevel(RenderLevelLastEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        ExtractionObjective objective = ClientVaults.getActive().flatMap(ExtractionObjective::get).orElse(null);
        if (objective == null) {
            return;
        }
        ExtractionObjectiveConfig.LaneHighlightEntry cfg = ModConfigs.EXTRACTION_OBJECTIVE.WAVES.laneHighlight;
        if (cfg == null) {
            return;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        double range = cfg.rangeBlocks * cfg.rangeBlocks;
        float red = ((cfg.colour >> 16) & 0xFF) / 255.0F;
        float green = ((cfg.colour >> 8) & 0xFF) / 255.0F;
        float blue = (cfg.colour & 0xFF) / 255.0F;

        List<BlockPos> caves = new ArrayList<>();
        for (ExtractionSite site : objective.get(ExtractionObjective.SITES)) {
            if (!site.has(ExtractionSite.LANE_CAVES)) {
                continue;
            }
            for (long packed : site.get(ExtractionSite.LANE_CAVES)) {
                BlockPos cave = BlockPos.of(packed);
                if (camera.distanceToSqr(Vec3.atCenterOf(cave)) <= range) {
                    caves.add(cave);
                }
            }
        }
        if (caves.isEmpty()) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = pose.last().pose();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (BlockPos cave : caves) {
            box(buffer, matrix, cave, cfg, red, green, blue, cfg.fillAlpha, false);
        }
        tesselator.end();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (BlockPos cave : caves) {
            box(buffer, matrix, cave, cfg, red, green, blue, cfg.outlineAlpha, true);
        }
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        pose.popPose();
    }

    private static void box(BufferBuilder b, Matrix4f m, BlockPos cave, ExtractionObjectiveConfig.LaneHighlightEntry cfg, float r, float g, float bl,
                            float a, boolean edges) {
        float x0 = cave.getX() + 0.5F - cfg.radiusBlocks;
        float x1 = cave.getX() + 0.5F + cfg.radiusBlocks;
        float y0 = cave.getY();
        float y1 = cave.getY() + cfg.heightBlocks;
        float z0 = cave.getZ() + 0.5F - cfg.radiusBlocks;
        float z1 = cave.getZ() + 0.5F + cfg.radiusBlocks;
        if (edges) {
            outline(b, m, x0, y0, z0, x1, y1, z1, r, g, bl, a);
        } else {
            faces(b, m, x0, y0, z0, x1, y1, z1, r, g, bl, a);
        }
    }

    private static void faces(BufferBuilder b, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float bl, float a) {
        quad(b, m, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, bl, a);
        quad(b, m, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, bl, a);
        quad(b, m, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, r, g, bl, a);
        quad(b, m, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, bl, a);
        quad(b, m, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, bl, a);
        quad(b, m, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, r, g, bl, a);
    }

    private static void quad(BufferBuilder b, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz,
                             float dx, float dy, float dz, float r, float g, float bl, float a) {
        b.vertex(m, ax, ay, az).color(r, g, bl, a).endVertex();
        b.vertex(m, bx, by, bz).color(r, g, bl, a).endVertex();
        b.vertex(m, cx, cy, cz).color(r, g, bl, a).endVertex();
        b.vertex(m, dx, dy, dz).color(r, g, bl, a).endVertex();
    }

    private static void outline(BufferBuilder b, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float bl, float a) {
        float[][] corners = {{x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1}, {x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}};
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] edge : edges) {
            float[] from = corners[edge[0]];
            float[] to = corners[edge[1]];
            b.vertex(m, from[0], from[1], from[2]).color(r, g, bl, a).endVertex();
            b.vertex(m, to[0], to[1], to[2]).color(r, g, bl, a).endVertex();
        }
    }
}
