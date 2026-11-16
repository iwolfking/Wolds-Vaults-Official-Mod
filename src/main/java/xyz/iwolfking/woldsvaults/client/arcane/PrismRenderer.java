package xyz.iwolfking.woldsvaults.client.arcane;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import xyz.iwolfking.woldsvaults.abilities.arcane.ArcanePrisms;
import xyz.iwolfking.woldsvaults.abilities.arcane.BeamOptics;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.client.init.ModParticles;
import xyz.iwolfking.woldsvaults.client.particle.PrismParticles;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Arcane: Prism seeds and pyramids. A seed is a small spinning crystal that flies from the caster's hand to its mob on
 * a low arc, following it as it moves, with a fading arcane trail, and bursts where it lands. A pyramid is square glass
 * turning in place at the server's spin, timed from the game time it appeared. It has rainbow-tinted faces, bright
 * edges, soft warm shafts of light pouring in at its tip, a core glow, and four steady level beams in the colors of
 * the spectrum leaving its sides out to its reach (with the split etching, four fainter ones leave its edges). Each
 * beam is traced every frame through {@link BeamOptics}: light that something bends stops where it enters, gathers
 * white-hot, and carries on in the color of what bent it.
 */
public final class PrismRenderer {
    private static final List<Prism> PRISMS = new ArrayList<>();
    private static final List<Seed> SEEDS = new ArrayList<>();
    private static final float PYRAMID_TIP = 0.85F;
    private static final float PYRAMID_BASE_DROP = 0.45F;
    private static final float PYRAMID_HALF_DIAGONAL = 0.75F;
    private static final Vector3f PYRAMID_EDGE = new Vector3f(0.9F, 0.95F, 1.0F);
    private static final int PRISM_FADE_TICKS = 10;
    private static final int SHAFTS = 6;
    private static final float[] BEAM_HUES = {0.0F, 0.13F, 0.45F, 0.75F};
    private static final float[] SPLIT_HUES = {0.06F, 0.29F, 0.6F, 0.875F};
    private static final Vector3f SEED_COLOR = new Vector3f(0.71F, 0.36F, 1.0F);
    private static final int TRAIL_STEPS = 6;
    private static final float TRAIL_STEP_TICKS = 0.5F;
    private static final Random RANDOM = new Random();
    @Nullable
    private static ClientLevel lastLevel;

    private static final class Prism {
        final Vec3 position;
        final float heading;
        final long created;
        final int duration;
        final float reach;
        final boolean split;
        final BeamOptics.Lens lens;
        BeamOptics.Paths paths;

        Prism(Vec3 position, float heading, long created, int duration, float reach, boolean split, BeamOptics.Lens lens) {
            this.position = position;
            this.heading = heading;
            this.created = created;
            this.duration = duration;
            this.reach = Math.max(0.5F, reach);
            this.split = split;
            this.lens = lens;
        }

        double spin(float time) {
            return this.heading + Math.toRadians(ArcanePrisms.SPIN_DEGREES_PER_TICK) * time;
        }

        int beams() {
            return this.split ? 8 : 4;
        }

        int rgb(int k) {
            return this.split && k % 2 == 1 ? Mth.hsvToRgb(SPLIT_HUES[k / 2], 0.6F, 1.0F) : Mth.hsvToRgb(BEAM_HUES[this.split ? k / 2 : k], 0.8F, 1.0F);
        }

        void findPaths(ClientLevel level) {
            this.paths = BeamOptics.bender().near(level, this.position, this.reach, this.lens);
        }

        /** Beam k's path at this angle of spin, bent by anything in its way. */
        List<BeamOptics.Ray> trace(ClientLevel level, double spin, int k) {
            if (this.paths == null) {
                this.findPaths(level);
            }
            double a = spin + k * Math.PI * 2.0 / this.beams();
            Vec3 dir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            return this.paths.trace(this.position, dir, this.reach, null);
        }
    }

    private record Seed(Vec3 start, int targetId, Vec3 aim, long launched, int flight) {
        Seed {
            flight = Math.max(1, flight);
        }

