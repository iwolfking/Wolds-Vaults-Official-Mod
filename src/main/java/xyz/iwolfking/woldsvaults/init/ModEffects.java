package xyz.iwolfking.woldsvaults.init;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.abilities.ColossusAbility;
import xyz.iwolfking.woldsvaults.abilities.SneakyGetawayAbility;
import xyz.iwolfking.woldsvaults.effect.mobeffects.*;
import xyz.iwolfking.woldsvaults.integration.pehkui.CustomScaleTypes;

public class ModEffects {
    public static final MobEffect GROWING = new GrowingPotionEffect(MobEffectCategory.NEUTRAL, 0xc90000, CustomScaleTypes.SIZE, WoldsVaults.id("growing"));
    public static final MobEffect SHRINKING = new ShrinkingPotionEffect(MobEffectCategory.NEUTRAL, 0xcca468, CustomScaleTypes.SIZE, WoldsVaults.id("shrinking"));
    public static final MobEffect REAVING = new ReavingPotionEffect(MobEffectCategory.BENEFICIAL, 0xcca468, WoldsVaults.id("reaving"));
    public static final MobEffect GROUNDED = new GroundedPotionEffect(MobEffectCategory.NEUTRAL, 0xcca468, WoldsVaults.id("grounded"));
    public static final MobEffect COLOSSUS = new ColossusAbility.ColossusEffect(MobEffectCategory.BENEFICIAL,0xc90000, CustomScaleTypes.SIZE_NO_MOVEMENT, WoldsVaults.id("colossus"));
    public static final MobEffect SNEAKY_GETAWAY = new SneakyGetawayAbility.SneakyGetawayEffect(MobEffectCategory.BENEFICIAL,0xcca468,CustomScaleTypes.SIZE_NO_MOVEMENT, WoldsVaults.id("sneaky_getaway"));
    public static final MobEffect ECHOING = new EchoingPotionEffect(MobEffectCategory.HARMFUL, 0x691997, WoldsVaults.id("echoing"));
    public static final MobEffect SAFER_SPACE = new SaferSpacePotionEffect(MobEffectCategory.BENEFICIAL,0xae6bd1, WoldsVaults.id("safer_space"));
    public static final MobEffect LEVITATEII = new LevitateIIPotionEffect(MobEffectCategory.BENEFICIAL,0xceffff, WoldsVaults.id("levitateii"));
    public static final MobEffect REROLLED_TIMEOUT = new RerolledPotionEffect(MobEffectCategory.BENEFICIAL,0xceffff, WoldsVaults.id("rerolled"));
    public static final MobEffect EMPOWER = new EmpowerEffect();
    public static final MobEffect QUICKENING = new QuickeningEffect();
    public static final MobEffect BURN = new PercentBurnEffect();

    public static void register(RegistryEvent.Register<MobEffect> event) {
        event.getRegistry().registerAll(  SHRINKING
                                        , GROWING
                                        , REAVING
                                        , COLOSSUS
                                        , SNEAKY_GETAWAY
                                        , ECHOING
                                        , SAFER_SPACE
                                        , LEVITATEII
                                        , REROLLED_TIMEOUT
                                        , EMPOWER
                                        , QUICKENING
                                        , BURN
                                        );
    }
}
