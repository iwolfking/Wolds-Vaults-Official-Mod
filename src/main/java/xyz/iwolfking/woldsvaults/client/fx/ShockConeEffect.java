package xyz.iwolfking.woldsvaults.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * One pulse of the riptide shock cone, left in the world where the dash was on that tick: a charged water crown at
 * the base, rippling shock rings inside the cone, and a braid of streamers that swirl back along its length from the
 * apex ahead of the player to the base ring behind. Successive pulses overlap into a trailing, spiralling bow shock.
 */
@OnlyIn(Dist.CLIENT)
final class ShockConeEffect implements RiptideFx.Effect {
    private static final int LIFETIME = 7;
    private static final int SEGMENTS = 64;
    private static final int SWIRLS = 6;
    private static final int SWIRL_POINTS = 12;
    private static final float SWIRL_TWIST = 2.4F;
    private static final float SWIRL_SPIN = 2.8F;
    private static final float SWIRL_LENGTH = 0.5F;
    private static final float GROWTH = 0.45F;
    private static final float DRIFT = 0.8F;
    private static final int BASE_CRESTS = 4;
    private static final int INNER_CRESTS = 3;
    private static final float CRACKLE = 0.06F;
    private static final float CRACKLE_FALLOFF = 0.62F;
    private static final int SPIKES = 13;
    private static final int SPIKE_POINTS = 6;
    private static final double TAU = Math.PI * 2.0;

    private final Vec3 origin;
    private final Vec3 axis;
    private final Vec3 side;
    private final Vec3 up;
    private final float ahead;
    private final float behind;
    private final float radius;
    private final float phase;
    private final long seed;
    private final double[] phases = new double[7];
    private final double[] spikeAngles = new double[SPIKES];
    private final float[] spikeLengths = new float[SPIKES];
    private final float[] spikeLags = new float[SPIKES];
    private final float[][] crackle = new float[3][];
    private int age;

    ShockConeEffect(Vec3 origin, Vec3 axis, float ahead, float behind, float radius, float phase, long seed) {
        this.origin = origin;
        this.axis = axis.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : axis.normalize();
        Vec3[] basis = FxGeometry.basis(this.axis);
        this.side = basis[0];
        this.up = basis[1];
        this.ahead = ahead;
        this.behind = behind;
        this.radius = radius;
        this.phase = phase;
        this.seed = seed;
        Random random = new Random(seed);
        for (int i = 0; i < this.phases.length; i++) {
            this.phases[i] = random.nextDouble() * TAU;
        }
        for (int i = 0; i < SPIKES; i++) {
            this.spikeAngles[i] = random.nextDouble() * TAU;
            this.spikeLengths[i] = 1.0F + random.nextFloat() * 1.2F;
            this.spikeLags[i] = random.nextFloat();
        }
        rollCrackle();
    }

    @Override
    public boolean tick(ClientLevel level) {
        if (++this.age % 2 == 0) {
            rollCrackle();
        }
        return this.age < LIFETIME;
    }

    private void rollCrackle() {
        for (int k = 0; k < this.crackle.length; k++) {
            this.crackle[k] = jag(new Random(this.seed ^ (this.age / 2 + 1) * 0x9E3779B97F4A7C15L ^ k * 0xC2B2AE3D27D4EB4FL));
        }
    }

    /** A closed loop of {@link #SEGMENTS} radial offsets by midpoint displacement, coarse swells with fine crackle on top. */
    private static float[] jag(Random random) {
        float[] values = {0.0F};
        float amplitude = CRACKLE;
        while (values.length < SEGMENTS) {
            float[] next = new float[values.length * 2];
            for (int i = 0; i < values.length; i++) {
                float a = values[i];
                float b = values[(i + 1) % values.length];
                next[i * 2] = a;
                next[i * 2 + 1] = (a + b) * 0.5F + (random.nextFloat() - 0.5F) * amplitude;
            }
            values = next;
            amplitude *= CRACKLE_FALLOFF;
        }
        return values;
    }

    private float progress(float partialTick) {
        return Math.min(1.0F, (this.age + partialTick) / LIFETIME);
    }

    private Vec3 point(float s, double theta, float t, Vec3 camera, double radial, double alongOffset) {
        double length = this.ahead + this.behind;
        double along = this.ahead - s * length - DRIFT * t + alongOffset;
        double r = this.radius * s * (1.0 + GROWTH * t) * radial;
        return this.origin.add(this.axis.scale(along))
                .add(this.side.scale(Math.cos(theta) * r))
                .add(this.up.scale(Math.sin(theta) * r))
                .subtract(camera);
    }

