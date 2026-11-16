package xyz.iwolfking.woldsvaults.client.arcane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.client.init.ModParticles;
import xyz.iwolfking.woldsvaults.client.particle.PrismParticles;
import xyz.iwolfking.woldsvaults.modifiers.arcane.BeamOptics;
import xyz.iwolfking.woldsvaults.network.message.ArcanePrismMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Laser beams (Arcane, Arcane Rail and Arcane: Prism), drawn after particles as
 * layered additive strips that face the camera: a wide soft glow and a narrower brighter band, both fading to nothing
 * at their edges, round a thin white-hot core, with a flare at every point of the line. Each beam lives a few ticks
 * (a held Arcane beam is renewed every tick), shimmers in width, fades and thins out over the second half of its life
 * and sheds sparks along its length.
 * <p>
 * Also the Arcane: Prism seeds and pyramids. A seed is a small spinning crystal that flies from the caster's hand to
 * its mob on a low arc, following it as it moves, with a fading arcane trail, and bursts where it lands. A pyramid is
 * square glass turning in place, at the server's spin timed from the game time it appeared. It has rainbow-tinted
 * faces, bright edges, soft warm shafts of light pouring in at its tip, a core glow, and four steady level beams in the
 * colours of the spectrum leaving its sides out to its reach (with the split etching, four fainter ones leave its
 * edges). Each beam is traced every frame through {@link BeamOptics}, the same as the server does, so light that
 * something bends is drawn where it really goes: it stops where it enters, gathers white-hot, and carries on in the
 * colour of what bent it.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public final class LaserBeams {
    public static final Vector3f WHITE = new Vector3f(1.0F, 1.0F, 1.0F);
    private static final List<Beam> BEAMS = new ArrayList<>();
    private static final List<Prism> PRISMS = new ArrayList<>();
    private static final List<Seed> SEEDS = new ArrayList<>();
    // the pyramid: its tip above the beams, its base below them, half the base's diagonal
    private static final float PYRAMID_TIP = 0.85F;
    private static final float PYRAMID_BASE_DROP = 0.45F;
    private static final float PYRAMID_HALF_DIAGONAL = 0.75F;
    private static final Vector3f PYRAMID_EDGE = new Vector3f(0.9F, 0.95F, 1.0F);
    private static final int PRISM_FADE_TICKS = 10;
    // the light shafts pouring into the tip
    private static final int SHAFTS = 6;
    // the four beams' colours, round the spectrum, and the split etching's four in between them
    private static final float[] BEAM_HUES = {0.0F, 0.13F, 0.45F, 0.75F};
    private static final float[] SPLIT_HUES = {0.06F, 0.29F, 0.6F, 0.875F};
    // the seed's arcane violet, and how far back its trail reaches (in ticks of flight)
    private static final Vector3f SEED_COLOR = new Vector3f(0.71F, 0.36F, 1.0F);
    private static final int TRAIL_STEPS = 6;
    private static final float TRAIL_STEP_TICKS = 0.5F;
    private static final Random RANDOM = new Random();

    private static final class Beam {
        final List<Vec3> points;
        final Vector3f color;
        final float width;
        final int ticks;
        int age;

        Beam(List<Vec3> points, int rgb, float width, int ticks) {
            this.points = points;
            this.color = ColorUtil.vector(rgb);
            this.width = width;
            this.ticks = Math.max(1, ticks);
        }
    }

    private static final class Prism {
        final Vec3 position;
        final float yaw;
        final long created;
        final int duration;
        final float reach;
        final boolean split;
        final BeamOptics.Lens lens;
        // how its beams travel, looked up once a tick (or on first use, if it is drawn before its first tick)
        BeamOptics.Paths paths;

        Prism(Vec3 position, float yaw, long created, int duration, float reach, boolean split, BeamOptics.Lens lens) {
            this.position = position;
            this.yaw = yaw;
            this.created = created;
            this.duration = duration;
            this.reach = Math.max(0.5F, reach);
            this.split = split;
            this.lens = lens;
        }

        // the angle of its first beam at this time since it appeared
        double spin(float time) {
            return this.yaw + Math.toRadians(ArcanePrismMessage.SPIN_DEGREES_PER_TICK) * time;
        }

        int beams() {
            return this.split ? 8 : 4;
        }

        // beam k's colour: the four main ones round the spectrum, the split ones between them
        int rgb(int k) {
            return this.split && k % 2 == 1 ? Mth.hsvToRgb(SPLIT_HUES[k / 2], 0.6F, 1.0F) : Mth.hsvToRgb(BEAM_HUES[this.split ? k / 2 : k], 0.8F, 1.0F);
        }

        void findPaths(ClientLevel level) {
            this.paths = BeamOptics.bender().near(level, this.position, this.reach, this.lens);
        }

        // beam k's path at this angle of spin, bent by anything in its way
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

        // where it is this long after it left: heading for its mob's middle (following it as it moves), on a low arc
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

    private LaserBeams() {
    }

    public static void addPrism(Vec3 position, float yaw, long created, int duration, float reach, boolean split, float bentRange, float bentRadius) {
        PRISMS.add(new Prism(position, yaw, created, duration, reach, split, new BeamOptics.Lens(bentRange, bentRadius)));
    }

    public static void addSeed(Vec3 start, int targetId, Vec3 aim, long launched, int flight) {
        SEEDS.add(new Seed(start, targetId, aim, launched, flight));
    }

    public static void add(List<Vec3> points, int rgb, float width, int ticks) {
        BEAMS.add(new Beam(points, rgb, width, ticks));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().isPaused()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
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
        } else {
            SEEDS.clear();
        }
        BEAMS.removeIf(beam -> ++beam.age > beam.ticks);
        BEAMS.forEach(LaserBeams::sparks);
    }

    // a spark drifting off somewhere along each beam's path, in that piece's colour
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

    // the seed taking root: a flash and a ring of sparks
    private static void landingBurst(Vec3 at) {
        PrismParticles.spawnSized(ModParticles.PRISM_FLARE.get(), at, 0.9, SEED_COLOR);
        for (int n = 0; n < 16; n++) {
            double a = n * Math.PI * 2.0 / 16;
            Vec3 velocity = new Vec3(Math.cos(a) * 0.18, (RANDOM.nextDouble() - 0.3) * 0.12, Math.sin(a) * 0.18);
            PrismParticles.spawn(ModParticles.PRISM_SPARK.get(), at, velocity, ColorUtil.vector(Mth.hsvToRgb(n / 16.0F, 0.6F, 1.0F)));
        }
    }

    // a few sparks drifting off the beam, and one at its far end
    private static void sparks(Beam beam) {
        Vector3f bright = ColorUtil.lighten(beam.color, 0.4F);
        for (int i = 0; i + 1 < beam.points.size(); i++) {
            Vec3 a = beam.points.get(i);
            Vec3 b = beam.points.get(i + 1);
            int count = 1 + (int) (a.distanceTo(b) / 6.0);
            for (int n = 0; n < count; n++) {
                Vec3 drift = new Vec3(PrismParticles.spread(0.025), PrismParticles.spread(0.025), PrismParticles.spread(0.025));
                PrismParticles.spawn(ModParticles.PRISM_SPARK.get(), a.lerp(b, RANDOM.nextDouble()), drift, bright);
            }
        }
        Vec3 drift = new Vec3(PrismParticles.spread(0.075), RANDOM.nextDouble() * 0.1, PrismParticles.spread(0.075));
        PrismParticles.spawn(ModParticles.PRISM_SPARK.get(), beam.points.get(beam.points.size() - 1), drift, beam.color);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || BEAMS.isEmpty() && PRISMS.isEmpty() && SEEDS.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(GlowRenderTypes.GLOW);
        float partial = event.getPartialTick();
        for (Beam beam : BEAMS) {
            draw(consumer, matrix, cam, left, up, beam, partial);
        }
        for (Prism prism : PRISMS) {
            draw(consumer, matrix, cam, left, up, level, prism, partial);
        }
        for (Seed seed : SEEDS) {
            draw(consumer, matrix, cam, left, up, level, seed, partial);
        }
        buffers.endBatch(GlowRenderTypes.GLOW);
        pose.popPose();
    }

    private static void draw(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vector3f left, Vector3f up, Beam beam, float partial) {
        float time = beam.age + partial;
        float fade = Mth.clamp((beam.ticks + 1 - time) / Math.max(2.0F, beam.ticks * 0.5F), 0.0F, 1.0F);
        if (fade <= 0.0F) {
            return;
        }
        for (int i = 0; i + 1 < beam.points.size(); i++) {
            Vec3 a = beam.points.get(i);
            Vec3 b = beam.points.get(i + 1);
            float shimmer = 1.0F + 0.12F * Mth.sin(time * 1.7F + i * 1.3F);
            laser(consumer, matrix, cam, a, b, beam.width * shimmer * (0.5F + 0.5F * fade), beam.color, fade);
        }
        for (int i = 0; i < beam.points.size(); i++) {
            float size = beam.width * (i == 0 || i == beam.points.size() - 1 ? 1.4F : 1.8F) * (1.0F + 0.15F * Mth.sin(time * 2.3F + i));
            flare(consumer, matrix, beam.points.get(i), left, up, size, beam.color, 0.6F * fade);
        }
    }

    private static void draw(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vector3f left, Vector3f up, ClientLevel level, Prism prism, float partial) {
        float time = (float) (level.getGameTime() - prism.created) + partial;
        float life = Mth.clamp(time / 6.0F, 0.0F, 1.0F) * Mth.clamp((prism.duration + PRISM_FADE_TICKS - time) / PRISM_FADE_TICKS, 0.0F, 1.0F);
        if (life <= 0.0F) {
            return;
        }
        Vec3 center = prism.position;
        // a slow, steady shimmer (the beams no longer pulse)
        float glow = 0.5F + 0.15F * Mth.sin(time * 0.3F);
        double spin = prism.spin(time);
        Vec3 tip = center.add(0.0, PYRAMID_TIP, 0.0);
        Vec3[] base = new Vec3[4];
        for (int k = 0; k < 4; k++) {
            double a = spin + Math.PI / 4.0 + k * Math.PI / 2.0;
            base[k] = center.add(Math.cos(a) * PYRAMID_HALF_DIAGONAL, -PYRAMID_BASE_DROP, Math.sin(a) * PYRAMID_HALF_DIAGONAL);
        }
        // glass faces, each tinted with the colour of the beam that leaves it, clearer towards the tip
        float faceAlpha = (0.12F + 0.16F * glow) * life;
        for (int k = 0; k < 4; k++) {
            Vector3f hue = ColorUtil.vector(Mth.hsvToRgb(BEAM_HUES[(k + 1) % 4], 0.45F, 1.0F));
            float[] tint = {hue.x(), hue.y(), hue.z(), faceAlpha};
            float[] clear = {1.0F, 1.0F, 1.0F, faceAlpha * 0.35F};
            quad(consumer, matrix, base[k], base[(k + 1) % 4], tip, tip, tint, tint, clear, clear);
        }
        float[] floor = {0.85F, 0.9F, 1.0F, 0.06F * life};
        quad(consumer, matrix, base[0], base[1], base[2], base[3], floor, floor, floor, floor);
        // bright edges
        float edge = (0.6F + 0.4F * glow) * life;
        for (int k = 0; k < 4; k++) {
            edge(consumer, matrix, cam, base[k], base[(k + 1) % 4], edge);
            edge(consumer, matrix, cam, base[k], tip, edge);
        }
        // the light inside
        flare(consumer, matrix, center, left, up, 0.25F + 0.4F * glow, new Vector3f(1.0F, 0.95F, 1.0F), (0.4F + 0.5F * glow) * life);
        flare(consumer, matrix, tip, left, up, 0.2F, WHITE, 0.35F * life);
        // soft shafts of warm light pouring in at the tip from above: wide and faint high up, gathering into the tip,
        // turning with the pyramid
        for (int k = 0; k < SHAFTS; k++) {
            double a = spin + Math.PI / 4.0 + k * Math.PI * 2.0 / SHAFTS;
            Vec3 source = tip.add(Math.cos(a) * 2.6, 5.0, Math.sin(a) * 2.6);
            float faint = (0.14F + 0.05F * Mth.sin(time * 0.25F + k * 1.7F)) * life;
            shaft(consumer, matrix, source, tip, side(source, tip, cam), 0.6F, 0.05F, faint);
        }
        // the beams out of its sides (and, split, its edges), sweeping round with it, each bent by whatever it enters
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
                    // out of the glass, not its middle
                    double length = from.distanceTo(to);
                    from = from.lerp(to, Math.min(1.0, 0.25 / Math.max(1.0E-3, length)));
                }
                Vector3f color = ray.bentBy() == null ? beamColor : ray.bentBy().shownColor(partial);
                if (ray.inside()) {
                    // crossing inside what bent it, white-hot, flaring where it goes in and comes out
                    laser(consumer, matrix, cam, from, to, w * 1.3F, WHITE, strength);
                    flare(consumer, matrix, from, left, up, w * 2.2F, color, 0.7F * strength);
                    flare(consumer, matrix, to, left, up, w * 2.6F, color, 0.8F * strength);
                    continue;
                }
                laser(consumer, matrix, cam, from, to, ray.bentBy() == null ? w : w * 0.9F, color, strength);
                if (i == rays.size() - 1) {
                    flare(consumer, matrix, to, left, up, w * 1.6F, color, 0.5F * strength);
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
        // the trail: back along the path it came, thinning and fading
        Vec3 ahead = head;
        for (int j = 1; j <= TRAIL_STEPS; j++) {
            float back = Math.max(0.0F, time - j * TRAIL_STEP_TICKS);
            Vec3 behind = seed.at(level, back, partial);
            Vec3 side = side(behind, ahead, cam);
            float fade = 1.0F - (j - 1) / (float) TRAIL_STEPS;
            float w = 0.04F + 0.14F * fade;
            band(consumer, matrix, behind, ahead, side, w, SEED_COLOR, 0.5F * fade);
            band(consumer, matrix, behind, ahead, side, w * 0.3F, WHITE, 0.8F * fade);
            ahead = behind;
            if (back <= 0.0F) {
                break;
            }
        }
        // the seed: a small spinning crystal (two pyramids base to base), rainbow-tinted round a white-hot core
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
            quad(consumer, matrix, ring[k], ring[(k + 1) % 4], top, top, tint, tint, point, point);
            quad(consumer, matrix, ring[k], ring[(k + 1) % 4], bottom, bottom, tint, tint, point, point);
        }
        flare(consumer, matrix, head, left, up, 0.5F, SEED_COLOR, 0.55F);
        flare(consumer, matrix, head, left, up, 0.2F, WHITE, 0.9F);
    }

    // a laser from a to b: soft outer glow, brighter band and white-hot core
    private static void laser(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vec3 a, Vec3 b, float w, Vector3f color, float strength) {
        Vec3 side = side(a, b, cam);
        band(consumer, matrix, a, b, side, w, color, 0.35F * strength);
        band(consumer, matrix, a, b, side, w * 0.45F, ColorUtil.lighten(color, 0.3F), 0.7F * strength);
        band(consumer, matrix, a, b, side, w * 0.14F, WHITE, 0.95F * strength);
    }

    // a soft tapering shaft of warm light: halfWide across at the source, halfNarrow at the end, brightest down its middle
    // and towards the end, fading out at its edges and up at the source
    private static void shaft(VertexConsumer consumer, Matrix4f matrix, Vec3 source, Vec3 end, Vec3 side, float halfWide, float halfNarrow, float alpha) {
        float[] edgeColor = {1.0F, 0.86F, 0.45F, 0.0F};
        float[] sourceMid = {1.0F, 0.9F, 0.55F, alpha * 0.35F};
        float[] endMid = {1.0F, 0.93F, 0.6F, alpha};
        Vec3 wide = side.scale(halfWide);
        Vec3 narrow = side.scale(halfNarrow);
        quad(consumer, matrix, source.subtract(wide), source, end, end.subtract(narrow), edgeColor, sourceMid, endMid, edgeColor);
        quad(consumer, matrix, source, source.add(wide), end.add(narrow), end, sourceMid, edgeColor, edgeColor, endMid);
    }

    private static void edge(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vec3 a, Vec3 b, float alpha) {
        band(consumer, matrix, a, b, side(a, b, cam), 0.035F, PYRAMID_EDGE, alpha);
    }

    // the direction across the line from a to b, as seen from the camera; zero when the camera is on the line, which
    // collapses the strip so it draws nothing
    private static Vec3 side(Vec3 a, Vec3 b, Vec3 cam) {
        return b.subtract(a).cross(cam.subtract(a.add(b).scale(0.5))).normalize();
    }

    // a strip from a to b, brightest along the line and fading to nothing at both edges
    public static void band(VertexConsumer consumer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 side, float half, Vector3f color, float alpha) {
        band(consumer, matrix, a, b, side, half, color, alpha, alpha);
    }

    // the same strip with its own brightness at each end
    public static void band(VertexConsumer consumer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 side, float half, Vector3f color, float a0, float a1) {
        Vec3 offset = side.scale(half);
        quad(consumer, matrix, a.subtract(offset), a, b, b.subtract(offset), color, 0.0F, a0, a1, 0.0F);
        quad(consumer, matrix, a, a.add(offset), b.add(offset), b, color, a0, 0.0F, 0.0F, a1);
    }

    // a glow facing the camera: a diamond bright in the middle, fading out to its four points
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

    // colour per corner (the glow type isn't culled, so one winding shows from both sides)
    private static void quad(VertexConsumer consumer, Matrix4f matrix, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
                             float[] c0, float[] c1, float[] c2, float[] c3) {
        vertex(consumer, matrix, p0, c0[0], c0[1], c0[2], c0[3]);
        vertex(consumer, matrix, p1, c1[0], c1[1], c1[2], c1[3]);
        vertex(consumer, matrix, p2, c2[0], c2[1], c2[2], c2[3]);
        vertex(consumer, matrix, p3, c3[0], c3[1], c3[2], c3[3]);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
                             Vector3f color, float a0, float a1, float a2, float a3) {
        vertex(consumer, matrix, p0, color.x(), color.y(), color.z(), a0);
        vertex(consumer, matrix, p1, color.x(), color.y(), color.z(), a1);
        vertex(consumer, matrix, p2, color.x(), color.y(), color.z(), a2);
        vertex(consumer, matrix, p3, color.x(), color.y(), color.z(), a3);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 p, float r, float g, float b, float a) {
        consumer.vertex(matrix, (float) p.x, (float) p.y, (float) p.z).color(r, g, b, a).endVertex();
    }

    // a small puff of coloured sparks
    public static void sparkle(Vec3 at, Vector3f color, int count, float spread) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Vector3f glow = ColorUtil.lighten(color, 0.45F);
        for (int i = 0; i < Math.max(1, count); i++) {
            Vec3 p = at.add(PrismParticles.spread(spread), PrismParticles.spread(spread), PrismParticles.spread(spread));
            PrismParticles.spawn(ModParticles.PRISM_SPARK.get(), p, PrismParticles.randomDirection(1.0, 0.0).scale(0.03), glow);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        BEAMS.clear();
        PRISMS.clear();
        SEEDS.clear();
    }
}
