package xyz.iwolfking.woldsvaults.init;

import com.alrex.parcool.api.Effects;
import iskallia.vault.VaultMod;
import iskallia.vault.gear.trinket.TrinketEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.effect.HeadlampTrinketEffect;
import xyz.iwolfking.woldsvaults.effect.RunningShoesTrinketEffect;

public class ModTrinkets {
    @Unique
    private static HeadlampTrinketEffect MINERS_LAMP;

    //@Unique
    private static final RunningShoesTrinketEffect RUNNING_SHOES;

    public static void init(RegistryEvent.Register<TrinketEffect<?>> event) {
        IForgeRegistry<TrinketEffect<?>> registry = event.getRegistry();
        if(WoldsVaultsConfig.COMMON.enableMinersHeadlampTrinket.get()) {
            registry.register(MINERS_LAMP);
        }

        if(WoldsVaultsConfig.COMMON.enableRunningShoesTrinket.get()) {
            registry.register(RUNNING_SHOES);
        }

    }

    static {
        MINERS_LAMP =  new HeadlampTrinketEffect(VaultMod.id("miners_headlamp"), MobEffects.NIGHT_VISION, 1);
        RUNNING_SHOES =  new RunningShoesTrinketEffect(VaultMod.id("running_shoes"), Effects.INEXHAUSTIBLE.get(), 1);
    }
}