    private Vec3 point(float s, double theta, float t, Vec3 camera) {
        return point(s, theta, t, camera, 1.0, 0.0);
    }

    /**
     * The slow water-surface wobble of a ring at {@code theta}: its radial scale, its offset along the axis and its
     * thickness from 0 to 1. Each ring index {@code k} is shifted so stacked rings ripple out of step.
     */
    private double[] wobble(double theta, float t, int k) {
        double offset = k * 1.7;
        double time = t * Math.PI;
        double radial = 1.0 + 0.075 * Math.sin(3.0 * theta + this.phases[0] + offset + 2.2 * time)
                + 0.045 * Math.sin(5.0 * theta + this.phases[1] - offset - 3.1 * time)
                + 0.022 * Math.sin(9.0 * theta + this.phases[2] + 5.0 * time);
        double along = 0.22 * Math.sin(4.0 * theta + this.phases[3] + offset + 4.0 * time);
        double thick = 0.5 + 0.5 * Math.sin(6.0 * theta + this.phases[4] + offset + 3.0 * time);
        return new double[]{radial, along, thick};
    }

    @Override
    public void renderGlow(VertexConsumer consumer, Matrix4f pose, Vec3 camera, float partialTick) {
        float t = progress(partialTick);
        float fade = (float) Math.pow(1.0F - t, 1.6F);
        float[] rimRgb = FxGeometry.lerp(RiptideFx.TEAL, RiptideFx.VIOLET, t);
        renderBaseRing(consumer, pose, camera, t, fade, rimRgb);
        for (int k = 0; k < 2; k++) {
            float s = 0.35F + 0.6F * ((k * 0.5F + t * 1.5F) % 1.0F);
            renderInnerRing(consumer, pose, camera, s, t, fade * (1.0F - s * 0.4F), k);
        }
        renderSpikes(consumer, pose, camera, t, fade, rimRgb);
        renderSwirls(consumer, pose, camera, t, fade);
    }

    private float crest(double theta, int crests, double drift) {
        double seg = theta * crests / TAU + this.phases[5] / TAU + drift;
        seg -= Math.floor(seg);
        float envelope = (float) Math.max(0.0, Math.min(1.0, (Math.min(seg, 1.0 - seg) - 0.05) * 6.0));
        return envelope * envelope * (3.0F - 2.0F * envelope);
    }

    /**
     * The crown's base ring, broken into drifting crests with tapered gaps: a wide glow that swells and thins along
     * its length, and a white core that crackles off it and changes shape every two ticks.
     */
    private void renderBaseRing(VertexConsumer consumer, Matrix4f pose, Vec3 camera, float t, float fade, float[] rimRgb) {
        float[] jitter = this.crackle[0];
        List<Vec3> glow = new ArrayList<>(SEGMENTS + 1);
        List<Vec3> core = new ArrayList<>(SEGMENTS + 1);
        float[] glowWidths = new float[SEGMENTS + 1];
        float[] coreWidths = new float[SEGMENTS + 1];
        float[][] glowColors = new float[SEGMENTS + 1][];
        float[][] coreColors = new float[SEGMENTS + 1][];
        for (int i = 0; i <= SEGMENTS; i++) {
            double theta = TAU * i / SEGMENTS;
            double[] wave = wobble(theta, t, 0);
            float j = jitter[i % SEGMENTS];
            glow.add(point(1.0F, theta, t, camera, wave[0] + j * 0.35, wave[1]));
            core.add(point(1.0F, theta, t, camera, wave[0] + j, wave[1] + j * 0.6));
            float envelope = crest(theta, BASE_CRESTS, 0.15 * t);
            glowWidths[i] = (float) (0.08 + 0.16 * wave[2]) * (0.4F + 0.6F * envelope);
            coreWidths[i] = (float) (0.025 + 0.05 * wave[2]) * envelope;
            glowColors[i] = FxGeometry.rgba(rimRgb, 0.85F * fade * (0.35F + 0.65F * envelope));
            coreColors[i] = FxGeometry.rgba(RiptideFx.WHITE, 0.95F * fade * envelope);
        }
        FxGeometry.ribbon(consumer, pose, glow, glowWidths, glowColors);
        FxGeometry.ribbon(consumer, pose, core, coreWidths, coreColors);
    }

