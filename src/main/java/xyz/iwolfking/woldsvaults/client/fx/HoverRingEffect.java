package xyz.iwolfking.woldsvaults.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The storm halo around a player in the post-dash hover: a steady teal ring at the waist, a dashed violet ring
 * turning the other way on a slight tilt, white beads running around the inner ring, and now and then a spark
 * crawling between the two. Lives as long as the player's hover stance, fading in and out with it.
 */
@OnlyIn(Dist.CLIENT)
final class HoverRingEffect implements RiptideFx.Effect {
    private static final int SEGMENTS = 40;
    private static final int DASHES = 6;
    private static final int BEADS = 3;
    private static final float INNER_RADIUS = 0.85F;
    private static final float OUTER_RADIUS = 1.1F;
    private static final float HEIGHT_FRACTION = 0.45F;

    private final int entityId;
    private final float phase;
    private int age;

    HoverRingEffect(int entityId, float phase) {
        this.entityId = entityId;
        this.phase = phase;
    }

    int getEntityId() {
        return this.entityId;
    }

    @Override
    public boolean tick(ClientLevel level) {
        this.age++;
        if (!HoverStance.isActive(level, this.entityId)) {
            return false;
        }
        Entity entity = level.getEntity(this.entityId);
        if (entity == null) {
            return false;
        }
        Random random = level.random;
        float blend = HoverStance.blend(this.entityId, 1.0F);
        if (blend < 0.5F) {
            return true;
        }
        Vec3 center = entity.position().add(0.0, entity.getBbHeight() * HEIGHT_FRACTION, 0.0);
        if (random.nextFloat() < 0.3F) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double b = a + (random.nextDouble() - 0.5) * 1.2;
            Vec3 offsetA = new Vec3(Math.cos(a) * INNER_RADIUS, entity.getBbHeight() * HEIGHT_FRACTION, Math.sin(a) * INNER_RADIUS);
            Vec3 offsetB = new Vec3(Math.cos(b) * OUTER_RADIUS, entity.getBbHeight() * HEIGHT_FRACTION + 0.05, Math.sin(b) * OUTER_RADIUS);
            RiptideFx.add(new LightningArcEffect(this.entityId, offsetA, entity.position().add(offsetA), this.entityId, offsetB,
                    entity.position().add(offsetB), false, 3, 1, 0.3F, false, random.nextLong()));
        }
        if (RiptideFx.particleCount(2) > 0 && this.age % 2 == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            Vec3 onRing = center.add(Math.cos(a) * OUTER_RADIUS, 0.0, Math.sin(a) * OUTER_RADIUS);
            level.addParticle(ParticleTypes.ENCHANT, center.x, center.y + 0.3, center.z, onRing.x - center.x, onRing.y - center.y, onRing.z - center.z);
            level.addParticle(random.nextBoolean() ? ParticleTypes.GLOW : ParticleTypes.REVERSE_PORTAL, onRing.x, onRing.y, onRing.z, 0.0, 0.01, 0.0);
        }
        return true;
    }

    @Override
    public void renderGlow(VertexConsumer consumer, Matrix4f pose, Vec3 camera, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(this.entityId);
        float blend = HoverStance.blend(this.entityId, partialTick);
        if (entity == null || blend <= 0.0F) {
            return;
        }
        float fade = blend * blend * (3.0F - 2.0F * blend);
        float time = this.age + partialTick;
        float pulse = 0.85F + 0.15F * (float) Math.sin(time * 0.35F);
        Vec3 center = entity.getPosition(partialTick).add(0.0, entity.getBbHeight() * HEIGHT_FRACTION, 0.0).subtract(camera);
        double spin = time * 0.09 + this.phase * Math.PI * 2.0;

        float scale = 0.7F + 0.3F * fade;
        List<Vec3> inner = arc(center, INNER_RADIUS * scale, 0.0, Math.PI * 2.0, SEGMENTS, 0.0, 0.0);
        FxGeometry.ribbon(consumer, pose, inner, 0.14F, 0.14F, FxGeometry.rgba(RiptideFx.TEAL, 0.3F * fade * pulse), FxGeometry.rgba(RiptideFx.TEAL, 0.3F * fade * pulse));
        FxGeometry.ribbon(consumer, pose, inner, 0.035F, 0.035F, FxGeometry.rgba(RiptideFx.WHITE, 0.55F * fade), FxGeometry.rgba(RiptideFx.WHITE, 0.55F * fade));

        double tilt = 0.12 + 0.05 * Math.sin(time * 0.07);
        double span = Math.PI * 2.0 / DASHES * 0.55;
        for (int i = 0; i < DASHES; i++) {
            double start = -spin * 1.3 + Math.PI * 2.0 * i / DASHES;
            List<Vec3> dash = arc(center, OUTER_RADIUS * scale, start, start + span, 8, tilt, spin * 0.5);
            FxGeometry.ribbon(consumer, pose, dash, 0.02F, 0.09F, FxGeometry.rgba(RiptideFx.VIOLET, 0.0F), FxGeometry.rgba(RiptideFx.VIOLET, 0.7F * fade * pulse));
        }

        for (int i = 0; i < BEADS; i++) {
            double head = spin * 2.2 + Math.PI * 2.0 * i / BEADS;
            List<Vec3> bead = arc(center, INNER_RADIUS * scale, head - 0.7, head, 8, 0.0, 0.0);
            FxGeometry.ribbon(consumer, pose, bead, 0.0F, 0.1F, FxGeometry.rgba(RiptideFx.TEAL, 0.0F), FxGeometry.rgba(RiptideFx.WHITE, 0.9F * fade));
        }
    }

    /** Points on a horizontal circle around {@code center}, optionally tilted about an axis that turns with {@code tiltYaw}. */
    private static List<Vec3> arc(Vec3 center, double radius, double from, double to, int steps, double tilt, double tiltYaw) {
        List<Vec3> points = new ArrayList<>(steps + 1);
        double tiltX = Math.cos(tiltYaw);
        double tiltZ = Math.sin(tiltYaw);
        for (int i = 0; i <= steps; i++) {
            double a = from + (to - from) * i / steps;
            double x = Math.cos(a) * radius;
            double z = Math.sin(a) * radius;
            double lift = (x * tiltZ - z * tiltX) * Math.sin(tilt);
            points.add(center.add(x, lift, z));
        }
        return points;
    }
}
