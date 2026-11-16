package xyz.iwolfking.woldsvaults.client.arcane;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.client.init.ModParticles;
import xyz.iwolfking.woldsvaults.client.particle.PrismParticles;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Arcane and Arcane Rail laser beams, drawn as layered additive strips that face the camera: a wide soft glow and a
 * narrower brighter band, both fading to nothing at their edges, round a thin white-hot core, with a flare at every
 * point of the line. Each beam lives a few ticks (a held Arcane beam is renewed every tick), shimmers in width, fades
 * and thins out over the second half of its life and sheds sparks along its length.
 */
public final class LaserBeams {
    private static final List<Beam> BEAMS = new ArrayList<>();
    private static final Random RANDOM = new Random();
    @Nullable
    private static ClientLevel lastLevel;

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

    private LaserBeams() {
    }

    public static void add(List<Vec3> points, int rgb, float width, int ticks) {
        BEAMS.add(new Beam(points, rgb, width, ticks));
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
        BEAMS.removeIf(beam -> ++beam.age > beam.ticks);
        BEAMS.forEach(LaserBeams::sparks);
    }

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

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (BEAMS.isEmpty()) {
            return;
        }
        GlowShapes.render(event, (consumer, matrix, cam, left, up, level, partial) -> {
            for (Beam beam : BEAMS) {
                draw(consumer, matrix, cam, left, up, beam, partial);
            }
        });
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
            GlowShapes.laser(consumer, matrix, cam, a, b, beam.width * shimmer * (0.5F + 0.5F * fade), beam.color, fade);
        }
        for (int i = 0; i < beam.points.size(); i++) {
            float size = beam.width * (i == 0 || i == beam.points.size() - 1 ? 1.4F : 1.8F) * (1.0F + 0.15F * Mth.sin(time * 2.3F + i));
            GlowShapes.flare(consumer, matrix, beam.points.get(i), left, up, size, beam.color, 0.6F * fade);
        }
    }

    public static void clear() {
        BEAMS.clear();
    }
}
