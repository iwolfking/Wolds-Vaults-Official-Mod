package xyz.iwolfking.woldsvaults.client.simon;

import com.mojang.math.Vector3f;
import iskallia.vault.client.render.EarthquakeRippleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.client.arcane.GlowShapes;
import xyz.iwolfking.woldsvaults.client.init.ModParticles;
import xyz.iwolfking.woldsvaults.network.message.SimonFxMessage;

import java.util.Random;

import static xyz.iwolfking.woldsvaults.client.particle.PrismParticles.randomDirection;
import static xyz.iwolfking.woldsvaults.client.particle.PrismParticles.spawn;
import static xyz.iwolfking.woldsvaults.client.particle.PrismParticles.spawnSized;
import static xyz.iwolfking.woldsvaults.client.particle.PrismParticles.spread;

/**
 * Client side of {@link SimonFxMessage}: crystals coming out of the ground, bursting and the finale,
 * drawn with the set's own particles (tinted crystal shards, glowing sparks, flares and shockwave rings) rather than
 * vanilla smoke and dust.
 */
public final class SimonFx {
    // half the width of each stage across its base, in blocks
    private static final float[] BASE_HALF = {0.15F, 0.25F, 0.4F, 0.55F};
    private static final Random RANDOM = new Random();

    // the colour at a point along a blast's palette, lifted towards white by whiten
    private interface Tone {
        Vector3f at(float position, float whiten);
    }

    private SimonFx() {
    }

