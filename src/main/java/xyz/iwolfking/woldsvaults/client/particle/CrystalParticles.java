package xyz.iwolfking.woldsvaults.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Simon's Set particles, drawn full bright and tinted by whoever spawns them (setColor): crystal shards that tumble,
 * fall and bounce, and a flat shockwave ring that races out along the ground to a radius. The ring is drawn additively
 * so it glows, and its spawn speed carries the radius instead of a velocity.
 */
public final class CrystalParticles {
    private CrystalParticles() {
    }

    /** A crystal shard: flung out, spinning, falling, bouncing off the ground and fading. */
    public static class Shard extends PrismParticles.FullBright {
        private float spin;

        public Shard(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
            super(level, x, y, z);
            this.xd = xd;
            this.yd = yd;
            this.zd = zd;
            this.pickSprite(sprites);
            this.lifetime = 40 + this.random.nextInt(30);
            this.gravity = 1.0F;
            this.quadSize = 0.07F + this.random.nextFloat() * 0.1F;
            this.spin = (this.random.nextFloat() - 0.5F) * 0.7F;
            this.roll = this.random.nextFloat() * Mth.TWO_PI;
            this.oRoll = this.roll;
        }

        @Override
        public void tick() {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            if (this.age++ >= this.lifetime) {
                this.remove();
                return;
            }
            this.yd -= 0.04 * this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98;
            this.yd *= 0.98;
            this.zd *= 0.98;
            if (this.onGround) {
                this.xd *= 0.75;
                this.zd *= 0.75;
                this.spin *= 0.7F;
            }
            this.oRoll = this.roll;
            this.roll += this.spin;
            int left = this.lifetime - this.age;
            this.alpha = left < 12 ? left / 12.0F : 1.0F;
        }

        @Override
        public void move(double dx, double dy, double dz) {
            double ox = dx;
            double oy = dy;
            double oz = dz;
            if (this.hasPhysics && (dx != 0.0 || dy != 0.0 || dz != 0.0)) {
                Vec3 moved = Entity.collideBoundingBox(null, new Vec3(dx, dy, dz), this.getBoundingBox(), this.level, List.of());
                dx = moved.x;
                dy = moved.y;
                dz = moved.z;
            }
            if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
                this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
                this.setLocationFromBoundingbox();
            }
            this.onGround = oy != dy && oy < 0.0;
            if (ox != dx) {
                this.xd = -this.xd * 0.3;
            }
            if (oz != dz) {
                this.zd = -this.zd * 0.3;
            }
            if (oy != dy) {
                this.yd = oy < -0.08 ? -oy * 0.35 : 0.0;
            }
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    /** A flat ring on the ground racing out to its radius and fading as it goes. */
    public static class Ring extends PrismParticles.FullBright {
        private final float radius;

        public Ring(ClientLevel level, double x, double y, double z, double radius, SpriteSet sprites) {
            super(level, x, y, z);
            this.pickSprite(sprites);
            this.radius = (float) Math.max(0.5, radius);
            this.lifetime = Mth.clamp((int) (10 + this.radius * 1.4F), 10, 28);
            this.hasPhysics = false;
        }

        @Override
        public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
            Vec3 cam = camera.getPosition();
            float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
            float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
            float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);
            float t = Mth.clamp((this.age + partialTicks) / this.lifetime, 0.0F, 1.0F);
            float r = this.radius * (1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t));
            float a = this.alpha * (1.0F - t);
            float u0 = this.getU0();
            float u1 = this.getU1();
            float v0 = this.getV0();
            float v1 = this.getV1();
            int light = this.getLightColor(partialTicks);
            float[][] corners = {{-r, -r, u0, v0}, {-r, r, u0, v1}, {r, r, u1, v1}, {r, -r, u1, v0}};
            for (int i = 0; i < 8; i++) {
                float[] c = corners[i < 4 ? i : 7 - i];
                consumer.vertex(x + c[0], y, z + c[1]).uv(c[2], c[3]).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
            }
        }

        @Override
        public ParticleRenderType getRenderType() {
            return PrismParticles.ADDITIVE;
        }
    }
}
