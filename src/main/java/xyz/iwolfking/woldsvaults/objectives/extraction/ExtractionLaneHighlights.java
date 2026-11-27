package xyz.iwolfking.woldsvaults.objectives.extraction;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.core.vault.ClientVaults;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws a skull marker with a soft glow over the cave of every active lane of the vault's extraction sites, read from
 * each site's synced lane caves: in the live colour while mobs that lane spawned are alive, else in the idle colour,
 * and in the stale colour over a cave that left the active lanes while its mobs are still alive. Markers face the camera, ignore depth so they show through walls, and grow with
 * distance between the table's size limits so they keep about the same size on screen.
 */
@OnlyIn(Dist.CLIENT)
public final class ExtractionLaneHighlights {
    private static final ResourceLocation SKULL = WoldsVaults.id("textures/misc/extraction_lane_skull.png");
    private static final ResourceLocation GLOW = WoldsVaults.id("textures/misc/extraction_lane_glow.png");

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
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 eye = camera.getPosition();
        double range = cfg.rangeBlocks * cfg.rangeBlocks;
        List<Vec3> markers = new ArrayList<>();
        List<Integer> colours = new ArrayList<>();
        for (ExtractionSite site : objective.get(ExtractionObjective.SITES)) {
            if (!site.has(ExtractionSite.LANE_CAVES)) {
                continue;
            }
            long[] caves = site.get(ExtractionSite.LANE_CAVES);
            int liveMask = site.has(ExtractionSite.LANE_CAVES_LIVE) ? site.get(ExtractionSite.LANE_CAVES_LIVE) : 0;
            int staleMask = site.has(ExtractionSite.LANE_CAVES_STALE) ? site.get(ExtractionSite.LANE_CAVES_STALE) : 0;
            for (int i = 0; i < caves.length; i++) {
                BlockPos cave = BlockPos.of(caves[i]);
                Vec3 marker = new Vec3(cave.getX() + 0.5D, cave.getY() + cfg.heightBlocks, cave.getZ() + 0.5D);
                if (eye.distanceToSqr(marker) <= range) {
                    markers.add(marker);
                    int bit = i < Integer.SIZE ? 1 << i : 0;
                    colours.add((staleMask & bit) != 0 ? cfg.staleColour : (liveMask & bit) != 0 ? cfg.colour : cfg.idleColour);
                }
            }
        }
        if (markers.isEmpty()) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderTexture(0, GLOW);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int i = 0; i < markers.size(); i++) {
            int colour = colours.get(i);
            billboard(buffer, pose, camera, eye, markers.get(i), size(cfg, eye, markers.get(i)) * cfg.glowScale, colour, cfg.glowAlpha);
        }
        tesselator.end();

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, SKULL);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int i = 0; i < markers.size(); i++) {
            int colour = colours.get(i);
            billboard(buffer, pose, camera, eye, markers.get(i), size(cfg, eye, markers.get(i)), colour, cfg.alpha);
        }
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static float size(ExtractionObjectiveConfig.LaneHighlightEntry cfg, Vec3 eye, Vec3 marker) {
        return Mth.clamp((float) eye.distanceTo(marker) * cfg.sizePerBlock, cfg.minSizeBlocks, cfg.maxSizeBlocks);
    }

    private static void billboard(BufferBuilder buffer, PoseStack pose, Camera camera, Vec3 eye, Vec3 marker, float size, int colour, float a) {
        float r = ((colour >> 16) & 0xFF) / 255.0F;
        float g = ((colour >> 8) & 0xFF) / 255.0F;
        float b = (colour & 0xFF) / 255.0F;
        pose.pushPose();
        pose.translate(marker.x - eye.x, marker.y - eye.y, marker.z - eye.z);
        pose.mulPose(camera.rotation());
        pose.scale(-size, size, size);
        Matrix4f m = pose.last().pose();
        buffer.vertex(m, -0.5F, -0.5F, 0.0F).uv(0.0F, 1.0F).color(r, g, b, a).endVertex();
        buffer.vertex(m, 0.5F, -0.5F, 0.0F).uv(1.0F, 1.0F).color(r, g, b, a).endVertex();
        buffer.vertex(m, 0.5F, 0.5F, 0.0F).uv(1.0F, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(m, -0.5F, 0.5F, 0.0F).uv(0.0F, 0.0F).color(r, g, b, a).endVertex();
        pose.popPose();
    }
}
