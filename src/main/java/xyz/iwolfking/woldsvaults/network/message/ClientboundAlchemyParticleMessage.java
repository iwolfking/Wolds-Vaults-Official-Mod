package xyz.iwolfking.woldsvaults.network.message;

import iskallia.vault.client.particles.AlchemyTableParticle;
import iskallia.vault.init.ModParticles;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class ClientboundAlchemyParticleMessage {
   public final BlockPos pos;
   public final Direction dir;
   public final int color;
   public final float yOffset;

   public ClientboundAlchemyParticleMessage(BlockPos pos, Direction dir, int color, float yOffset) {
      this.pos = pos;
      this.dir = dir;
      this.color = color;
      this.yOffset = yOffset;
   }

   public static void encode(ClientboundAlchemyParticleMessage message, FriendlyByteBuf buffer) {
      buffer.writeBlockPos(message.pos);
      buffer.writeEnum(message.dir);
      buffer.writeInt(message.color);
      buffer.writeFloat(message.yOffset);
   }

   public static ClientboundAlchemyParticleMessage decode(FriendlyByteBuf buffer) {
      return new ClientboundAlchemyParticleMessage(buffer.readBlockPos(), (Direction)buffer.readEnum(Direction.class), buffer.readInt(), buffer.readFloat());
   }

   public static void handle(ClientboundAlchemyParticleMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> renderParticles(message));
      context.setPacketHandled(true);
   }

   @OnlyIn(Dist.CLIENT)
   private static void renderParticles(ClientboundAlchemyParticleMessage message) {
      renderParticlesFirst(message.pos, message.dir, message.color, message.yOffset);
   }

   public static void renderParticlesFirst(BlockPos pos, Direction dir, int color, float yOffset) {
      int rot = 0;
      if (dir == Direction.WEST) {
         rot = 90;
      }

      if (dir == Direction.SOUTH) {
         rot = 180;
      }

      if (dir == Direction.EAST) {
         rot = 270;
      }

      Vec3 vec3 = new Vec3(0.325F, 0.0, 0.325F).yRot((float)Math.toRadians(rot));
      if (Minecraft.getInstance()
         .particleEngine
         .createParticle(
                 ModParticles.ALCHEMY_TABLE.get(),
            pos.getX() + 0.5 + vec3.x(),
            pos.getY() + 1.1,
            pos.getZ() + 0.5 + vec3.z(),
            pos.getX() + 0.5,
            pos.getY() + 0.7 + yOffset,
            pos.getZ() + 0.5
         ) instanceof AlchemyTableParticle alchemyTableParticle) {
         alchemyTableParticle.setColorStart(0.9F, 0.0F, 0.5F);
         alchemyTableParticle.setColorEnd((color >>> 16 & 0xFF) / 255.0F, (color >>> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
      }

      vec3 = new Vec3(-0.325F, 0.0, -0.325F).yRot((float)Math.toRadians(rot));
      if (Minecraft.getInstance()
         .particleEngine
         .createParticle(
            (ParticleOptions)ModParticles.ALCHEMY_TABLE.get(),
            pos.getX() + 0.5 + vec3.x(),
            pos.getY() + 1.2,
            pos.getZ() + 0.5 + vec3.z(),
            pos.getX() + 0.5,
            pos.getY() + 0.7 + yOffset,
            pos.getZ() + 0.5
         ) instanceof AlchemyTableParticle alchemyTableParticle) {
         alchemyTableParticle.setColorStart(0.0F, 0.9F, 0.4F);
         alchemyTableParticle.setColorEnd((color >>> 16 & 0xFF) / 255.0F, (color >>> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
      }
   }
}