        /** Where it is this long after it left: heading for its mob's middle (following it as it moves), on a low arc. */
        Vec3 at(ClientLevel level, float time, float partial) {
            Vec3 target = this.aim;
            Entity entity = this.targetId < 0 ? null : level.getEntity(this.targetId);
            if (entity != null) {
                target = entity.getPosition(partial).add(0.0, entity.getBbHeight() / 2.0, 0.0);
            }
            float progress = Mth.clamp(time / this.flight, 0.0F, 1.0F);
            double lift = Math.sin(progress * Math.PI) * Math.min(1.5, this.start.distanceTo(target) * 0.08);
            return this.start.lerp(target, progress).add(0.0, lift, 0.0);
        }
    }

    private PrismRenderer() {
    }

    public static void addPrism(Vec3 position, float heading, long created, int duration, float reach, boolean split, float bentLength, float bentRadius) {
        PRISMS.add(new Prism(position, heading, created, duration, reach, split, new BeamOptics.Lens(bentLength, bentRadius)));
    }

    public static void addSeed(Vec3 start, int targetId, Vec3 aim, long launched, int flight) {
        SEEDS.add(new Seed(start, targetId, aim, launched, flight));
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().isPaused()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != lastLevel) {
            clear();
            lastLevel = level;
        }
        if (level == null) {
            return;
        }
        long now = level.getGameTime();
        PRISMS.removeIf(prism -> now - prism.created > prism.duration + PRISM_FADE_TICKS);
        for (Prism prism : PRISMS) {
            prism.findPaths(level);
            long age = now - prism.created;
            if (age >= 0 && age <= prism.duration) {
                beamSparks(level, prism, age);
            }
        }
        SEEDS.removeIf(seed -> {
            boolean landed = now - seed.launched() >= seed.flight();
            if (landed) {
                landingBurst(seed.at(level, seed.flight(), 1.0F));
            }
            return landed;
        });
        SEEDS.forEach(seed -> seedSparks(seed.at(level, Math.max(0.0F, now - seed.launched()), 1.0F)));
    }

    private static void beamSparks(ClientLevel level, Prism prism, long age) {
        double spin = prism.spin(age);
        for (int k = 0; k < prism.beams(); k++) {
            List<BeamOptics.Ray> rays = prism.trace(level, spin, k);
            BeamOptics.Ray ray = rays.get(RANDOM.nextInt(rays.size()));
            if (ray.inside()) {
                continue;
            }
            int rgb = ray.bentBy() == null ? prism.rgb(k) : ColorUtil.rgb(ray.bentBy().shownColor(0.0F));
            Vec3 drift = new Vec3(PrismParticles.spread(0.025), RANDOM.nextDouble() * 0.04, PrismParticles.spread(0.025));
            PrismParticles.spawn(ModParticles.PRISM_SPARK.get(), ray.from().lerp(ray.to(), RANDOM.nextDouble()), drift, ColorUtil.vector(rgb));
        }
    }

    private static void seedSparks(Vec3 at) {
        for (int n = 0; n < 2; n++) {
            Vec3 drift = new Vec3(PrismParticles.spread(0.03), PrismParticles.spread(0.03), PrismParticles.spread(0.03));
            PrismParticles.spawn(ModParticles.PRISM_SPARK.get(), at, drift, ColorUtil.lighten(SEED_COLOR, RANDOM.nextFloat() * 0.5F));
        }
    }

    private static void landingBurst(Vec3 at) {
        PrismParticles.spawnSized(ModParticles.PRISM_FLARE.get(), at, 0.9, SEED_COLOR);
        for (int n = 0; n < 16; n++) {
            double a = n * Math.PI * 2.0 / 16;
            Vec3 velocity = new Vec3(Math.cos(a) * 0.18, (RANDOM.nextDouble() - 0.3) * 0.12, Math.sin(a) * 0.18);
            PrismParticles.spawn(ModParticles.PRISM_SPARK.get(), at, velocity, ColorUtil.vector(Mth.hsvToRgb(n / 16.0F, 0.6F, 1.0F)));
        }
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (PRISMS.isEmpty() && SEEDS.isEmpty()) {
            return;
        }
        GlowShapes.render(event, (consumer, matrix, cam, left, up, level, partial) -> {
            for (Prism prism : PRISMS) {
                draw(consumer, matrix, cam, left, up, level, prism, partial);
            }
            for (Seed seed : SEEDS) {
                draw(consumer, matrix, cam, left, up, level, seed, partial);
            }
        });
    }

    private static void draw(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vector3f left, Vector3f up, ClientLevel level, Prism prism, float partial) {
        float time = (float) (level.getGameTime() - prism.created) + partial;
        float life = Mth.clamp(time / 6.0F, 0.0F, 1.0F) * Mth.clamp((prism.duration + PRISM_FADE_TICKS - time) / PRISM_FADE_TICKS, 0.0F, 1.0F);
        if (life <= 0.0F) {
            return;
        }
        Vec3 center = prism.position;
        float glow = 0.5F + 0.15F * Mth.sin(time * 0.3F);
        double spin = prism.spin(time);
        Vec3 tip = center.add(0.0, PYRAMID_TIP, 0.0);
        Vec3[] base = new Vec3[4];
        for (int k = 0; k < 4; k++) {
            double a = spin + Math.PI / 4.0 + k * Math.PI / 2.0;
            base[k] = center.add(Math.cos(a) * PYRAMID_HALF_DIAGONAL, -PYRAMID_BASE_DROP, Math.sin(a) * PYRAMID_HALF_DIAGONAL);
        }
        float faceAlpha = (0.12F + 0.16F * glow) * life;
        for (int k = 0; k < 4; k++) {
            Vector3f hue = ColorUtil.vector(Mth.hsvToRgb(BEAM_HUES[(k + 1) % 4], 0.45F, 1.0F));
            float[] tint = {hue.x(), hue.y(), hue.z(), faceAlpha};
            float[] clear = {1.0F, 1.0F, 1.0F, faceAlpha * 0.35F};
            GlowShapes.quad(consumer, matrix, base[k], base[(k + 1) % 4], tip, tip, tint, tint, clear, clear);
        }
        float[] floor = {0.85F, 0.9F, 1.0F, 0.06F * life};
        GlowShapes.quad(consumer, matrix, base[0], base[1], base[2], base[3], floor, floor, floor, floor);
        float edge = (0.6F + 0.4F * glow) * life;
        for (int k = 0; k < 4; k++) {
            edge(consumer, matrix, cam, base[k], base[(k + 1) % 4], edge);
            edge(consumer, matrix, cam, base[k], tip, edge);
        }
        GlowShapes.flare(consumer, matrix, center, left, up, 0.25F + 0.4F * glow, new Vector3f(1.0F, 0.95F, 1.0F), (0.4F + 0.5F * glow) * life);
        GlowShapes.flare(consumer, matrix, tip, left, up, 0.2F, GlowShapes.WHITE, 0.35F * life);
        for (int k = 0; k < SHAFTS; k++) {
            double a = spin + Math.PI / 4.0 + k * Math.PI * 2.0 / SHAFTS;
            Vec3 source = tip.add(Math.cos(a) * 2.6, 5.0, Math.sin(a) * 2.6);
            float faint = (0.14F + 0.05F * Mth.sin(time * 0.25F + k * 1.7F)) * life;
            shaft(consumer, matrix, source, tip, GlowShapes.side(source, tip, cam), 0.6F, 0.05F, faint);
        }
        for (int k = 0; k < prism.beams(); k++) {
            boolean splitBeam = prism.split && k % 2 == 1;
            Vector3f beamColor = ColorUtil.vector(prism.rgb(k));
            float strength = (splitBeam ? 0.55F : 0.8F) * life;
            float w = (splitBeam ? 0.11F : 0.19F) * (1.0F + 0.08F * Mth.sin(time * 1.7F + k));
            List<BeamOptics.Ray> rays = prism.trace(level, spin, k);
            for (int i = 0; i < rays.size(); i++) {
                BeamOptics.Ray ray = rays.get(i);
                Vec3 from = ray.from();
                Vec3 to = ray.to();
                if (i == 0) {
                    double length = from.distanceTo(to);
                    from = from.lerp(to, Math.min(1.0, 0.25 / Math.max(1.0E-3, length)));
                }
                Vector3f color = ray.bentBy() == null ? beamColor : ray.bentBy().shownColor(partial);
                if (ray.inside()) {
                    GlowShapes.laser(consumer, matrix, cam, from, to, w * 1.3F, GlowShapes.WHITE, strength);
                    GlowShapes.flare(consumer, matrix, from, left, up, w * 2.2F, color, 0.7F * strength);
                    GlowShapes.flare(consumer, matrix, to, left, up, w * 2.6F, color, 0.8F * strength);
                    continue;
                }
                GlowShapes.laser(consumer, matrix, cam, from, to, ray.bentBy() == null ? w : w * 0.9F, color, strength);
                if (i == rays.size() - 1) {
                    GlowShapes.flare(consumer, matrix, to, left, up, w * 1.6F, color, 0.5F * strength);
                }
            }
        }
    }

    private static void draw(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vector3f left, Vector3f up, ClientLevel level, Seed seed, float partial) {
        float time = (float) (level.getGameTime() - seed.launched()) + partial;
        if (time > seed.flight()) {
            return;
        }
        time = Math.max(0.0F, time);
        Vec3 head = seed.at(level, time, partial);
        Vec3 ahead = head;
        for (int j = 1; j <= TRAIL_STEPS; j++) {
            float back = Math.max(0.0F, time - j * TRAIL_STEP_TICKS);
            Vec3 behind = seed.at(level, back, partial);
            Vec3 side = GlowShapes.side(behind, ahead, cam);
            float fade = 1.0F - (j - 1) / (float) TRAIL_STEPS;
            float w = 0.04F + 0.14F * fade;
            GlowShapes.band(consumer, matrix, behind, ahead, side, w, SEED_COLOR, 0.5F * fade);
            GlowShapes.band(consumer, matrix, behind, ahead, side, w * 0.3F, GlowShapes.WHITE, 0.8F * fade);
            ahead = behind;
            if (back <= 0.0F) {
                break;
            }
        }
        double spin = time * 0.6;
        float size = 0.15F;
        Vec3 top = head.add(0.0, size * 1.5, 0.0);
        Vec3 bottom = head.add(0.0, -size * 1.5, 0.0);
        Vec3[] ring = new Vec3[4];
        for (int k = 0; k < 4; k++) {
            double a = spin + k * Math.PI / 2.0;
            ring[k] = head.add(Math.cos(a) * size, 0.0, Math.sin(a) * size);
        }
        float[] point = {1.0F, 1.0F, 1.0F, 0.5F};
        for (int k = 0; k < 4; k++) {
            Vector3f hue = ColorUtil.vector(Mth.hsvToRgb(BEAM_HUES[k], 0.5F, 1.0F));
            float[] tint = {hue.x(), hue.y(), hue.z(), 0.6F};
            GlowShapes.quad(consumer, matrix, ring[k], ring[(k + 1) % 4], top, top, tint, tint, point, point);
            GlowShapes.quad(consumer, matrix, ring[k], ring[(k + 1) % 4], bottom, bottom, tint, tint, point, point);
        }
        GlowShapes.flare(consumer, matrix, head, left, up, 0.5F, SEED_COLOR, 0.55F);
        GlowShapes.flare(consumer, matrix, head, left, up, 0.2F, GlowShapes.WHITE, 0.9F);
    }

    /**
     * A soft tapering shaft of warm light: halfWide across at the source, halfNarrow at the end, brightest down its
     * middle and towards the end, fading out at its edges and up at the source.
     */
    private static void shaft(VertexConsumer consumer, Matrix4f matrix, Vec3 source, Vec3 end, Vec3 side, float halfWide, float halfNarrow, float alpha) {
        float[] edgeColor = {1.0F, 0.86F, 0.45F, 0.0F};
        float[] sourceMid = {1.0F, 0.9F, 0.55F, alpha * 0.35F};
        float[] endMid = {1.0F, 0.93F, 0.6F, alpha};
        Vec3 wide = side.scale(halfWide);
        Vec3 narrow = side.scale(halfNarrow);
        GlowShapes.quad(consumer, matrix, source.subtract(wide), source, end, end.subtract(narrow), edgeColor, sourceMid, endMid, edgeColor);
        GlowShapes.quad(consumer, matrix, source, source.add(wide), end.add(narrow), end, sourceMid, edgeColor, edgeColor, endMid);
    }

    private static void edge(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vec3 a, Vec3 b, float alpha) {
        GlowShapes.band(consumer, matrix, a, b, GlowShapes.side(a, b, cam), 0.035F, PYRAMID_EDGE, alpha);
    }

    public static void clear() {
        PRISMS.clear();
        SEEDS.clear();
    }
}
