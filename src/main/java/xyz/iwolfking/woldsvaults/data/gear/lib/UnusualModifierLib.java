package xyz.iwolfking.woldsvaults.data.gear.lib;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.gear.attribute.custom.effect.EffectCloudAttribute;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.data.gear.util.GearModifierRegistryHelper;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.StringValueGenerator;

import java.util.List;

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
    public static VaultGearTierConfig.ModifierTierGroup EFFECT_CLOUD_CHANCE = GearModifierRegistryHelper.create(VaultMod.id("effect_cloud_chance_additive"), "ModBonus", VaultMod.id("mod_effect_cloud_chance"));



    //Jewel Suffixes
    public static VaultGearTierConfig.ModifierTierGroup COOLDOWN_REDUCTION_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("cooldown_reduction"), "ModCooldownReduction", VaultMod.id("mod_cooldown_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup MANA_REGEN_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("mana_regen"), "ModManaRegen", VaultMod.id("mod_mana_regen_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup RESISTANCE_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("resistance"), "ModResistance", VaultMod.id("mod_resistance_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup MOVEMENT_SPEED_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("movement_speed"), "ModSpeed", VaultMod.id("mod_movement_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup KNOCKBACK_RESISTANCE_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("knockback_resistance"), "ModKBR", VaultMod.id("mod_kbr_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup HEALING_EFFECTIVENESS_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("healing_effectiveness"), "ModHealing", VaultMod.id("mod_healing_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup DURABILITY_WEAR_REDUCTION_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("durability_wear_reduction"), "ModDurabilityReduction", VaultMod.id("mod_dura_unusual"));
    public static VaultGearTierConfig.ModifierTierGroup VEIN_MINER_LEVEL_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("added_ability_level"), "ModVeinMinerLevel", VaultMod.id("mod_vein_miner_normal_level"));
    public static VaultGearTierConfig.ModifierTierGroup CHAIN_MINER_LEVEL_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("added_ability_level"), "ModVeinMinerLevel", VaultMod.id("mod_vein_miner_chain_level"));
    public static VaultGearTierConfig.ModifierTierGroup FINESSE_MINER_LEVEL_JEWEL = GearModifierRegistryHelper.create(VaultMod.id("added_ability_level"), "ModVeinMinerLevel", VaultMod.id("mod_vein_miner_dura_level"));

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
    public static VaultGearTierConfig.ModifierTierGroup DODGE_CHANCE = GearModifierRegistryHelper.create(VaultMod.id("dodge_percent"), "ModResistance", VaultMod.id("mod_dodge_chance_unusual"));

    //Map
    public static VaultGearTierConfig.ModifierTierGroup NON_LETHAL = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModMobCrit", VaultMod.id("no_crit_mobs"));
    public static VaultGearTierConfig.ModifierTierGroup CORROSIVE = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModDurability", VaultMod.id("corrosive"));
    public static VaultGearTierConfig.ModifierTierGroup EXTRA_REINFORCED = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModDurability", VaultMod.id("extra_reinforced"));
    public static VaultGearTierConfig.ModifierTierGroup TIRED = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModMiningSpeed", VaultMod.id("tired"));
    public static VaultGearTierConfig.ModifierTierGroup LEECHING = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModLeech", VaultMod.id("leeching"));
    public static VaultGearTierConfig.ModifierTierGroup BACKWARDS = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModUnique", VaultMod.id("backwards"));
    public static VaultGearTierConfig.ModifierTierGroup BLESSED = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModGods", VaultMod.id("blessed"));
    public static VaultGearTierConfig.ModifierTierGroup DOOR_HUNTER = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModHunters", VaultMod.id("door_hunter"));
    public static VaultGearTierConfig.ModifierTierGroup SWEET_RETRO = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModRetro", VaultMod.id("sweet_retro"));
    public static VaultGearTierConfig.ModifierTierGroup HAUNTED_MANSION = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModUnique", VaultMod.id("haunted_mansion"));
    public static VaultGearTierConfig.ModifierTierGroup KILL_NOVA = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModMobDeath", VaultMod.id("kill_nova"));
    public static VaultGearTierConfig.ModifierTierGroup KILL_FROST_NOVA = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModMobDeath", VaultMod.id("kill_frostnova"));
    public static VaultGearTierConfig.ModifierTierGroup FRENZY = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModFrenzy", VaultMod.id("frenzy"));
    public static VaultGearTierConfig.ModifierTierGroup HUNGER = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModUnique", VaultMod.id("hunger"));
    public static VaultGearTierConfig.ModifierTierGroup JUMPY = GearModifierRegistryHelper.create(VaultMod.id("static_modifier"), "ModJump", VaultMod.id("jumpy_deluxe"));

    static {
        AXE_CLEAVE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 50, 90), List.of(-1, -1, -1), List.of(2, 3, 4), List.of(2, 3, 4), 1, 6));
        CHAINING.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 50, 90), List.of(-1, -1, -1), List.of(1, 2, 3), List.of(2, 3, 4), 1, 6));
        MANA_ADDITIVE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 30, 50, 70, 95), List.of(45, 60, 80, -1, -1), List.of(10, 21, 31, 41, 51), List.of(20, 30, 40, 50, 60), 1, 10));
        ABILITY_POWER.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 12, 20, 34, 47, 62, 71, 80, 86, 96), List.of(25, 34, 44, 52, 60, 75, 90, -1, -1, -1), List.of(2.0F, 5.0F, 11.0F, 15.0F, 19.0F, 23.0F, 27.0F, 32.0F, 37.0F, 43.0F), List.of(4.0F, 10.0F, 14.0F, 18.0F, 22.0F, 26.0F, 31.0F, 36.0F, 42.0F, 46.0F), 1.0F, 10));
        AXE_REAVING.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 28, 45, 75), List.of(45, 75, -1, -1), List.of(0.04F, 0.1F, 0.15F, 0.19F), List.of(0.09F, 0.14F, 0.18F, 0.23F), 0.01F, 5));
        ITEM_QUANTITY.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 22, 45, 72), List.of(40, 70, -1, -1), List.of(0.03F, 0.07F, 0.13F, 0.19F), List.of(0.06F, 0.12F, 0.18F, 0.25F), 0.01F, 10));
        ITEM_RARITY.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 22, 45, 72), List.of(40, 70, -1, -1), List.of(0.03F, 0.07F, 0.13F, 0.19F), List.of(0.06F, 0.12F, 0.18F, 0.25F), 0.01F, 10));
        COPIOUSLY.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 21, 46, 72), List.of(40, 70, -1, -1), List.of(0.05F, 0.07F, 0.11F, 0.16F), List.of(0.07F, 0.09F, 0.14F, 0.20F), 0.01F, 10));
        TRAP_DISARMING.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 21, 46, 72), List.of(40, 70, -1, -1), List.of(0.05F, 0.07F, 0.11F, 0.16F), List.of(0.07F, 0.09F, 0.14F, 0.20F), 0.01F, 10));
        REACH.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 30, 43, 62, 78), List.of(25, 45, 65, -1, -1), List.of(0.1, 0.31, 0.66, 0.91, 1.11), List.of(0.3, 0.65, 0.9, 1.1, 1.35), 0.01, 10));
        HEALTH.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 20, 45, 65), List.of(40, 65, -1, -1), List.of(2.0F, 4.0F, 6.0F, 8.0F), List.of(4.0F, 6.0F, 8.0F, 10.0F), 1.0F, 10));
        MOVEMENT_SPEED.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 40, 65, 85), List.of(40, 65, -1, -1), List.of(0.02F, 0.07F, 0.11F, 0.16F), List.of(0.06F, 0.1F, 0.15F, 0.2F), 0.01F, 10));
        RESISTANCE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 36, 57, 82), List.of(40, 80, -1, -1), List.of(0.02F, 0.05F, 0.07F, 0.09F), List.of(0.04F, 0.06F, 0.08F, 0.1F), 0.01F, 10));
        DAMAGE_INCREASE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 21, 44, 73, 90), List.of(35, 60, 75, -1, -1), List.of(0.04F, 0.09F, 0.14F, 0.2F, 0.26F), List.of(0.08F, 0.13F, 0.19F, 0.25F, 0.3F), 0.01F, 10));
        DAMAGE_BABY.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 35, 90), List.of(35, 75, -1), List.of(0.15F, 0.31F, 0.51F), List.of(0.3F, 0.5F, 0.75F), 0.01F, 1));
        LEECH.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(50, 75, 90), List.of(-1, -1, -1), List.of(0.01F, 0.02F, 0.03F), List.of(0.02F, 0.03F, 0.035F), 0.01F, 1));
        EFFECT_CLOUD_CHANCE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(25, 50, 90), List.of(65, -1, -1), List.of(0.01F, 0.02F, 0.03F), List.of(0.02F, 0.03F, 0.04F), 0.01F, 5));
        DODGE_CHANCE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(75, 95), List.of(-1, -1), List.of(0.01F, 0.05F), List.of(0.05F, 0.1F), 0.01F, 2));
        EFFECT_RADIUS.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 40, 65, 90), List.of(45, 75, -1, -1), List.of(0.03F, 0.08F, 0.12F, 0.17F), List.of(0.07F, 0.11F, 0.16F, 0.2F), 0.01F, 10));
        CRITICAL_HIT_MITIGATION.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 35, 65), List.of(65, -1, -1), List.of(0.05F, 0.11F, 0.21F), List.of(0.1F, 0.2F, 0.25F), 0.01F, 10));
        LUCKY_HIT_CHANCE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(25, 50, 75), List.of(75, -1, -1), List.of(0.01F, 0.04F, 0.07F), List.of(0.04F, 0.06F, 0.09F), 0.01F, 8));
        KNOCKBACK_RESISTANCE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 15, 30, 57, 89), List.of(35, 60, 90, -1, -1), List.of(0.05F, 0.11F, 0.15F, 0.2F, 0.25F), List.of(0.1F, 0.14F, 0.19F, 0.24F, 0.3F), 0.01F, 10));
        DURABILITY_WEAR_REDUCTION.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 25, 50, 65, 90), List.of(30, 60, 90, -1, -1), List.of(0.05F, 0.11F, 0.21F, 0.31F, 0.41F), List.of(0.1F, 0.2F, 0.3F, 0.4F, 0.5F), 0.01F, 10));
        MANA_PERCENTILE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 45, 80), List.of(45, -1, -1), List.of(0.1F, 0.15F, 0.25F), List.of(0.15F, 0.2F, 0.3F), 0.01F, 10));
        ABILITY_POWER_PERCENTILE.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 30, 65), List.of(45, -1, -1), List.of(0.03F, 0.06F, 0.1F), List.of(0.05F, 0.09F, 0.15F), 0.01F, 10));
        EFFECT_DURATION.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 25, 50, 75, 85, 95), List.of(25, 50, 75, -1, -1, -1), List.of(0.03F, 0.06F, 0.09F, 0.12F, 0.15F, 0.18F), List.of(0.05F, 0.08F, 0.11F, 0.14F, 0.17F, 0.2F), 0.01F, 10));
        HEALING_EFFECTIVENESS.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 25, 45, 70), List.of(30, 60, -1, -1), List.of(0.04F, 0.09F, 0.13F, 0.17F), List.of(0.08F, 0.12F, 0.16F, 0.2F), 0.01F, 10));
        MANA_REGEN.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 15, 35, 50, 65, 80), List.of(30, 50, 75, -1, -1, -1), List.of(0.05F, 0.11F, 0.16F, 0.21F, 0.31F, 0.4F), List.of(0.1F, 0.15F, 0.2F, 0.3F, 0.4F, 0.5F), 0.01F, 10));
        COOLDOWN_REDUCTION.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(0, 25, 45, 65, 85), List.of(30, 60, 90, -1, -1), List.of(0.03F, 0.07F, 0.11F, 0.15F, 0.18F), List.of(0.06F, 0.1F, 0.14F, 0.17F, 0.2F), 0.01F, 10));
        HEXING_HIT.addAll(GearModifierRegistryHelper.createNumberRangeTiers(List.of(25, 40, 60, 80, 95), List.of(50, 60, 80, -1, -1), List.of(0.02F, 0.05F, 0.07F, 0.09F, 0.11F), List.of(0.04F, 0.07F, 0.08F, 0.1F, 0.12F), 0.01F, 4));

        //Boolean
        KINETIC_IMMUNITY.add(new VaultGearTierConfig.ModifierTier<>(0, 1, new BooleanFlagGenerator.BooleanFlag(true)));

        //Effect Clouds
        EffectCloudAttribute.CloudConfig BLEED_0 = new EffectCloudAttribute.CloudConfig("Bleed", new ResourceLocation("minecraft:empty"), 80, 4.0F, 3080192, false, 0.02F);
        EffectCloudAttribute.CloudConfig BLEED_1 = new EffectCloudAttribute.CloudConfig("Bleed", new ResourceLocation("minecraft:empty"), 120, 4.0F, 3080192, false, 0.02F);
        EffectCloudAttribute.CloudConfig BLEED_2 = new EffectCloudAttribute.CloudConfig("Bleed", new ResourceLocation("minecraft:empty"), 160, 4.0F, 3080192, false, 0.02F);
        BLEED_0.setAdditionalEffect(new EffectCloudAttribute.CloudEffectConfig(VaultMod.id("bleed"), 40, 0));
        BLEED_1.setAdditionalEffect(new EffectCloudAttribute.CloudEffectConfig(VaultMod.id("bleed"), 40, 1));
        BLEED_2.setAdditionalEffect(new EffectCloudAttribute.CloudEffectConfig(VaultMod.id("bleed"), 40, 2));

        AXE_BLEED_CLOUD.add(new VaultGearTierConfig.ModifierTier<>(0, 10, BLEED_0));
        AXE_BLEED_CLOUD.add(new VaultGearTierConfig.ModifierTier<>(32, 10, BLEED_1));
        AXE_BLEED_CLOUD.add(new VaultGearTierConfig.ModifierTier<>(64, 10, BLEED_2));

        //Jewels
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

        VEIN_MINER_LEVEL_JEWEL.addAll(GearModifierRegistryHelper.createAbilityLevelTiers("Vein_Miner", List.of(65), List.of(-1), List.of(1), 15));


        //Maps
        NON_LETHAL.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:no_crit_mobs")));
        CORROSIVE.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:corrosive")));
        EXTRA_REINFORCED.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:extra_reinforced")));
        TIRED.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:tired")));
        LEECHING.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:leeching")));
        BACKWARDS.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:backwards")));
        BLESSED.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:blessed")));
        DOOR_HUNTER.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:door_hunter")));
        SWEET_RETRO.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:sweet_retro")));
        HAUNTED_MANSION.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:haunted_mansion")));
        KILL_NOVA.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:kill_nova")));
        KILL_FROST_NOVA.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:kill_frostnova")));
        FRENZY.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:kill_frostnova")));
        HUNGER.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:kill_frostnova")));
        JUMPY.add(new VaultGearTierConfig.ModifierTier<>(0, 10, new StringValueGenerator.StringValue("the_vault:jumpy_deluxe")));
    }
}
