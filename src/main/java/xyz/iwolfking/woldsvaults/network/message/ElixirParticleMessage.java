package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
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

    public ElixirParticleMessage(int size) {
        this.size = size;
    }

    public static void encode(ElixirParticleMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.size);

    }

    public static ElixirParticleMessage decode(FriendlyByteBuf buffer) {
        return new ElixirParticleMessage(buffer.readInt());
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
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            ParticleEngine pe = Minecraft.getInstance().particleEngine;

            for(int i = 0; i < Math.abs(size) / 2 + 3; i++) {
                Particle p = pe.createParticle(ModParticles.ELIXIR_ORB.get(), 0.2, 0.2, 0.2, 0.1, 0.1, 0.1);

                if (p instanceof ElixirOrbParticle elixParticle) {
                    elixParticle.setSize(size);
                }
            }
        }
    }
}
