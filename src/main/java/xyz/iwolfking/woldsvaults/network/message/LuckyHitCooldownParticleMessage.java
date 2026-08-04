package xyz.iwolfking.woldsvaults.network.message;

import iskallia.vault.client.particles.ShockedParticle;
import iskallia.vault.init.ModParticles;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class LuckyHitCooldownParticleMessage {
    private final Vec3 targetPos;
    private final int entityID;
    private final int count;

    public LuckyHitCooldownParticleMessage(Vec3 targetPos, int entityID, int count) {
        this.targetPos = targetPos;
        this.entityID = entityID;
        this.count = count;
    }

    public static void encode(LuckyHitCooldownParticleMessage message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.targetPos.x);
        buffer.writeDouble(message.targetPos.y);
        buffer.writeDouble(message.targetPos.z);
        buffer.writeInt(message.entityID);
        buffer.writeInt(message.count);
    }

    public static LuckyHitCooldownParticleMessage decode(FriendlyByteBuf buffer) {
        return new LuckyHitCooldownParticleMessage(
            new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
            buffer.readInt(),
            buffer.readInt()
        );
    }

    public static void handle(LuckyHitCooldownParticleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                spawnParticles(message.targetPos, message.entityID, message.count);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnParticles(Vec3 pos, int entityID, int count) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Random random = new Random();
        ParticleEngine pe = Minecraft.getInstance().particleEngine;
        Entity entity = level.getEntity(entityID);

        if (entity instanceof LivingEntity livingEntity) {
            for (int i = 0; i < count; ++i) {
                double theta = random.nextDouble() * 2.0 * Math.PI;
                double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
                double speed = 0.25 + (random.nextDouble() * 0.35);

                double vx = speed * Math.sin(phi) * Math.cos(theta);
                double vy = speed * Math.cos(phi);
                double vz = speed * Math.sin(phi) * Math.sin(theta);

                Particle particle = pe.createParticle(
                        ModParticles.LUCKY_HIT_DAMAGE.get(),
                    pos.x(), pos.y(), pos.z(),
                    vx, vy, vz
                );

                if (particle instanceof ShockedParticle shockedParticle) {
                    shockedParticle.setLivingEntity(livingEntity);

                    float variance = random.nextFloat() * 0.15F;
                    float red = 0.55F + variance;
                    float green = 0.05F + (variance * 0.5F);
                    float blue = 0.85F + variance;

                    shockedParticle.setColor(red, green, blue);
                }
            }
        }
    }
}