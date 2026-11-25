package xyz.iwolfking.woldsvaults.network.message;

import iskallia.vault.client.particles.ChainingParticle;
import iskallia.vault.init.ModParticles;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class ClientboundAlchemySecondParticleMessage {
   public final BlockPos pos;
   public final int color;

   public ClientboundAlchemySecondParticleMessage(BlockPos pos, int color) {
      this.pos = pos;
      this.color = color;
   }

   public static void encode(ClientboundAlchemySecondParticleMessage message, FriendlyByteBuf buffer) {
      buffer.writeBlockPos(message.pos);
      buffer.writeInt(message.color);
   }

   public static ClientboundAlchemySecondParticleMessage decode(FriendlyByteBuf buffer) {
      return new ClientboundAlchemySecondParticleMessage(buffer.readBlockPos(), buffer.readInt());
   }

   public static void handle(ClientboundAlchemySecondParticleMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> renderParticles(message));
      context.setPacketHandled(true);
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderParticles(ClientboundAlchemySecondParticleMessage message) {
      Level level = Minecraft.getInstance().level;
      renderParticlesSecond(message.pos, level, message.color);
   }

   @OnlyIn(Dist.CLIENT)
   public static void renderParticlesSecond(BlockPos pos, Level level, int color) {
      for (int i = 0; i < 30; i++) {
         Random random = level.getRandom();
         float rotation = random.nextFloat() * 360.0F;
         float length = random.nextFloat() / 5.0F + 0.1F;
         Vec3 offset = new Vec3(Math.cos(rotation) * length, 0.0, Math.sin(rotation) * length);
         if (Minecraft.getInstance()
            .particleEngine
            .createParticle(
                    ModParticles.CHAINING.get(),
               pos.getX() + 0.5 + offset.x / 20.0,
               pos.getY() + 0.9 + (-0.05F + random.nextFloat() * 0.1F),
               pos.getZ() + 0.5,
               0.0,
               0.0,
               0.0
            ) instanceof ChainingParticle alchemyTableParticle) {
            alchemyTableParticle.setColor((color >>> 16 & 0xFF) / 255.0F, (color >>> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
            alchemyTableParticle.setParticleSpeed(offset.x / 3.0, -0.01F + random.nextFloat() * 0.02F, offset.z / 3.0);
            alchemyTableParticle.setLifetime(10);
         }
      }
   }
}