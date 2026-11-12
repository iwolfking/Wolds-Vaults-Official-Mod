package xyz.iwolfking.woldsvaults.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;

/**
 * A branching lightning arc that snakes out of its source and crawls to its target over the first ticks, then
 * flickers and fades. The jagged path is stored as offsets along and across the source-to-target line, so the
 * arc stays pinned to both entities while they move; it is re-rolled every few ticks for the crackle.
 */
@OnlyIn(Dist.CLIENT)
final class LightningArcEffect implements RiptideFx.Effect {
    private static final int DEPTH = 5;
    private static final int REROLL_TICKS = 2;

    private final int sourceId;
    private final int targetId;
    private final Vec3 sourceOffset;
    private final Vec3 targetOffset;
    private Vec3 from;
    private Vec3 to;
    private final boolean chilled;
    private final int lifetime;
    private final int growTicks;
    private final float width;
    private final boolean impact;
    private final long seed;
    private int age;
    private boolean landed;
    private List<float[]> main = List.of();
    private final List<List<float[]>> forks = new ArrayList<>();
    private float flicker = 1.0F;

    LightningArcEffect(int sourceId, Vec3 sourceOffset, Vec3 from, int targetId, Vec3 targetOffset, Vec3 to,
                       boolean chilled, int lifetime, int growTicks, float width, boolean impact, long seed) {
        this.sourceId = sourceId;
        this.sourceOffset = sourceOffset;
        this.from = from;
        this.targetId = targetId;
        this.targetOffset = targetOffset;
        this.to = to;
        this.chilled = chilled;
        this.lifetime = lifetime;
        this.growTicks = growTicks;
        this.width = width;
        this.impact = impact;
        this.seed = seed;
        reroll();
    }

    @Override
    public boolean tick(ClientLevel level) {
        this.age++;
        if (this.age % REROLL_TICKS == 0) {
            reroll();
        }
        if (this.impact && !this.landed && this.age >= this.growTicks) {
            this.landed = true;
            Vec3 end = endpoint(level.getEntity(this.targetId), this.targetOffset, this.to, 1.0F);
            Entity target = level.getEntity(this.targetId);
            float bbWidth = target == null ? 0.6F : target.getBbWidth();
            float bbHeight = target == null ? 1.8F : target.getBbHeight();
            RiptideFx.impactBurst(level, end, bbWidth, bbHeight, this.chilled);
        }
        return this.age < this.lifetime;
    }

    private void reroll() {
        Random random = new Random(this.seed * 31L + this.age / REROLL_TICKS);
        this.flicker = 0.7F + 0.3F * random.nextFloat();
        this.main = displace(0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.16F, DEPTH, random);
        this.forks.clear();
        int forkCount = this.width > 0.6F ? 2 + random.nextInt(2) : random.nextInt(2);
        for (int i = 0; i < forkCount; i++) {
            float[] root = this.main.get(2 + random.nextInt(this.main.size() - 4));
            float reach = 0.18F + random.nextFloat() * 0.22F;
            float endU = Math.min(1.0F, root[0] + reach * 0.6F);
            float endA = root[1] + (random.nextFloat() - 0.5F) * reach * 1.6F;
            float endB = root[2] + (random.nextFloat() - 0.5F) * reach * 1.6F;
            this.forks.add(displace(root[0], root[1], root[2], endU, endA, endB, 0.1F, 3, random));
        }
    }

    /** Midpoint displacement between two (along, across, across) points in arc space. */
    private static List<float[]> displace(float u0, float a0, float b0, float u1, float a1, float b1, float spread, int depth, Random random) {
        List<float[]> points = new ArrayList<>();
        points.add(new float[]{u0, a0, b0});
        points.add(new float[]{u1, a1, b1});
        float amount = spread;
        for (int d = 0; d < depth; d++) {
            List<float[]> next = new ArrayList<>(points.size() * 2);
            for (int i = 0; i < points.size() - 1; i++) {
                float[] p = points.get(i);
                float[] q = points.get(i + 1);
                next.add(p);
                next.add(new float[]{(p[0] + q[0]) * 0.5F,
                        (p[1] + q[1]) * 0.5F + (random.nextFloat() - 0.5F) * amount,
                        (p[2] + q[2]) * 0.5F + (random.nextFloat() - 0.5F) * amount});
            }
            next.add(points.get(points.size() - 1));
            points = next;
            amount *= 0.55F;
        }
        return points;
    }

    boolean isStrike() {
        return this.impact;
    }

    private static Vec3 endpoint(Entity entity, Vec3 offset, Vec3 fallback, float partialTick) {
        return entity == null ? fallback : entity.getPosition(partialTick).add(offset);
    }

