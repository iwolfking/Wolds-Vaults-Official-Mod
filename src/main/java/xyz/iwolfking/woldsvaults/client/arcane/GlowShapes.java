package xyz.iwolfking.woldsvaults.client.arcane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;

/**
 * The additive glow shapes Arcane's effects are drawn from (lasers, strips, flares and quads in
 * {@link GlowRenderTypes#GLOW}), and the render pass that draws them after particles, in world space.
 */
public final class GlowShapes {
    public static final Vector3f WHITE = new Vector3f(1.0F, 1.0F, 1.0F);

    /** What one renderer draws in a glow pass. */
    @FunctionalInterface
    public interface Pass {
        void draw(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vector3f left, Vector3f up, ClientLevel level, float partial);
    }

    private GlowShapes() {
    }

    /** Runs {@code pass} in world space with a glow buffer after particles are drawn, and flushes it. */
    public static void render(RenderLevelStageEvent event, Pass pass) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        pass.draw(buffers.getBuffer(GlowRenderTypes.GLOW), pose.last().pose(), cam, camera.getLeftVector(), camera.getUpVector(), level,
                event.getPartialTick());
        buffers.endBatch(GlowRenderTypes.GLOW);
        pose.popPose();
    }

    /** A laser from a to b: soft outer glow, brighter band and white-hot core. */
    public static void laser(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vec3 a, Vec3 b, float w, Vector3f color, float strength) {
        Vec3 side = side(a, b, cam);
        band(consumer, matrix, a, b, side, w, color, 0.35F * strength);
        band(consumer, matrix, a, b, side, w * 0.45F, ColorUtil.lighten(color, 0.3F), 0.7F * strength);
        band(consumer, matrix, a, b, side, w * 0.14F, WHITE, 0.95F * strength);
    }

    /**
     * The direction across the line from a to b, as seen from the camera; zero when the camera is on the line, which
     * collapses the strip so it draws nothing.
     */
    public static Vec3 side(Vec3 a, Vec3 b, Vec3 cam) {
        return b.subtract(a).cross(cam.subtract(a.add(b).scale(0.5))).normalize();
    }

    /** A strip from a to b, brightest along the line and fading to nothing at both edges. */
    public static void band(VertexConsumer consumer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 side, float half, Vector3f color, float alpha) {
        band(consumer, matrix, a, b, side, half, color, alpha, alpha);
    }

    /** The same strip with its own brightness at each end. */
    public static void band(VertexConsumer consumer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 side, float half, Vector3f color, float a0, float a1) {
        Vec3 offset = side.scale(half);
        quad(consumer, matrix, a.subtract(offset), a, b, b.subtract(offset), color, 0.0F, a0, a1, 0.0F);
        quad(consumer, matrix, a, a.add(offset), b.add(offset), b, color, a0, 0.0F, 0.0F, a1);
    }

    /** A glow facing the camera: a diamond bright in the middle, fading out to its four points. */
    public static void flare(VertexConsumer consumer, Matrix4f matrix, Vec3 at, Vector3f left, Vector3f up, float size, Vector3f color, float alpha) {
        Vec3 l = new Vec3(left.x(), left.y(), left.z()).scale(size);
        Vec3 u = new Vec3(up.x(), up.y(), up.z()).scale(size);
        Vec3[] around = {at.add(u), at.subtract(l), at.subtract(u), at.add(l)};
        for (int i = 0; i < 4; i++) {
            Vec3 p = around[i];
            Vec3 q = around[(i + 1) % 4];
            quad(consumer, matrix, p, q, at, at, color, 0.0F, 0.0F, alpha, alpha);
        }
    }

    public static void quad(VertexConsumer consumer, Matrix4f matrix, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
                            float[] c0, float[] c1, float[] c2, float[] c3) {
        vertex(consumer, matrix, p0, c0[0], c0[1], c0[2], c0[3]);
        vertex(consumer, matrix, p1, c1[0], c1[1], c1[2], c1[3]);
        vertex(consumer, matrix, p2, c2[0], c2[1], c2[2], c2[3]);
        vertex(consumer, matrix, p3, c3[0], c3[1], c3[2], c3[3]);
    }

    public static void quad(VertexConsumer consumer, Matrix4f matrix, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
                            Vector3f color, float a0, float a1, float a2, float a3) {
        vertex(consumer, matrix, p0, color.x(), color.y(), color.z(), a0);
        vertex(consumer, matrix, p1, color.x(), color.y(), color.z(), a1);
        vertex(consumer, matrix, p2, color.x(), color.y(), color.z(), a2);
        vertex(consumer, matrix, p3, color.x(), color.y(), color.z(), a3);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 p, float r, float g, float b, float a) {
        consumer.vertex(matrix, (float) p.x, (float) p.y, (float) p.z).color(r, g, b, a).endVertex();
    }
}
