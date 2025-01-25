package xyz.iwolfking.woldsvaults.data.gear.lib;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import iskallia.vault.gear.attribute.config.DoubleAttributeGenerator;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.gear.attribute.config.IntegerAttributeGenerator;
import iskallia.vault.gear.attribute.custom.effect.EffectCloudAttribute;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.data.gear.util.GearModifierRegistryHelper;

public class UnusualModifierLib {
    //Prefixes
    public static VaultGearTierConfig.ModifierTierGroup AXE_CLEAVE = GearModifierRegistryHelper.create(VaultMod.id("on_hit_aoe"), "ModOnHitType", VaultMod.id("mod_on_hit_aoe_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup AXE_REAVING = GearModifierRegistryHelper.create(VaultMod.id("reaving_damage"), "ModOnHitType", VaultMod.id("mod_reaving_damage_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup REACH = GearModifierRegistryHelper.create(VaultMod.id("reach"), "ModReach", VaultMod.id("mod_reach_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup MOVEMENT_SPEED = GearModifierRegistryHelper.create(VaultMod.id("movement_speed"), "ModMovementSpeed", VaultMod.id("mod_movement_speed_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup RESISTANCE = GearModifierRegistryHelper.create(VaultMod.id("resistance"), "ModResistance", VaultMod.id("mod_resistance_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup DAMAGE_INCREASE = GearModifierRegistryHelper.create(VaultMod.id("damage_increase"), "ModResistance", VaultMod.id("mod_damage_increase_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup DAMAGE_BABY = GearModifierRegistryHelper.create(VaultMod.id("damage_baby"), "uDamageBaby", VaultMod.id("mod_damage_baby_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup CHAINING = GearModifierRegistryHelper.create(VaultMod.id("on_hit_chain"), "ModOnHitType", VaultMod.id("mod_chaining_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup LEECH = GearModifierRegistryHelper.create(VaultMod.id("leech"), "ModLeech", VaultMod.id("mod_leech_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup CRITICAL_HIT_MITIGATION = GearModifierRegistryHelper.create(VaultMod.id("critical_hit_mitigation"), "ModCriticalHitMitigation", VaultMod.id("mod_crit_mit_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup ABILITY_POWER = GearModifierRegistryHelper.create(VaultMod.id("ability_power"), "ModAbilityPower", VaultMod.id("mod_ability_power_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup MANA_ADDITIVE = GearModifierRegistryHelper.create(VaultMod.id("mana_additive"), "ModHealthMana", VaultMod.id("mod_mana_additive_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup HEALTH = GearModifierRegistryHelper.create(VaultMod.id("health"), "ModHealthMana", VaultMod.id("mod_health_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup MANA_REGEN = GearModifierRegistryHelper.create(VaultMod.id("mana_regen"), "ModManaRegen", VaultMod.id("mod_mana_regen_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup COOLDOWN_REDUCTION = GearModifierRegistryHelper.create(VaultMod.id("cooldown_reduction"), "ModCooldownReduction", VaultMod.id("mod_cooldown_unusual"));



    //Jewel Suffixes
    public static VaultGearTierConfig.ModifierTierGroup COOLDOWN_REDUCTION_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("cooldown_reduction"), "ModCooldownReduction", VaultMod.id("mod_cooldown_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup MANA_REGEN_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("mana_regen"), "ModManaRegen", VaultMod.id("mod_mana_regen_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup RESISTANCE_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("resistance"), "ModResistance", VaultMod.id("mod_resistance_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup MOVEMENT_SPEED_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("movement_speed"), "ModSpeed", VaultMod.id("mod_movement_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup KNOCKBACK_RESISTANCE_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("knockback_resistance"), "ModKBR", VaultMod.id("mod_kbr_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup HEALING_EFFECTIVENESS_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("healing_effectiveness"), "ModHealing", VaultMod.id("mod_healing_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup DURABILITY_WEAR_REDUCTION_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("durability_wear_reduction"), "ModDurabilityReduction", VaultMod.id("mod_dura_unusual"));

    //Suffixes
    public static VaultGearTierConfig.ModifierTierGroup AXE_BLEED_CLOUD = GearModifierRegistryHelper.create(VaultMod.id("effect_cloud"), "ModOnHitAddition", VaultMod.id("mod_bleed_cloud_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup ITEM_QUANTITY = GearModifierRegistryHelper.create(VaultMod.id("item_quantity"), "ModItemQuantity", VaultMod.id("mod_item_quantity_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup ITEM_RARITY = GearModifierRegistryHelper.create(VaultMod.id("item_rarity"), "ModItemRarity", VaultMod.id("mod_item_rarity_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup COPIOUSLY = GearModifierRegistryHelper.create(VaultMod.id("copiously"), "ModCopiously", VaultMod.id("mod_copiously_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup TRAP_DISARMING = GearModifierRegistryHelper.create(VaultMod.id("trap_disarming"), "ModTrapDisarming", VaultMod.id("mod_trap_disarming_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup EFFECT_RADIUS = GearModifierRegistryHelper.create(VaultMod.id("area_of_effect"), "ModEffectRadius", VaultMod.id("mod_effect_radius_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup KINETIC_IMMUNITY = GearModifierRegistryHelper.create(VaultMod.id("kinetic_immunity"), "UniqueKineticImmunity", VaultMod.id("mod_kinetic_immunity_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup LUCKY_HIT_CHANCE = GearModifierRegistryHelper.create(VaultMod.id("lucky_hit_chance"), "ModOnHit", VaultMod.id("mod_lucky_hit_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup KNOCKBACK_RESISTANCE = GearModifierRegistryHelper.create(VaultMod.id("knockback_resistance"), "ModKnockbackResistance", VaultMod.id("mod_knockback_resistance_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup DURABILITY_WEAR_REDUCTION = GearModifierRegistryHelper.create(VaultMod.id("durability_wear_reduction"), "ModDuraReduction", VaultMod.id("mod_dura_reduction_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup MANA_PERCENTILE = GearModifierRegistryHelper.create(VaultMod.id("mana_additive_percentile"), "ModHealthMana", VaultMod.id("mod_mana_percent_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup ABILITY_POWER_PERCENTILE = GearModifierRegistryHelper.create(VaultMod.id("ability_power_percent"), "ModResistance", VaultMod.id("mod_ability_increase_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup EFFECT_DURATION = GearModifierRegistryHelper.create(VaultMod.id("effect_duration"), "ModCooldownReduction", VaultMod.id("mod_effect_duration_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup HEALING_EFFECTIVENESS = GearModifierRegistryHelper.create(VaultMod.id("healing_effectiveness"), "ModHealthEff", VaultMod.id("mod_health_eff_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup HEXING_HIT = GearModifierRegistryHelper.create(VaultMod.id("hexing_chance"), "ModOnHitType", VaultMod.id("mod_hexing_chance_unusual"));
    static {
        //Effect Clouds
        EffectCloudAttribute.CloudConfig BLEED_0 = new EffectCloudAttribute.CloudConfig("Bleed", new ResourceLocation("minecraft:empty"), 80, 4.0F, 3080192, false, 0.02F);
        EffectCloudAttribute.CloudConfig BLEED_1 = new EffectCloudAttribute.CloudConfig("Bleed", new ResourceLocation("minecraft:empty"), 120, 4.0F, 3080192, false, 0.02F);
        EffectCloudAttribute.CloudConfig BLEED_2 = new EffectCloudAttribute.CloudConfig("Bleed", new ResourceLocation("minecraft:empty"), 160, 4.0F, 3080192, false, 0.02F);
        BLEED_0.setAdditionalEffect(new EffectCloudAttribute.CloudEffectConfig(VaultMod.id("bleed"), 40, 0));
        BLEED_1.setAdditionalEffect(new EffectCloudAttribute.CloudEffectConfig(VaultMod.id("bleed"), 40, 1));
        BLEED_2.setAdditionalEffect(new EffectCloudAttribute.CloudEffectConfig(VaultMod.id("bleed"), 40, 2));

        AXE_CLEAVE.add(new VaultGearTierConfig.ModifierTier<>(0, 7, new IntegerAttributeGenerator.Range(2, 2, 1)));
        CHAINING.add(new VaultGearTierConfig.ModifierTier<>(0, 7, new IntegerAttributeGenerator.Range(1, 3, 1)));
        MANA_ADDITIVE.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new IntegerAttributeGenerator.Range(1, 3, 1)));
        MANA_ADDITIVE.add(new VaultGearTierConfig.ModifierTier<>(30, 10, new IntegerAttributeGenerator.Range(21, 30, 1)));
        MANA_ADDITIVE.add(new VaultGearTierConfig.ModifierTier<>(50, 10, new IntegerAttributeGenerator.Range(31, 40, 1)));
        MANA_ADDITIVE.add(new VaultGearTierConfig.ModifierTier<>(70, 10, new IntegerAttributeGenerator.Range(41, 50, 1)));
        MANA_ADDITIVE.add(new VaultGearTierConfig.ModifierTier<>(95, 10, new IntegerAttributeGenerator.Range(51, 60, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(2.0F, 4.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(12, 10, new FloatAttributeGenerator.Range(5.0F, 10.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(20, 10, new FloatAttributeGenerator.Range(11.0F, 14.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(34, 10, new FloatAttributeGenerator.Range(15.0F, 18.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(47, 10, new FloatAttributeGenerator.Range(19.0F, 22.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(62, 10, new FloatAttributeGenerator.Range(23.0F, 26.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(71, 10, new FloatAttributeGenerator.Range(27.0F, 31.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(80, 10, new FloatAttributeGenerator.Range(32.0F, 36.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(86, 10, new FloatAttributeGenerator.Range(37.0F, 42.0F, 1)));
        ABILITY_POWER.add(new VaultGearTierConfig.ModifierTier<>(96, 10, new FloatAttributeGenerator.Range(43.0F, 46.0F, 1)));
        AXE_REAVING.add(new VaultGearTierConfig.ModifierTier<>(0, 8, new FloatAttributeGenerator.Range(0.04F, 0.09F, 0.01F)));
        AXE_REAVING.add(new VaultGearTierConfig.ModifierTier<>(28, 8, new FloatAttributeGenerator.Range(0.1F, 0.14F, 0.01F)));
        AXE_REAVING.add(new VaultGearTierConfig.ModifierTier<>(45, 8, new FloatAttributeGenerator.Range(0.15F, 0.18F, 0.01F)));
        AXE_REAVING.add(new VaultGearTierConfig.ModifierTier<>(75, 8, new FloatAttributeGenerator.Range(0.19F, 0.23F, 0.01F)));
        ITEM_QUANTITY.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.03F, 0.06F, 0.01F)));
        ITEM_QUANTITY.add(new VaultGearTierConfig.ModifierTier<>(22, 10, new FloatAttributeGenerator.Range(0.07F, 0.12F, 0.01F)));
        ITEM_QUANTITY.add(new VaultGearTierConfig.ModifierTier<>(45, 10, new FloatAttributeGenerator.Range(0.13F, 0.18F, 0.01F)));
        ITEM_QUANTITY.add(new VaultGearTierConfig.ModifierTier<>(72, 10, new FloatAttributeGenerator.Range(0.19F, 0.25F, 0.01F)));
        ITEM_RARITY.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.04F, 0.09F, 0.01F)));
        ITEM_RARITY.add(new VaultGearTierConfig.ModifierTier<>(22, 10, new FloatAttributeGenerator.Range(0.1F, 0.14F, 0.01F)));
        ITEM_RARITY.add(new VaultGearTierConfig.ModifierTier<>(45, 10, new FloatAttributeGenerator.Range(0.15F, 0.18F, 0.01F)));
        ITEM_RARITY.add(new VaultGearTierConfig.ModifierTier<>(72, 10, new FloatAttributeGenerator.Range(0.19F, 0.23F, 0.01F)));
        COPIOUSLY.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.02F, 0.04F, 0.01F)));
        COPIOUSLY.add(new VaultGearTierConfig.ModifierTier<>(21, 10, new FloatAttributeGenerator.Range(0.05F, 0.08F, 0.01F)));
        COPIOUSLY.add(new VaultGearTierConfig.ModifierTier<>(46, 10, new FloatAttributeGenerator.Range(0.09F, 0.14F, 0.01F)));
        COPIOUSLY.add(new VaultGearTierConfig.ModifierTier<>(72, 10, new FloatAttributeGenerator.Range(0.15F, 0.18F, 0.01F)));
        TRAP_DISARMING.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.03F, 0.06F, 0.01F)));
        TRAP_DISARMING.add(new VaultGearTierConfig.ModifierTier<>(21, 10, new FloatAttributeGenerator.Range(0.07F, 0.1F, 0.01F)));
        TRAP_DISARMING.add(new VaultGearTierConfig.ModifierTier<>(35, 10, new FloatAttributeGenerator.Range(0.11F, 0.14F, 0.01F)));
        TRAP_DISARMING.add(new VaultGearTierConfig.ModifierTier<>(50, 10, new FloatAttributeGenerator.Range(0.15F, 0.18F, 0.01F)));
        REACH.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new DoubleAttributeGenerator.Range(0.1, 0.3, 0.01F)));
        REACH.add(new VaultGearTierConfig.ModifierTier<>(30, 10, new DoubleAttributeGenerator.Range(0.31, 0.65, 0.01F)));
        REACH.add(new VaultGearTierConfig.ModifierTier<>(43, 10, new DoubleAttributeGenerator.Range(0.66, 0.9, 0.01F)));
        REACH.add(new VaultGearTierConfig.ModifierTier<>(62, 10, new DoubleAttributeGenerator.Range(0.91, 1.1, 0.01F)));
        REACH.add(new VaultGearTierConfig.ModifierTier<>(78, 10, new DoubleAttributeGenerator.Range(1.11, 1.35, 0.01F)));
        HEALTH.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new DoubleAttributeGenerator.Range(2.0, 3.0, 1.0F)));
        HEALTH.add(new VaultGearTierConfig.ModifierTier<>(20, 10, new FloatAttributeGenerator.Range(4.0F, 4.0F, 1.0F)));
        HEALTH.add(new VaultGearTierConfig.ModifierTier<>(45, 10, new FloatAttributeGenerator.Range(5.0F, 5.0F, 1.0F)));
        HEALTH.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(6.0F, 6.0F, 1.0F)));
        MOVEMENT_SPEED.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.02F, 0.06F, 0.01F)));
        MOVEMENT_SPEED.add(new VaultGearTierConfig.ModifierTier<>(40, 10, new FloatAttributeGenerator.Range(0.07F, 0.1F, 0.01F)));
        MOVEMENT_SPEED.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.11F, 0.15F, 0.01F)));
        MOVEMENT_SPEED.add(new VaultGearTierConfig.ModifierTier<>(85, 10, new FloatAttributeGenerator.Range(0.16F, 0.2F, 0.01F)));
        RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.02F, 0.04F, 0.01F)));
        RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(36, 10, new FloatAttributeGenerator.Range(0.05F, 0.06F, 0.01F)));
        RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(57, 10, new FloatAttributeGenerator.Range(0.07F, 0.08F, 0.01F)));
        RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(82, 10, new FloatAttributeGenerator.Range(0.09F, 0.1F, 0.01F)));
        DAMAGE_INCREASE.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.04F, 0.08F, 0.01F)));
        DAMAGE_INCREASE.add(new VaultGearTierConfig.ModifierTier<>(21, 10, new FloatAttributeGenerator.Range(0.09F, 0.13F, 0.01F)));
        DAMAGE_INCREASE.add(new VaultGearTierConfig.ModifierTier<>(44, 10, new FloatAttributeGenerator.Range(0.14F, 0.19F, 0.01F)));
        DAMAGE_INCREASE.add(new VaultGearTierConfig.ModifierTier<>(73, 10, new FloatAttributeGenerator.Range(0.2F, 0.25F, 0.01F)));
        DAMAGE_INCREASE.add(new VaultGearTierConfig.ModifierTier<>(90, 10, new FloatAttributeGenerator.Range(0.26F, 0.3F, 0.01F)));
        DAMAGE_BABY.add(new VaultGearTierConfig.ModifierTier<>(0, 1, new FloatAttributeGenerator.Range(0.15F, 0.3F, 0.01F)));
        DAMAGE_BABY.add(new VaultGearTierConfig.ModifierTier<>(35, 1, new FloatAttributeGenerator.Range(0.31F, 0.5F, 0.01F)));
        DAMAGE_BABY.add(new VaultGearTierConfig.ModifierTier<>(90, 1, new FloatAttributeGenerator.Range(0.51F, 0.75F, 0.01F)));
        LEECH.add(new VaultGearTierConfig.ModifierTier<>(0, 1, new FloatAttributeGenerator.Range(0.01F, 0.02F, 0.01F)));
        LEECH.add(new VaultGearTierConfig.ModifierTier<>(50, 1, new FloatAttributeGenerator.Range(0.021F, 0.03F, 0.01F)));
        LEECH.add(new VaultGearTierConfig.ModifierTier<>(90, 1, new FloatAttributeGenerator.Range(0.031F, 0.035F, 0.01F)));
        EFFECT_RADIUS.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.03F, 0.07F, 0.01F)));
        EFFECT_RADIUS.add(new VaultGearTierConfig.ModifierTier<>(40, 10, new FloatAttributeGenerator.Range(0.08F, 0.11F, 0.01F)));
        EFFECT_RADIUS.add(new VaultGearTierConfig.ModifierTier<>(63, 10, new FloatAttributeGenerator.Range(0.12F, 0.16F, 0.01F)));
        EFFECT_RADIUS.add(new VaultGearTierConfig.ModifierTier<>(89, 10, new FloatAttributeGenerator.Range(0.17F, 0.2F, 0.01F)));
        CRITICAL_HIT_MITIGATION.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.05F, 0.1F, 0.01F)));
        CRITICAL_HIT_MITIGATION.add(new VaultGearTierConfig.ModifierTier<>(35, 10, new FloatAttributeGenerator.Range(0.11F, 0.2F, 0.01F)));
        CRITICAL_HIT_MITIGATION.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.21F, 0.25F, 0.01F)));
        LUCKY_HIT_CHANCE.add(new VaultGearTierConfig.ModifierTier<>(25, 7, new FloatAttributeGenerator.Range(0.01F, 0.02F, 0.01F)));
        LUCKY_HIT_CHANCE.add(new VaultGearTierConfig.ModifierTier<>(50, 7, new FloatAttributeGenerator.Range(0.03F, 0.04F, 0.01F)));
        LUCKY_HIT_CHANCE.add(new VaultGearTierConfig.ModifierTier<>(75, 7, new FloatAttributeGenerator.Range(0.05F, 0.06F, 0.01F)));
        KNOCKBACK_RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.05F, 0.1F, 0.01F)));
        KNOCKBACK_RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(15, 10, new FloatAttributeGenerator.Range(0.11F, 0.14F, 0.01F)));
        KNOCKBACK_RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(30, 10, new FloatAttributeGenerator.Range(0.15F, 0.19F, 0.01F)));
        KNOCKBACK_RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(57, 10, new FloatAttributeGenerator.Range(0.2F, 0.24F, 0.01F)));
        KNOCKBACK_RESISTANCE.add(new VaultGearTierConfig.ModifierTier<>(89, 10, new FloatAttributeGenerator.Range(0.25F, 0.3F, 0.01F)));
        DURABILITY_WEAR_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.05F, 0.1F, 0.01F)));
        DURABILITY_WEAR_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(24, 10, new FloatAttributeGenerator.Range(0.11F, 0.2F, 0.01F)));
        DURABILITY_WEAR_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(50, 10, new FloatAttributeGenerator.Range(0.21F, 0.3F, 0.01F)));
        DURABILITY_WEAR_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.31F, 0.4F, 0.01F)));
        DURABILITY_WEAR_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(90, 10, new FloatAttributeGenerator.Range(0.41F, 0.5F, 0.01F)));
        MANA_PERCENTILE.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.1F, 0.15F, 0.01F)));
        MANA_PERCENTILE.add(new VaultGearTierConfig.ModifierTier<>(45, 10, new FloatAttributeGenerator.Range(0.15F, 0.2F, 0.01F)));
        MANA_PERCENTILE.add(new VaultGearTierConfig.ModifierTier<>(80, 10, new FloatAttributeGenerator.Range(0.25F, 0.3F, 0.01F)));
        ABILITY_POWER_PERCENTILE.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.03F, 0.05F, 0.01F)));
        ABILITY_POWER_PERCENTILE.add(new VaultGearTierConfig.ModifierTier<>(30, 10, new FloatAttributeGenerator.Range(0.06F, 0.09F, 0.01F)));
        ABILITY_POWER_PERCENTILE.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.1F, 0.15F, 0.01F)));
        EFFECT_DURATION.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.03F, 0.05F, 0.01F)));
        EFFECT_DURATION.add(new VaultGearTierConfig.ModifierTier<>(20, 10, new FloatAttributeGenerator.Range(0.06F, 0.08F, 0.01F)));
        EFFECT_DURATION.add(new VaultGearTierConfig.ModifierTier<>(50, 10, new FloatAttributeGenerator.Range(0.09F, 0.11F, 0.01F)));
        EFFECT_DURATION.add(new VaultGearTierConfig.ModifierTier<>(70, 10, new FloatAttributeGenerator.Range(0.12F, 0.14F, 0.01F)));
        EFFECT_DURATION.add(new VaultGearTierConfig.ModifierTier<>(80, 10, new FloatAttributeGenerator.Range(0.15F, 0.17F, 0.01F)));
        EFFECT_DURATION.add(new VaultGearTierConfig.ModifierTier<>(92, 10, new FloatAttributeGenerator.Range(0.18F, 0.2F, 0.01F)));
        HEALING_EFFECTIVENESS.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.04F, 0.08F, 0.01F)));
        HEALING_EFFECTIVENESS.add(new VaultGearTierConfig.ModifierTier<>(24, 10, new FloatAttributeGenerator.Range(0.09F, 0.12F, 0.01F)));
        HEALING_EFFECTIVENESS.add(new VaultGearTierConfig.ModifierTier<>(40, 10, new FloatAttributeGenerator.Range(0.13F, 0.16F, 0.01F)));
        HEALING_EFFECTIVENESS.add(new VaultGearTierConfig.ModifierTier<>(66, 10, new FloatAttributeGenerator.Range(0.17F, 0.2F, 0.01F)));
        MANA_REGEN.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.05F, 0.1F, 0.01F)));
        MANA_REGEN.add(new VaultGearTierConfig.ModifierTier<>(15, 10, new FloatAttributeGenerator.Range(0.11F, 0.15F, 0.01F)));
        MANA_REGEN.add(new VaultGearTierConfig.ModifierTier<>(30, 10, new FloatAttributeGenerator.Range(0.16F, 0.2F, 0.01F)));
        MANA_REGEN.add(new VaultGearTierConfig.ModifierTier<>(40, 10, new FloatAttributeGenerator.Range(0.21F, 0.3F, 0.01F)));
        MANA_REGEN.add(new VaultGearTierConfig.ModifierTier<>(50, 10, new FloatAttributeGenerator.Range(0.31F, 0.4F, 0.01F)));
        MANA_REGEN.add(new VaultGearTierConfig.ModifierTier<>(67, 10, new FloatAttributeGenerator.Range(0.41F, 0.5F, 0.01F)));
        COOLDOWN_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.03F, 0.06F, 0.01F)));
        COOLDOWN_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(20, 10, new FloatAttributeGenerator.Range(0.07F, 0.1F, 0.01F)));
        COOLDOWN_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(40, 10, new FloatAttributeGenerator.Range(0.11F, 0.14F, 0.01F)));
        COOLDOWN_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(60, 10, new FloatAttributeGenerator.Range(0.15F, 0.17F, 0.01F)));
        COOLDOWN_REDUCTION.add(new VaultGearTierConfig.ModifierTier<>(85, 10, new FloatAttributeGenerator.Range(0.18F, 0.2F, 0.01F)));
        KINETIC_IMMUNITY.add(new VaultGearTierConfig.ModifierTier<>(0, 2, new BooleanFlagGenerator.BooleanFlag(true)));
        AXE_BLEED_CLOUD.add(new VaultGearTierConfig.ModifierTier<>(0, 10, BLEED_0));
        AXE_BLEED_CLOUD.add(new VaultGearTierConfig.ModifierTier<>(32, 10, BLEED_1));
        AXE_BLEED_CLOUD.add(new VaultGearTierConfig.ModifierTier<>(64, 10, BLEED_2));
        HEXING_HIT.add(new VaultGearTierConfig.ModifierTier<>(0, 4, new FloatAttributeGenerator.Range(0.02F, 0.04F, 0.01F)));
        HEXING_HIT.add(new VaultGearTierConfig.ModifierTier<>(20, 4, new FloatAttributeGenerator.Range(0.05F, 0.07F, 0.01F)));
        HEXING_HIT.add(new VaultGearTierConfig.ModifierTier<>(40, 4, new FloatAttributeGenerator.Range(0.07F, 0.08F, 0.01F)));
        HEXING_HIT.add(new VaultGearTierConfig.ModifierTier<>(60, 4, new FloatAttributeGenerator.Range(0.09F, 0.1F, 0.01F)));
        HEXING_HIT.add(new VaultGearTierConfig.ModifierTier<>(85, 4, new FloatAttributeGenerator.Range(0.11F, 0.12F, 0.01F)));

        COOLDOWN_REDUCTION_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.005F, 0.01F, 0.001F)));
        COOLDOWN_REDUCTION_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.01F, 0.02F, 0.001F)));

        MANA_REGEN_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.01F, 0.02F, 0.001F)));
        MANA_REGEN_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.02F, 0.04F, 0.001F)));

        RESISTANCE_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.005F, 0.01F, 0.001F)));
        RESISTANCE_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.01F, 0.02F, 0.001F)));

        MOVEMENT_SPEED_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.01F, 0.02F, 0.001F)));
        MOVEMENT_SPEED_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.02F, 0.04F, 0.001F)));

        KNOCKBACK_RESISTANCE_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.01F, 0.03F, 0.001F)));
        KNOCKBACK_RESISTANCE_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.03F, 0.04F, 0.001F)));

        HEALING_EFFECTIVENESS_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.01F, 0.02F, 0.001F)));
        HEALING_EFFECTIVENESS_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.02F, 0.04F, 0.001F)));

        DURABILITY_WEAR_REDUCTION_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new FloatAttributeGenerator.Range(0.01F, 0.03F, 0.001F)));
        DURABILITY_WEAR_REDUCTION_JEWEL.add(new VaultGearTierConfig.ModifierTier<>(65, 10, new FloatAttributeGenerator.Range(0.03F, 0.05F, 0.001F)));
    }
}