    public static void play(SimonFxMessage.Kind kind, double x, double y, double z, Direction face, int stage, int rgb, float size) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Vector3f color = ColorUtil.vector(rgb);
        Vec3 at = new Vec3(x, y, z);
        float radius = Math.max(2.0F, size);
        int power = Math.max(0, stage);
        switch (kind) {
            case EMERGE -> emerge(level, at, face, Mth.clamp(stage, 0, 3), color);
            case SHATTER -> burst(at, Mth.clamp(stage, 0, 3), color, 1.0F, size);
            case PULSE -> burst(at, Mth.clamp(stage, 0, 3), color, 0.45F, size);
            // the stage carries the Prismatic Geodes in the chain, the colour the chain's colours blended
            case FINALE -> explosion(at, radius, rgb, power, power, SimonFx::hue);
            case BLAST -> {
                // a Bullet Bill's detonation: the finale's blast in fire colours, with smoke rolling out of it
                explosion(at, radius, rgb, power, 0, SimonFx::fire);
                smoke(level, at, radius);
            }
            case FACET -> facet(at, color, Math.max(1, stage), size);
        }
    }

    // The blast's particles and what it does to those near it. Power adds to the blast and its jolt; rainbow is the
    // count of Prismatic Geodes that turn its flash into a rainbow.
    private static void explosion(Vec3 at, float radius, int color, int power, int rainbow, Tone tone) {
        finale(at, radius, power, tone);
        BlastFeedback.flash(at, radius, color, rainbow);
        BlastFeedback.shake(at, radius, power);
        // the ground heaves as the shockwave passes, as under Vault Hunters' Earthquake: surface blocks lift
        // a little and drop back in a ring racing out to the blast's edge
        EarthquakeRippleRenderer.addRipple(at, radius, 0, false);
    }

    // debris of the block it grows from kicked up round the base, little shards popping out and sparks rising with it
    private static void emerge(ClientLevel level, Vec3 base, Direction face, int stage, Vector3f color) {
        Vec3 up = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 across = face.getAxis() == Direction.Axis.Y ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 t1 = up.cross(across).normalize();
        Vec3 t2 = up.cross(t1).normalize();
        BlockState ground = level.getBlockState(new BlockPos(base.subtract(up.scale(0.1))));
        if (ground.isAir()) {
            ground = Blocks.AMETHYST_BLOCK.defaultBlockState();
        }
        BlockParticleOption debris = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        Vector3f light = ColorUtil.lighten(color, 0.35F);
        Vector3f glow = ColorUtil.lighten(color, 0.55F);
        float half = BASE_HALF[stage];
        int count = 10 + stage * 10;
        for (int i = 0; i < count; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0;
            Vec3 out = t1.scale(Math.cos(angle)).add(t2.scale(Math.sin(angle)));
            Vec3 at = base.add(out.scale(half * (0.6 + RANDOM.nextDouble() * 0.7))).add(up.scale(0.05));
            Vec3 v = up.scale(0.12 + RANDOM.nextDouble() * 0.2).add(out.scale(0.05 + RANDOM.nextDouble() * 0.08));
            level.addParticle(debris, at.x, at.y, at.z, v.x, v.y, v.z);
            if (i % 3 == 0) {
                spawn(ModParticles.CRYSTAL_SHARD.get(), at, v.scale(0.8), light);
            }
            if (i % 2 == 0) {
                Vec3 s = at.add(up.scale(RANDOM.nextDouble() * half * 2.0));
                spawn(ModParticles.PRISM_SPARK.get(), s, up.scale(0.02 + RANDOM.nextDouble() * 0.04), glow);
            }
        }
    }

    // a grenade of crystal: a bloom of light, a shockwave ring out to the blast's edge, shards flung on arcs and sparks
    private static void burst(Vec3 at, int stage, Vector3f color, float scale, float radius) {
        Vector3f light = ColorUtil.lighten(color, 0.3F);
        Vector3f glow = ColorUtil.lighten(color, 0.5F);
        spawnSized(ModParticles.PRISM_FLARE.get(), at, (1.2 + stage * 0.5) * (0.5 + 0.5 * scale), glow);
        if (radius > 0.0F) {
            spawnSized(ModParticles.PRISM_RING.get(), at.add(0.0, -0.3, 0.0), radius * (scale >= 1.0F ? 1.0F : 0.6F), glow);
        }
        int shards = (int) ((28 + stage * 14) * scale);
        double speed = (0.42 + stage * 0.1) * (0.6 + 0.4 * scale);
        for (int i = 0; i < shards; i++) {
            Vec3 dir = randomDirection(0.85, 0.15);
            spawn(ModParticles.CRYSTAL_SHARD.get(), at, dir.scale(speed * (0.6 + RANDOM.nextDouble() * 0.6)), light);
        }
        int sparks = (int) ((22 + stage * 8) * scale) + 4;
        for (int i = 0; i < sparks; i++) {
            Vec3 dir = randomDirection(1.0, 0.0);
            spawn(ModParticles.PRISM_SPARK.get(), at, dir.scale(0.25 + RANDOM.nextDouble() * 0.35), glow);
        }
        BlockParticleOption chips = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_CLUSTER.defaultBlockState());
        for (int i = 0; i < (int) (10 * scale); i++) {
            Vec3 dir = randomDirection(0.8, 0.2);
            double v = speed * 0.6;
            Minecraft.getInstance().particleEngine.createParticle(chips, at.x, at.y, at.z, dir.x * v, dir.y * v, dir.z * v);
        }
    }

    // A Geode gaining a facet: a bright flare in its colour, a ring snapping out round it at the facet's height and
    // sparks spiralling in, one more ring for each facet it has.
    private static void facet(Vec3 c, Vector3f color, int facets, float width) {
        Vector3f glow = ColorUtil.lighten(color, 0.4F);
        spawnSized(ModParticles.PRISM_FLARE.get(), c, 1.6 + facets * 0.3, glow);
        for (int ring = 0; ring < facets; ring++) {
            spawnSized(ModParticles.PRISM_RING.get(), c.add(0.0, -0.4 + ring * 0.4, 0.0), width * 0.6 + 1.0 + ring * 0.4, glow);
        }
        for (int i = 0; i < 24; i++) {
            double angle = i * Math.PI * 2.0 / 24;
            Vec3 at = c.add(Math.cos(angle) * 1.6, spread(0.6), Math.sin(angle) * 1.6);
            // in towards the crystal and round it
            Vec3 in = c.subtract(at).scale(0.08).add(-Math.sin(angle) * 0.06, 0.02, Math.cos(angle) * 0.06);
            spawn(ModParticles.PRISM_SPARK.get(), at, in, i % 3 == 0 ? GlowShapes.WHITE : glow);
        }
    }

    // The finale: blooms of light, rainbow shockwave rings racing out to the blast's edge, a pillar of sparks, a
    // rainbow storm of crystal shards flung high and wide, and crackling electricity. Each Prismatic Geode in the chain
    // adds rings, flares and sparks (the radius it is given has already doubled for each).
    private static void finale(Vec3 c, float radius, int prismaticGeodes, Tone tone) {
        int extra = Math.min(prismaticGeodes, 3);
        spawnSized(ModParticles.PRISM_FLARE.get(), c, 4.5 + extra * 2.5, GlowShapes.WHITE);
        for (int i = 0; i < 4 * extra; i++) {
            Vec3 at = c.add(spread(radius * 0.3), spread(radius * 0.15), spread(radius * 0.3));
            spawnSized(ModParticles.PRISM_FLARE.get(), at, 2.5 + RANDOM.nextDouble() * 2.0, tone.at(RANDOM.nextFloat(), 0.3F));
        }
        for (int ring = 3; ring < 3 + 2 * extra; ring++) {
            spawnSized(ModParticles.PRISM_RING.get(), c.add(0.0, -0.4 + ring * 0.3, 0.0), radius * (1.0F - (ring - 3) * 0.12F), tone.at(ring / 7.0F, 0.3F));
        }
        for (int i = 0; i < 80 * extra; i++) {
            Vec3 dir = randomDirection(0.9, 0.0);
            spawn(ModParticles.PRISM_SPARK.get(), c, dir.scale(0.5 + RANDOM.nextDouble() * radius / 8.0), tone.at(RANDOM.nextFloat(), 0.4F));
        }
        for (int i = 0; i < 3; i++) {
            Vec3 at = c.add(spread(1.2), spread(0.8), spread(1.2));
            spawnSized(ModParticles.PRISM_FLARE.get(), at, 2.0 + RANDOM.nextDouble() * 1.5, tone.at(RANDOM.nextFloat(), 0.4F));
        }
        for (int ring = 0; ring < 3; ring++) {
            spawnSized(ModParticles.PRISM_RING.get(), c.add(0.0, -0.4 + ring * 0.35, 0.0), radius * (1.0F - ring * 0.25F), tone.at(ring / 3.0F, 0.45F));
        }
        for (int i = 0; i < 60; i++) {
            Vec3 at = c.add(spread(0.5), 0.0, spread(0.5));
            spawn(ModParticles.PRISM_SPARK.get(), at, new Vec3(0.0, 0.3 + RANDOM.nextDouble() * 1.1, 0.0), GlowShapes.WHITE);
        }
        for (int i = 0; i < 140; i++) {
            Vec3 dir = randomDirection(0.9, 0.0);
            spawn(ModParticles.PRISM_SPARK.get(), c, dir.scale(0.4 + RANDOM.nextDouble() * radius / 10.0), tone.at(RANDOM.nextFloat(), 0.45F));
        }
        double force = 0.6 + radius / 12.0;
        for (int i = 0; i < 130; i++) {
            Vec3 dir = randomDirection(1.0, 0.3);
            spawn(ModParticles.CRYSTAL_SHARD.get(), c, dir.scale(force * (0.5 + RANDOM.nextDouble() * 0.8)), tone.at(RANDOM.nextFloat(), 0.3F));
        }
        for (int i = 0; i < 40; i++) {
            Vec3 dir = randomDirection(1.0, 0.0);
            Vec3 at = c.add(dir.scale(RANDOM.nextDouble() * radius * 0.6));
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, dir.x * 0.3, dir.y * 0.3, dir.z * 0.3);
        }
    }

    // black smoke, fire and a few bursts rolling out from the middle of a detonation
    private static void smoke(ClientLevel level, Vec3 c, float radius) {
        for (int i = 0; i < 40 + (int) (radius * 4.0F); i++) {
            Vec3 dir = randomDirection(0.8, 0.2);
            Vec3 at = c.add(dir.scale(RANDOM.nextDouble() * radius * 0.4));
            double speed = 0.05 + RANDOM.nextDouble() * radius / 40.0;
            level.addParticle(i % 3 == 0 ? ParticleTypes.FLAME : ParticleTypes.LARGE_SMOKE, true, at.x, at.y, at.z, dir.x * speed, dir.y * speed + 0.02, dir.z * speed);
        }
        for (int i = 0; i < 6; i++) {
            level.addParticle(ParticleTypes.EXPLOSION, true, c.x + spread(radius * 0.5), c.y + spread(radius * 0.3), c.z + spread(radius * 0.5), 0.0, 0.0, 0.0);
        }
    }

    // yellow through orange to deep red as the position runs from 0 to 1
    private static Vector3f fire(float position, float whiten) {
        float t = position - (float) Math.floor(position);
        return ColorUtil.lighten(new Vector3f(1.0F, 0.85F - 0.7F * t, 0.3F - 0.25F * t), whiten);
    }

    private static Vector3f hue(float hue, float whiten) {
        return ColorUtil.lighten(ColorUtil.vector(Mth.hsvToRgb(hue, 0.8F, 1.0F)), whiten);
    }
}