    @Override
    public void renderGlow(VertexConsumer consumer, Matrix4f pose, Vec3 camera, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        this.from = endpoint(level.getEntity(this.sourceId), this.sourceOffset, this.from, partialTick);
        this.to = endpoint(level.getEntity(this.targetId), this.targetOffset, this.to, partialTick);
        Vec3 span = this.to.subtract(this.from);
        double length = span.length();
        if (length < 1.0E-3) {
            return;
        }
        Vec3 dir = span.scale(1.0 / length);
        Vec3[] basis = FxGeometry.basis(dir);
        float time = this.age + partialTick;
        float head = this.growTicks <= 0 ? 1.0F : Math.min(1.0F, time / this.growTicks);
        float fadeStart = this.lifetime * 0.45F;
        float fade = time < fadeStart ? 1.0F : Math.max(0.0F, 1.0F - (time - fadeStart) / (this.lifetime - fadeStart));
        float alpha = fade * this.flicker;
        if (alpha <= 0.01F) {
            return;
        }
        float[] glow = this.chilled ? RiptideFx.ICE : RiptideFx.VIOLET;
        float[] mid = this.chilled ? RiptideFx.FROST : RiptideFx.TEAL;
        List<Vec3> path = reveal(this.main, head, length, dir, basis, camera);
        layers(consumer, pose, path, alpha, glow, mid, 1.0F);
        for (List<float[]> fork : this.forks) {
            if (fork.get(0)[0] < head) {
                layers(consumer, pose, reveal(fork, head, length, dir, basis, camera), alpha * 0.8F, glow, mid, 0.55F);
            }
        }
        if (head < 1.0F && !path.isEmpty()) {
            Vec3 tip = path.get(path.size() - 1);
            float flare = 0.55F * this.width;
            Vec3[] cross = FxGeometry.basis(tip.lengthSqr() < 1.0E-6 ? dir : tip.normalize());
            List<Vec3> a = List.of(tip.subtract(cross[0].scale(flare)), tip, tip.add(cross[0].scale(flare)));
            List<Vec3> b = List.of(tip.subtract(cross[1].scale(flare)), tip, tip.add(cross[1].scale(flare)));
            FxGeometry.ribbon(consumer, pose, a, 0.14F * this.width, 0.14F * this.width, FxGeometry.rgba(RiptideFx.WHITE, 0.9F), FxGeometry.rgba(mid, 0.9F));
            FxGeometry.ribbon(consumer, pose, b, 0.14F * this.width, 0.14F * this.width, FxGeometry.rgba(mid, 0.9F), FxGeometry.rgba(RiptideFx.WHITE, 0.9F));
        }
    }

    private void layers(VertexConsumer consumer, Matrix4f pose, List<Vec3> path, float alpha, float[] glow, float[] mid, float scale) {
        float w = this.width * scale;
        FxGeometry.ribbon(consumer, pose, path, 0.42F * w, 0.3F * w, FxGeometry.rgba(glow, 0.3F * alpha), FxGeometry.rgba(glow, 0.22F * alpha));
        FxGeometry.ribbon(consumer, pose, path, 0.16F * w, 0.11F * w, FxGeometry.rgba(mid, 0.75F * alpha), FxGeometry.rgba(mid, 0.6F * alpha));
        FxGeometry.ribbon(consumer, pose, path, 0.055F * w, 0.04F * w, FxGeometry.rgba(RiptideFx.WHITE, alpha), FxGeometry.rgba(RiptideFx.WHITE, 0.9F * alpha));
    }

    private List<Vec3> reveal(List<float[]> profile, float head, double length, Vec3 dir, Vec3[] basis, Vec3 camera) {
        List<Vec3> out = new ArrayList<>(profile.size());
        for (int i = 0; i < profile.size(); i++) {
            float[] p = profile.get(i);
            if (p[0] <= head) {
                out.add(toWorld(p, length, dir, basis, camera));
                continue;
            }
            if (i > 0) {
                float[] q = profile.get(i - 1);
                float f = (head - q[0]) / Math.max(1.0E-4F, p[0] - q[0]);
                out.add(toWorld(new float[]{head, q[1] + (p[1] - q[1]) * f, q[2] + (p[2] - q[2]) * f}, length, dir, basis, camera));
            }
            break;
        }
        return out;
    }

    private Vec3 toWorld(float[] p, double length, Vec3 dir, Vec3[] basis, Vec3 camera) {
        return this.from.add(dir.scale(p[0] * length))
                .add(basis[0].scale(p[1] * length))
                .add(basis[1].scale(p[2] * length))
                .subtract(camera);
    }
}
