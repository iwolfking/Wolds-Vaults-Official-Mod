package xyz.iwolfking.woldsvaults.init;

import com.alrex.parcool.api.Effects;
import iskallia.vault.VaultMod;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.effects.AttributeTrinket;
import iskallia.vault.gear.trinket.effects.PotionEffectTrinket;
import iskallia.vault.init.ModEffects;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import xyz.iwolfking.woldsvaults.api.lib.trinket.MultiAttributeTrinket;
import xyz.iwolfking.woldsvaults.effect.trinkets.EffectOnHitTakenEffect;
import xyz.iwolfking.woldsvaults.effect.trinkets.HeadlampTrinketEffect;
import xyz.iwolfking.woldsvaults.effect.trinkets.RunningShoesTrinketEffect;
import xyz.iwolfking.woldsvaults.effect.trinkets.SpeedLimitTrinketEffect;

import java.util.List;

public class ModTrinkets {

    public static final HeadlampTrinketEffect MINERS_LAMP;

    public static final RunningShoesTrinketEffect RUNNING_SHOES;

    public static final AttributeTrinket<Float> CHROMATIC_DIFFUSER;
    public static final AttributeTrinket<Float> SWIFT_AMULET;
    public static final MultiAttributeTrinket<Float> VIBRATING_STONE;
    public static final EffectOnHitTakenEffect IMMORTAL_SEAL;
    public static final PotionEffectTrinket LUCKY_COINS;
    public static final SpeedLimitTrinketEffect WEIGHTED_BOOTS;

    public static void init(RegistryEvent.Register<TrinketEffect<?>> event) {
        IForgeRegistry<TrinketEffect<?>> registry = event.getRegistry();
        registry.register(MINERS_LAMP);
        registry.register(RUNNING_SHOES);
        registry.register(CHROMATIC_DIFFUSER);
        registry.register(SWIFT_AMULET);
        registry.register(VIBRATING_STONE);
        registry.register(IMMORTAL_SEAL);
        registry.register(LUCKY_COINS);
        registry.register(WEIGHTED_BOOTS);
    }

    static {
        MINERS_LAMP =  new HeadlampTrinketEffect(VaultMod.id("miners_headlamp"), MobEffects.NIGHT_VISION, 1);
        RUNNING_SHOES =  new RunningShoesTrinketEffect(VaultMod.id("running_shoes"), Effects.INEXHAUSTIBLE.get(), 1);
        CHROMATIC_DIFFUSER =  new AttributeTrinket<>(VaultMod.id("chromatic_diffuser"), ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, 0.1F);
        SWIFT_AMULET =  new AttributeTrinket<>(VaultMod.id("swift_amulet"), ModGearAttributes.DODGE_PERCENT, 0.15F);
        VIBRATING_STONE =  new MultiAttributeTrinket<>(VaultMod.id("vibrating_stone"), List.of(ModGearAttributes.ECHOING_CHANCE, ModGearAttributes.ECHOING_DAMAGE), List.of(0.1F, 0.25F));
        IMMORTAL_SEAL =  new EffectOnHitTakenEffect(VaultMod.id("immortal_seal"), 0.05F, ModEffects.IMMORTALITY, 1, 60);
        LUCKY_COINS =  new PotionEffectTrinket(VaultMod.id("lucky_coins"), MobEffects.LUCK, 1);
        WEIGHTED_BOOTS = new SpeedLimitTrinketEffect(VaultMod.id("weighted_boots"));
    }
}
