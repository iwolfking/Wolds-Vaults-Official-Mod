package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.particle.CrystalParticles;
import xyz.iwolfking.woldsvaults.client.particle.ElixirOrbParticle;
import xyz.iwolfking.woldsvaults.client.particle.PrismParticles;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, WoldsVaults.MOD_ID);


    public static final RegistryObject<SimpleParticleType> ELIXIR_ORB = REGISTRY.register("elixir_orb", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PRISM_SPARK = REGISTRY.register("prism_spark", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PRISM_FLARE = REGISTRY.register("prism_flare", () -> new SimpleParticleType(true));
    // Simon's Set: tumbling crystal shards and a ground shockwave ring
    public static final RegistryObject<SimpleParticleType> CRYSTAL_SHARD = REGISTRY.register("crystal_shard", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PRISM_RING = REGISTRY.register("prism_ring", () -> new SimpleParticleType(true));


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event) {
        ParticleEngine particleManager = Minecraft.getInstance().particleEngine;

        particleManager.register(ELIXIR_ORB.get(), ElixirOrbParticle.Provider::new);
        particleManager.register(PRISM_SPARK.get(), sprites -> (type, level, x, y, z, xd, yd, zd) -> new PrismParticles.Glow(level, x, y, z, xd, yd, zd, sprites, false));
        particleManager.register(PRISM_FLARE.get(), sprites -> (type, level, x, y, z, xd, yd, zd) -> new PrismParticles.Glow(level, x, y, z, xd, yd, zd, sprites, true));
        particleManager.register(CRYSTAL_SHARD.get(), sprites -> (type, level, x, y, z, xd, yd, zd) -> new CrystalParticles.Shard(level, x, y, z, xd, yd, zd, sprites));
        // xd is the ring's radius
        particleManager.register(PRISM_RING.get(), sprites -> (type, level, x, y, z, xd, yd, zd) -> new CrystalParticles.Ring(level, x, y, z, xd, sprites));
    }
}
