package xyz.iwolfking.woldsvaults.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
final class FxGeometry {
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    private static final Vec3 EAST = new Vec3(1.0, 0.0, 0.0);

    private FxGeometry() {
    }

    /** Two unit vectors perpendicular to {@code axis} and to each other. */
    static Vec3[] basis(Vec3 axis) {
        Vec3 helper = Math.abs(axis.y) < 0.95 ? UP : EAST;
        Vec3 first = axis.cross(helper).normalize();
        Vec3 second = axis.cross(first).normalize();
        return new Vec3[]{first, second};
    }

    /**
     * A camera-facing strip through camera-relative {@code points}, its width and colour blended from start to end.
     * Both windings are emitted so the strip shows whatever the cull state.
     */
    static void ribbon(VertexConsumer consumer, Matrix4f pose, List<Vec3> points, float startWidth, float endWidth, float[] startColor, float[] endColor) {
        int count = points.size();
        if (count < 2) {
            return;
        }
        Vec3[] sides = new Vec3[count];
        for (int i = 0; i < count; i++) {
            Vec3 tangent = points.get(Math.min(count - 1, i + 1)).subtract(points.get(Math.max(0, i - 1)));
            Vec3 side = tangent.cross(points.get(i));
            double length = side.length();
            float f = count == 1 ? 0.0F : (float) i / (count - 1);
            float width = startWidth + (endWidth - startWidth) * f;
            sides[i] = length < 1.0E-6 ? Vec3.ZERO : side.scale(width * 0.5 / length);
        }
        for (int i = 0; i < count - 1; i++) {
            float f0 = (float) i / (count - 1);
            float f1 = (float) (i + 1) / (count - 1);
            float[] c0 = lerp(startColor, endColor, f0);
            float[] c1 = lerp(startColor, endColor, f1);
            Vec3 a = points.get(i).subtract(sides[i]);
            Vec3 b = points.get(i).add(sides[i]);
            Vec3 c = points.get(i + 1).add(sides[i + 1]);
            Vec3 d = points.get(i + 1).subtract(sides[i + 1]);
            quad(consumer, pose, a, b, c, d, c0, c0, c1, c1);
        }
    }

    /** A camera-facing strip like {@link #ribbon(VertexConsumer, Matrix4f, List, float, float, float[], float[])}, with a width and colour per point. */
    static void ribbon(VertexConsumer consumer, Matrix4f pose, List<Vec3> points, float[] widths, float[][] colors) {
        int count = points.size();
        if (count < 2) {
            return;
        }
        Vec3[] sides = new Vec3[count];
        for (int i = 0; i < count; i++) {
            Vec3 tangent = points.get(Math.min(count - 1, i + 1)).subtract(points.get(Math.max(0, i - 1)));
            Vec3 side = tangent.cross(points.get(i));
            double length = side.length();
            sides[i] = length < 1.0E-6 ? Vec3.ZERO : side.scale(widths[i] * 0.5 / length);
        }
        for (int i = 0; i < count - 1; i++) {
            Vec3 a = points.get(i).subtract(sides[i]);
            Vec3 b = points.get(i).add(sides[i]);
            Vec3 c = points.get(i + 1).add(sides[i + 1]);
            Vec3 d = points.get(i + 1).subtract(sides[i + 1]);
            quad(consumer, pose, a, b, c, d, colors[i], colors[i], colors[i + 1], colors[i + 1]);
        }
    }

    /** One quad in both windings with a colour per corner. */
    static void quad(VertexConsumer consumer, Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float[] ca, float[] cb, float[] cc, float[] cd) {
        vertex(consumer, pose, a, ca);
        vertex(consumer, pose, b, cb);
        vertex(consumer, pose, c, cc);
        vertex(consumer, pose, d, cd);
        vertex(consumer, pose, d, cd);
        vertex(consumer, pose, c, cc);
        vertex(consumer, pose, b, cb);
        vertex(consumer, pose, a, ca);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Vec3 point, float[] color) {
        consumer.vertex(pose, (float) point.x, (float) point.y, (float) point.z).color(color[0], color[1], color[2], color[3]).endVertex();
    }

    static float[] lerp(float[] a, float[] b, float f) {
        float[] out = new float[Math.min(a.length, b.length)];
        for (int i = 0; i < out.length; i++) {
            out[i] = a[i] + (b[i] - a[i]) * f;
        }
        return out;
    }

    static float[] rgba(float[] rgb, float alpha) {
        return new float[]{rgb[0], rgb[1], rgb[2], alpha};
    }

    static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0.0F, Math.min(1.0F, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0F - 2.0F * t);
    }
}
