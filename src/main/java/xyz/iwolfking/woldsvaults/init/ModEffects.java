package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.effect.ThresholdEffect;
import iskallia.vault.skill.ability.effect.spi.core.ToggleAbilityEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.abilities.ColossusAbility;
import xyz.iwolfking.woldsvaults.abilities.SneakyGetawayAbility;
import xyz.iwolfking.woldsvaults.abilities.UltimateShieldAbility;
import xyz.iwolfking.woldsvaults.effect.mobeffects.*;
import xyz.iwolfking.woldsvaults.effect.mobeffects.lib.ComboEffect;
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
    public static final MobEffect TIDAL_HOVER = new TidalHoverEffect(MobEffectCategory.BENEFICIAL, 0x1c8f3a, WoldsVaults.id("tidal_hover"));
    public static final MobEffect REROLLED_TIMEOUT = new RerolledPotionEffect(MobEffectCategory.BENEFICIAL,0xceffff, WoldsVaults.id("rerolled"));
    public static final MobEffect EMPOWER = new EmpowerEffect();
    public static final MobEffect QUICKENING = new QuickeningEffect();
    public static final MobEffect BURN = new PercentBurnEffect();
    public static final MobEffect STEADFAST = new SteadfastPotionEffect();
    public static final MobEffect ARMORED = new ArmoredPotionEffect();
    public static final MobEffect BLITZ = new BlitzPotionEffect();
    public static final MobEffect BLEED_OVERRIDE = new BleedOverrideEffect();
    public static final MobEffect SHREDDED = new ShreddedEffect();
    public static final MobEffect HEALING_BLOCK = new HealBlockEffect();
    public static final ComboEffect MAGIC_BURST_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0x8f32a8, WoldsVaults.id("magic_burst_combo"));
    public static final ComboEffect DAMAGE_BURST_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0x911d44, WoldsVaults.id("damage_burst_combo"));
    public static final ComboEffect HEALTH_BURST_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0xf58782, WoldsVaults.id("health_burst_combo"));
    public static final ComboEffect BLEED_EFFECT_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0xcc5a54, WoldsVaults.id("bleed_effect_combo"));
    public static final ComboEffect POISON_EFFECT_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0x4d8254, WoldsVaults.id("poison_effect_combo"));
    public static final ComboEffect WITHER_EFFECT_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0x363431, WoldsVaults.id("wither_effect_combo"));
    public static final ComboEffect HYBRID_BURST_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0xdcde66, WoldsVaults.id("hybrid_burst_combo"));
    public static final ComboEffect CHILLING_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0x66dcde, WoldsVaults.id("chilling_combo"));
    public static final ComboEffect GRAVITY_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0x5b3fa8, WoldsVaults.id("gravity_combo"));
    public static final ComboEffect DIFFUSE_COMBO = new ComboEffect(MobEffectCategory.NEUTRAL, 0x4d9e7d, WoldsVaults.id("diffuse_combo"));
    public static final ThresholdEffect BATTLESTAFF_BLOCKING = new ThresholdEffect(0xceffff, WoldsVaults.id("battlestaff_blocking"));
    public static final ThresholdEffect MOMENTUM_ENGINE = new ThresholdEffect(0x99a63c, WoldsVaults.id("momentum_engine"));
    public static final ThresholdEffect BLOOD_CHAKRA = new ThresholdEffect(0xa83262, WoldsVaults.id("blood_chakra"));
    public static final ToggleAbilityEffect ULTIMATE_SHIELD = new UltimateShieldAbility.UltimateShieldEffect(0x0000ff, WoldsVaults.id("ultimate_shield"));

    public static void register(RegistryEvent.Register<MobEffect> event) {
        event.getRegistry().registerAll(  SHRINKING
                                        , GROWING
                                        , REAVING
                                        , COLOSSUS
                                        , SNEAKY_GETAWAY
                                        , ECHOING
                                        , SAFER_SPACE
                                        , LEVITATEII
                                        , TIDAL_HOVER
                                        , REROLLED_TIMEOUT
                                        , EMPOWER
                                        , QUICKENING
                                        , BURN
                                        , STEADFAST
                                        , ARMORED
                                        , SHREDDED
                                        , BLITZ
                                        , BLEED_OVERRIDE
                                        , BATTLESTAFF_BLOCKING
                                        , MOMENTUM_ENGINE
                                        , BLOOD_CHAKRA
                                        , ULTIMATE_SHIELD
                                        , HEALING_BLOCK
                                        , MAGIC_BURST_COMBO
                                        , DAMAGE_BURST_COMBO
                                        , HYBRID_BURST_COMBO
                                        , HEALTH_BURST_COMBO
                                        , BLEED_EFFECT_COMBO
                                        , POISON_EFFECT_COMBO
                                        , WITHER_EFFECT_COMBO
                                        , CHILLING_COMBO
                                        , DIFFUSE_COMBO
                                        , GRAVITY_COMBO
                                        );
    }
}
