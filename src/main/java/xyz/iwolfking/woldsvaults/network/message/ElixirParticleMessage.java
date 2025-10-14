package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.init.ModParticles;
import xyz.iwolfking.woldsvaults.client.particle.ElixirOrbParticle;

import java.util.function.Supplier;

public class ElixirParticleMessage {
    private final int size;
    private final BlockPos blockPos;

    public ElixirParticleMessage(int size, BlockPos pos) {
        this.size = size;
        this.blockPos = pos;
    }

    public static void encode(ElixirParticleMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.size);
        buffer.writeBlockPos(message.blockPos);
    }

    public static ElixirParticleMessage decode(FriendlyByteBuf buffer) {
        return new ElixirParticleMessage(buffer.readInt(), buffer.readBlockPos());
    }

    public static void handle(ElixirParticleMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
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
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            ParticleEngine pe = Minecraft.getInstance().particleEngine;

            for(int i = 0; i < Math.abs(size) / 2 + 3; i++) {
                Particle p = new ElixirOrbParticle(level, this.blockPos.getX() + 0.5, this.blockPos.getY() + 0.5, this.blockPos.getZ() + 0.5,
                        level.random.nextDouble() * 0.2D - 0.10D,
                        level.random.nextDouble() * 0.2D,
                        level.random.nextDouble() * 0.2D - 0.10D, this.size);


                if (p instanceof ElixirOrbParticle elixParticle) {
                    elixParticle.setSize(size);
                }

                pe.add(p);
            }
        }
    }
}
