package xyz.iwolfking.woldsvaults.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.AccessorParticleEngine;

import javax.annotation.Nullable;
import java.util.List;

public class ElixirOrbParticle extends TextureSheetParticle {
    private int size = 11;
    private boolean stoppedByCollision;
    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square((double)100.0F);

    protected ElixirOrbParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ);
        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;
        this.rCol = 0.635F;
        this.gCol = 0F;
        this.bCol = 1.0F;
        this.setSprite(pSprites.get(this.random));
        this.lifetime = 80;
        this.gravity = 0.02F;

    }

    public ElixirOrbParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, int size) {
        super(pLevel, pX, pY, pZ);
        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;
        this.rCol = 0.635F;
        this.gCol = 0F;
        this.bCol = 1.0F;
        this.lifetime = 80;
        this.gravity = 0.02F;
        sprite = ((AccessorParticleEngine) Minecraft.getInstance().particleEngine).getTextureAtlas().getSprite(WoldsVaults.id("particle/elixir_" + size));
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();

        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        setAlpha(1F - (float) age / getLifetime());
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        if (!this.stoppedByCollision) {
            double d0 = pX;
            double d1 = pY;
            double d2 = pZ;
            if (this.hasPhysics && (pX != 0.0F || pY != 0.0F || pZ != 0.0F) && pX * pX + pY * pY + pZ * pZ < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
                Vec3 vec3 = Entity.collideBoundingBox((Entity)null, new Vec3(pX, pY, pZ), this.getBoundingBox(), this.level, List.of());
                pX = vec3.x;
                pY = vec3.y;
                pZ = vec3.z;
            }

            if (pX != 0.0F || pY != 0.0F || pZ != 0.0F) {
                this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
                this.setLocationFromBoundingbox();
            }

            if (Math.abs(d1) >= 1.0E-5F && Math.abs(pY) < 1.0E-5F) {
                this.stoppedByCollision = true;
            }

            this.onGround = d1 != pY && d1 < 0.0F;
            boolean hitCeiling = d1 != pY && d1 > 0.0D;

            if (hitCeiling) this.yd = Math.min(this.yd, -0.02D);

            if (d0 != pX) {
                this.xd = 0.0F;
            }

            if (d2 != pZ) {
                this.zd = 0.0F;
            }
        }
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        return super.getQuadSize(pScaleFactor);
        //return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            sprites = pSprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ElixirOrbParticle(worldIn, x + 0.5, y + 0.5, z + 0.5, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