    private void renderInnerRing(VertexConsumer consumer, Matrix4f pose, Vec3 camera, float s, float t, float fade, int k) {
        float[] jitter = this.crackle[1 + k];
        List<Vec3> ring = new ArrayList<>(SEGMENTS + 1);
        float[] widths = new float[SEGMENTS + 1];
        float[][] colors = new float[SEGMENTS + 1][];
        for (int i = 0; i <= SEGMENTS; i++) {
            double theta = TAU * i / SEGMENTS;
            double[] wave = wobble(theta, t, k);
            double radial = 1.0 + (wave[0] - 1.0) * 1.4;
            ring.add(point(s, theta, t, camera, radial + jitter[i % SEGMENTS] * 0.35, wave[1] * s));
            float envelope = crest(theta, INNER_CRESTS, -0.2 * t);
            widths[i] = (float) (0.04 + 0.07 * wave[2]) * envelope;
            colors[i] = FxGeometry.rgba(RiptideFx.VIOLET, 0.55F * fade * envelope);
        }
        FxGeometry.ribbon(consumer, pose, ring, widths, colors);
    }

    /**
     * Splash spikes thrown out and back from the base ring like a water crown. Each one shoots out over the first half
     * of the pulse, a little after the last, and fades from the rim colour at its root to white at its tip.
     */
    private void renderSpikes(VertexConsumer consumer, Matrix4f pose, Vec3 camera, float t, float fade, float[] rimRgb) {
        float grow = Math.min(1.0F, t * 2.2F);
        float[] widths = new float[SPIKE_POINTS + 1];
        float[][] colors = new float[SPIKE_POINTS + 1][];
        for (int j = 0; j <= SPIKE_POINTS; j++) {
            float f = (float) j / SPIKE_POINTS;
            widths[j] = 0.2F * (1.0F - f) + 0.015F;
            colors[j] = FxGeometry.rgba(FxGeometry.lerp(rimRgb, RiptideFx.WHITE, f), 0.8F * fade * (1.0F - 0.5F * f));
        }
        for (int i = 0; i < SPIKES; i++) {
            float g = Math.max(0.0F, Math.min(1.0F, grow - this.spikeLags[i] * 0.3F));
            if (g <= 0.0F) {
                continue;
            }
            double theta = this.spikeAngles[i];
            double[] wave = wobble(theta, t, 0);
            double bend = Math.sin(this.phases[6] + theta * 3.0);
            List<Vec3> spike = new ArrayList<>(SPIKE_POINTS + 1);
            for (int j = 0; j <= SPIKE_POINTS; j++) {
                float f = (float) j / SPIKE_POINTS;
                double extent = f * this.spikeLengths[i] * g;
                spike.add(point(1.0F, theta + 0.12 * f * bend, t, camera, wave[0] + extent * 0.22, wave[1] - extent * 0.9 - f * f * 0.25 * g));
            }
            FxGeometry.ribbon(consumer, pose, spike, widths, colors);
        }
    }

    /**
     * Streamers wound around the cone as helices. Each one's bright head runs from near the apex to the base over the
     * pulse's life with a fading tail behind it; alternate streamers wind the other way and glow violet, so the set
     * reads as a turning braid.
     */
    private void renderSwirls(VertexConsumer consumer, Matrix4f pose, Vec3 camera, float t, float fade) {
        float head = Math.min(1.0F, 0.15F + t * 1.05F);
        float tail = Math.max(0.06F, head - SWIRL_LENGTH);
        if (head - tail < 0.02F) {
            return;
        }
        for (int i = 0; i < SWIRLS; i++) {
            boolean counter = i % 2 == 1;
            double sign = counter ? -1.0 : 1.0;
            double start = TAU * (this.phase + (double) i / SWIRLS);
            List<Vec3> streamer = new ArrayList<>(SWIRL_POINTS + 1);
            for (int j = 0; j <= SWIRL_POINTS; j++) {
                float s = tail + (head - tail) * j / SWIRL_POINTS;
                double theta = start + sign * (SWIRL_TWIST * s + SWIRL_SPIN * t);
                streamer.add(point(s, theta, t, camera));
            }
            float[] tip = counter ? RiptideFx.VIOLET : RiptideFx.TEAL;
            FxGeometry.ribbon(consumer, pose, streamer, 0.02F, 0.09F,
                    FxGeometry.rgba(tip, 0.0F), FxGeometry.rgba(tip, 0.7F * fade));
            FxGeometry.ribbon(consumer, pose, streamer, 0.006F, 0.03F,
                    FxGeometry.rgba(RiptideFx.WHITE, 0.0F), FxGeometry.rgba(RiptideFx.WHITE, 0.85F * fade));
        }
    }
}
