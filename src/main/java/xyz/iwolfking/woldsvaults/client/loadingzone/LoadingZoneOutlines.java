package xyz.iwolfking.woldsvaults.client.loadingzone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.LoadingZoneArrivalBlock;
import xyz.iwolfking.woldsvaults.blocks.tiles.LoadingZoneMarkerTileEntity;
import xyz.iwolfking.woldsvaults.loadingzone.LoadingZoneShape;
import xyz.iwolfking.woldsvaults.loadingzone.LoadingZones;

// In creative, draws every nearby loading zone: its gate or outline, its posts and its arrival markers with their facing.
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LoadingZoneOutlines {
    private static final double RANGE_SQR = 96.0 * 96.0;
    private static final int ZONE = 0x55FFFF;
    private static final int POST = 0x2AA0A0;
    private static final int LOOSE = 0x808080;
    private static final int ARRIVAL = 0x55FF55;

    private LoadingZoneOutlines() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelLastEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !minecraft.player.isCreative()) {
            return;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);

        for (LoadingZoneShape zone : LoadingZones.allZones(minecraft.level)) {
            if (camera.distanceToSqr(zone.centreX(), zone.minY(), zone.centreZ()) > RANGE_SQR) {
                continue;
            }
            int corners = zone.xs().length;
            int edges = zone.isGate() ? 1 : corners;
            for (int i = 0; i < edges; i++) {
                int next = (i + 1) % corners;
                line(pose, lines, zone.xs()[i], zone.minY(), zone.zs()[i], zone.xs()[next], zone.minY(), zone.zs()[next], ZONE);
                line(pose, lines, zone.xs()[i], zone.maxY(), zone.zs()[i], zone.xs()[next], zone.maxY(), zone.zs()[next], ZONE);
            }
            for (int i = 0; i < corners; i++) {
                line(pose, lines, zone.xs()[i], zone.minY(), zone.zs()[i], zone.xs()[i], zone.maxY(), zone.zs()[i], ZONE);
            }
        }

        for (LoadingZoneMarkerTileEntity marker : LoadingZones.markers(minecraft.level)) {
            BlockPos pos = marker.getBlockPos();
            if (camera.distanceToSqr(Vec3.atCenterOf(pos)) > RANGE_SQR) {
                continue;
            }
            int colour = marker.isArrival() ? ARRIVAL : marker.getLink().isEmpty() ? LOOSE : POST;
            LevelRenderer.renderLineBox(pose, lines, new AABB(pos).deflate(0.3), (colour >> 16 & 255) / 255.0F, (colour >> 8 & 255) / 255.0F, (colour & 255) / 255.0F, 1.0F);
            if (marker.isArrival()) {
                float yaw = LoadingZoneArrivalBlock.yaw(marker.getBlockState()) * Mth.DEG_TO_RAD;
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;
                line(pose, lines, x, y, z, x - Mth.sin(yaw) * 1.5, y, z + Mth.cos(yaw) * 1.5, ARRIVAL);
            }
        }

        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    private static void line(PoseStack pose, VertexConsumer lines, double x0, double y0, double z0, double x1, double y1, double z1, int colour) {
        float dx = (float) (x1 - x0);
        float dy = (float) (y1 - y0);
        float dz = (float) (z1 - z0);
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-4F) {
            return;
        }
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int red = colour >> 16 & 255;
        int green = colour >> 8 & 255;
        int blue = colour & 255;
        lines.vertex(matrix, (float) x0, (float) y0, (float) z0).color(red, green, blue, 255).normal(normal, dx / length, dy / length, dz / length).endVertex();
        lines.vertex(matrix, (float) x1, (float) y1, (float) z1).color(red, green, blue, 255).normal(normal, dx / length, dy / length, dz / length).endVertex();
    }
}
