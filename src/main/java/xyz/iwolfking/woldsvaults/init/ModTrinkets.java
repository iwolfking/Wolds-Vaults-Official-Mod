package xyz.iwolfking.woldsvaults.init;

import com.alrex.parcool.api.Effects;
import iskallia.vault.VaultMod;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.effects.AttributeTrinket;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import xyz.iwolfking.woldsvaults.effect.trinkets.HeadlampTrinketEffect;
import xyz.iwolfking.woldsvaults.effect.trinkets.RunningShoesTrinketEffect;

public class ModTrinkets {

    private static final HeadlampTrinketEffect MINERS_LAMP;

    private static final RunningShoesTrinketEffect RUNNING_SHOES;

    private static final AttributeTrinket<Float> CHROMATIC_DIFFUSER;

    public static void init(RegistryEvent.Register<TrinketEffect<?>> event) {
        IForgeRegistry<TrinketEffect<?>> registry = event.getRegistry();
        registry.register(MINERS_LAMP);
        registry.register(RUNNING_SHOES);
        registry.register(CHROMATIC_DIFFUSER);

    }

    static {
        MINERS_LAMP =  new HeadlampTrinketEffect(VaultMod.id("miners_headlamp"), MobEffects.NIGHT_VISION, 1);
        RUNNING_SHOES =  new RunningShoesTrinketEffect(VaultMod.id("running_shoes"), Effects.INEXHAUSTIBLE.get(), 1);
        CHROMATIC_DIFFUSER =  new AttributeTrinket<>(VaultMod.id("chromatic_diffuser"), ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, 0.1F);
    }
}
