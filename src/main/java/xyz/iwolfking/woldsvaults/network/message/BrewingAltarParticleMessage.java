package xyz.iwolfking.woldsvaults.network.message;

import iskallia.vault.client.particles.AbsorbingParticle;
import iskallia.vault.init.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class BrewingAltarParticleMessage {
    private final Vec3 startPos;
    private final int targetEntity;
    private final Vec3 targetPos;
    private final int color;

    public BrewingAltarParticleMessage(Vec3 startPos, int targetEntity, Vec3 targetPos, int color) {
        this.startPos = startPos;
        this.targetEntity = targetEntity;
        this.targetPos = targetPos;
        this.color = color;
    }

    public BrewingAltarParticleMessage(Vec3 startPos, Entity targetEntity, int color) {
        this.startPos = startPos;
        this.targetEntity = targetEntity.getId();
        this.targetPos = targetEntity.position();
        this.color = color;
    }

    public static void encode(BrewingAltarParticleMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.startPos.x);
        buffer.writeDouble(message.startPos.y);
        buffer.writeDouble(message.startPos.z);
        buffer.writeVarInt(message.targetEntity);
        buffer.writeDouble(message.targetPos.x);
        buffer.writeDouble(message.targetPos.y);
        buffer.writeDouble(message.targetPos.z);
        buffer.writeInt(message.color);
    }

    public static BrewingAltarParticleMessage decode(FriendlyByteBuf buffer) {
        return new BrewingAltarParticleMessage(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readVarInt(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readInt());
    }

    public static void handle(BrewingAltarParticleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                message.spawnParticles();
            }

        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public void spawnParticles() {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Random random = new Random();
            ParticleEngine pe = Minecraft.getInstance().particleEngine;

            for(int i = 0; i < 30; ++i) {
                float rotation = random.nextFloat() * 360.0F;
                float radius = 1.0F + random.nextFloat();
                Vec3 offset = new Vec3((double)(radius / 5.0F) * Math.cos(rotation), 0.0F, (double)(radius / 5.0F) * Math.sin(rotation));
                offset.scale(random.nextDouble() + (double)0.5F);
                Particle particle = pe.createParticle(ModParticles.ABSORBING.get(), this.startPos.x() + (double)0.5F, this.startPos.y() + random.nextDouble() * 0.6 + 0.15, this.startPos.z() + (double)0.5F, offset.x / (double)2.0F, random.nextDouble() * (double)0.25F + 0.1, offset.z / (double)2.0F);
                if (particle instanceof AbsorbingParticle pylonParticle) {
                    pylonParticle.setTarget(() -> {
                        Entity patt3905$temp = level.getEntity(this.targetEntity);
                        if (patt3905$temp instanceof LivingEntity entity) {
                            return entity.position().add((double)entity.getBbWidth() / (double)2.0F, (double)entity.getBbHeight() / (double)2.0F, entity.getBbWidth() / 2.0F);
                        } else {
                            return this.targetPos;
                        }
                    });
                    pylonParticle.setColor((float)(this.color >>> 16 & 255) / 255.0F, (float)(this.color >>> 8 & 255) / 255.0F, (float)(this.color & 255) / 255.0F);
                }
            }

            for(int i = 0; i < 12; ++i) {
                float rotation = random.nextFloat() * 360.0F;
                Vec3 offset = new Vec3((double)0.4F * Math.cos(rotation), 0.0F, (double)0.4F * Math.sin(rotation));
                offset.scale(random.nextDouble() + (double)0.5F);
                Particle particle = pe.createParticle(ModParticles.NOVA_CLOUD.get(), this.startPos.x() + (double)0.5F + offset.x, this.startPos.y() + random.nextDouble() * 0.15, this.startPos.z() + (double)0.5F + offset.z, offset.x / (double)8.0F, random.nextDouble() * (double)0.125F, offset.z / (double)8.0F);
                if (particle != null) {
                    particle.setLifetime(60 + random.nextInt(20));
                }

                offset = new Vec3((double)0.25F * Math.cos(rotation), 0.0F, (double)0.25F * Math.sin(rotation));
                offset.scale(random.nextDouble() + (double)0.5F);
                particle = pe.createParticle(ModParticles.NOVA_CLOUD.get(), this.startPos.x() + (double)0.5F + offset.x, this.startPos.y() + random.nextDouble() * 0.15, this.startPos.z() + (double)0.5F + offset.z, offset.x / (double)8.0F, random.nextDouble() * (double)0.125F, offset.z / (double)8.0F);
                if (particle != null) {
                    particle.setLifetime(60 + random.nextInt(20));
                }
            }

        }
    }

}
