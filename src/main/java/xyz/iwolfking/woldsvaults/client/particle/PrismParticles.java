package xyz.iwolfking.woldsvaults.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * The glowing particles Arcane's lasers shed, drawn full bright and additively and tinted by whoever spawns them
 * (setColor): twinkling star sparks and a flare that blooms and fades, whose spawn speed carries its size instead of a
 * velocity. Also the helpers that spawn tinted particles.
 */
public final class PrismParticles {
    // Particles drawn with additive blending (light adds up, black is see-through), without writing depth.
    public static final ParticleRenderType ADDITIVE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textures) {
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "woldsvaults:additive";
        }
    };

    private static final Random RANDOM = new Random();

    private PrismParticles() {
    }

    public static void spawn(ParticleOptions type, Vec3 at, Vec3 velocity, Vector3f color) {
        Particle particle = Minecraft.getInstance().particleEngine.createParticle(type, at.x, at.y, at.z, velocity.x, velocity.y, velocity.z);
        if (particle != null) {
            particle.setColor(color.x(), color.y(), color.z());
        }
    }

    // flares and rings take their size in place of a velocity
    public static void spawnSized(ParticleOptions type, Vec3 at, double size, Vector3f color) {
        spawn(type, at, new Vec3(size, 0.0, 0.0), color);
    }

    public static double spread(double amount) {
        return (RANDOM.nextDouble() * 2.0 - 1.0) * amount;
    }

    // a random direction, its height squashed by upScale and lifted by upBias so pieces mostly fly up and out
    public static Vec3 randomDirection(double upScale, double upBias) {
        double theta = RANDOM.nextDouble() * Math.PI * 2.0;
        double y = RANDOM.nextDouble() * 2.0 - 1.0;
        double r = Math.sqrt(1.0 - y * y);
        return new Vec3(Math.cos(theta) * r, Math.abs(y) * upScale + upBias, Math.sin(theta) * r).normalize();
    }

    abstract static class FullBright extends TextureSheetParticle {
        FullBright(ClientLevel level, double x, double y, double z) {
            super(level, x, y, z);
        }

        @Override
        protected int getLightColor(float partialTicks) {
            return LightTexture.FULL_BRIGHT;
        }
    }

    // A glowing spark that twinkles as it drifts, or (as a flare) a soft glow that blooms out and fades.
    public static class Glow extends FullBright {
        private final boolean flare;
        private final float baseSize;

        public Glow(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, boolean flare) {
            super(level, x, y, z);
            this.flare = flare;
            this.pickSprite(sprites);
            this.hasPhysics = false;
            if (flare) {
                this.lifetime = 9 + this.random.nextInt(4);
                this.quadSize = (float) Math.max(0.2, xd);
            } else {
                this.xd = xd;
                this.yd = yd;
                this.zd = zd;
                this.lifetime = 14 + this.random.nextInt(18);
                this.quadSize = 0.06F + this.random.nextFloat() * 0.07F;
            }
            this.baseSize = this.quadSize;
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
            if (!this.flare) {
                this.move(this.xd, this.yd, this.zd);
                this.xd *= 0.9;
                this.yd = this.yd * 0.9 + 0.002;
                this.zd *= 0.9;
            }
            float t = this.age / (float) this.lifetime;
            this.alpha = this.flare ? (float) Math.pow(1.0F - t, 1.5) : t > 0.6F ? (1.0F - t) / 0.4F : 1.0F;
        }

        @Override
        public float getQuadSize(float partialTicks) {
            float t = Mth.clamp((this.age + partialTicks) / this.lifetime, 0.0F, 1.0F);
            if (this.flare) {
                return this.baseSize * (0.6F + 1.6F * (1.0F - (1.0F - t) * (1.0F - t)));
            }
            return this.baseSize * (0.75F + 0.35F * Mth.sin((this.age + partialTicks) * 1.4F)) * (1.0F - t * 0.5F);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ADDITIVE;
        }
    }
}
