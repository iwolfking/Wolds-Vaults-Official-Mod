package xyz.iwolfking.woldsvaults.data.gear;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.data.gear.lib.UnusualModifierLib;

import java.util.HashMap;
import java.util.Map;

public class UnusualModifiers {
    public static Map<ResourceLocation, VaultGearTierConfig.AttributeGroup> UNUSUAL_MODIFIERS_MAP_PREFIX = new HashMap<>();
    public static Map<ResourceLocation, VaultGearTierConfig.AttributeGroup> UNUSUAL_MODIFIERS_MAP_SUFFIX = new HashMap<>();

    private static VaultGearTierConfig.AttributeGroup AXE_MODIFIERS_PREFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup AXE_MODIFIERS_SUFFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup SWORD_MODIFIERS_PREFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup SWORD_MODIFIERS_SUFFIX = new VaultGearTierConfig.AttributeGroup();

    private static VaultGearTierConfig.AttributeGroup ARMOR_MODIFIERS_PREFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup ARMOR_MODIFIERS_SUFFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup NON_SACK_PREFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup PLUSHIE_PREFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup NON_SACK_SUFFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup SACK_SUFFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup PLUSHIE_SUFFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup SACK_PREFIX = new VaultGearTierConfig.AttributeGroup();
    private static VaultGearTierConfig.AttributeGroup JEWEL_SUFFIX = new VaultGearTierConfig.AttributeGroup();

    static {
        JEWEL_SUFFIX.add(UnusualModifierLib.HEALING_EFFECTIVENESS_JEWEL);
        JEWEL_SUFFIX.add(UnusualModifierLib.MANA_REGEN_JEWEL);
        JEWEL_SUFFIX.add(UnusualModifierLib.KNOCKBACK_RESISTANCE_JEWEL);
        JEWEL_SUFFIX.add(UnusualModifierLib.RESISTANCE_JEWEL);
        JEWEL_SUFFIX.add(UnusualModifierLib.MOVEMENT_SPEED_JEWEL);
        JEWEL_SUFFIX.add(UnusualModifierLib.COOLDOWN_REDUCTION_JEWEL);
        JEWEL_SUFFIX.add(UnusualModifierLib.DURABILITY_WEAR_REDUCTION_JEWEL);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("jewel"), JEWEL_SUFFIX);

        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.REACH);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.MOVEMENT_SPEED);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.RESISTANCE);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.DAMAGE_INCREASE);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.DAMAGE_BABY);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.LEECH);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.CRITICAL_HIT_MITIGATION);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.ABILITY_POWER);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.MANA_ADDITIVE);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.MANA_REGEN);
        AXE_MODIFIERS_PREFIX.add(UnusualModifierLib.COOLDOWN_REDUCTION);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.ITEM_QUANTITY);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.ITEM_RARITY);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.COPIOUSLY);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.TRAP_DISARMING);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.EFFECT_DURATION);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.EFFECT_RADIUS);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.KINETIC_IMMUNITY);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.KNOCKBACK_RESISTANCE);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.DURABILITY_WEAR_REDUCTION);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.ABILITY_POWER_PERCENTILE);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.HEALING_EFFECTIVENESS);
        AXE_MODIFIERS_SUFFIX.add(UnusualModifierLib.HEXING_HIT);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("axe"), AXE_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("axe"), AXE_MODIFIERS_SUFFIX);

        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.REACH);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.MOVEMENT_SPEED);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.RESISTANCE);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.DAMAGE_INCREASE);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.DAMAGE_BABY);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.LEECH);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.CRITICAL_HIT_MITIGATION);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.ABILITY_POWER);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.MANA_ADDITIVE);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.MANA_REGEN);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.COOLDOWN_REDUCTION);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.AXE_BLEED_CLOUD);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.AXE_REAVING);
        SWORD_MODIFIERS_PREFIX.add(UnusualModifierLib.AXE_CLEAVE);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.ITEM_QUANTITY);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.ITEM_RARITY);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.COPIOUSLY);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.TRAP_DISARMING);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.EFFECT_DURATION);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.EFFECT_RADIUS);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.KINETIC_IMMUNITY);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.KNOCKBACK_RESISTANCE);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.DURABILITY_WEAR_REDUCTION);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.ABILITY_POWER_PERCENTILE);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.HEALING_EFFECTIVENESS);
        SWORD_MODIFIERS_SUFFIX.add(UnusualModifierLib.HEXING_HIT);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("sword"), SWORD_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("sword"), SWORD_MODIFIERS_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("battlestaff"), SWORD_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("battlestaff"), SWORD_MODIFIERS_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("trident"), SWORD_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("trident"), SWORD_MODIFIERS_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("rang"), SWORD_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("rang"), SWORD_MODIFIERS_SUFFIX);



        ARMOR_MODIFIERS_PREFIX.add(UnusualModifierLib.MOVEMENT_SPEED);
        ARMOR_MODIFIERS_PREFIX.add(UnusualModifierLib.DAMAGE_BABY);
        ARMOR_MODIFIERS_PREFIX.add(UnusualModifierLib.COPIOUSLY);
        ARMOR_MODIFIERS_SUFFIX.add(UnusualModifierLib.KINETIC_IMMUNITY);
        ARMOR_MODIFIERS_SUFFIX.add(UnusualModifierLib.DURABILITY_WEAR_REDUCTION);
        ARMOR_MODIFIERS_SUFFIX.add(UnusualModifierLib.LUCKY_HIT_CHANCE);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("helmet"), ARMOR_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("helmet"), ARMOR_MODIFIERS_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("chestplate"), ARMOR_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("chestplate"), ARMOR_MODIFIERS_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("leggings"), ARMOR_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("leggings"), ARMOR_MODIFIERS_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("boots"), ARMOR_MODIFIERS_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("boots"), ARMOR_MODIFIERS_SUFFIX);

        NON_SACK_PREFIX.add(UnusualModifierLib.REACH);
        NON_SACK_PREFIX.add(UnusualModifierLib.MOVEMENT_SPEED);
        NON_SACK_PREFIX.add(UnusualModifierLib.RESISTANCE);
        NON_SACK_PREFIX.add(UnusualModifierLib.DAMAGE_BABY);
        NON_SACK_PREFIX.add(UnusualModifierLib.LEECH);
        NON_SACK_PREFIX.add(UnusualModifierLib.MANA_ADDITIVE);
        NON_SACK_PREFIX.add(UnusualModifierLib.MANA_REGEN);
        NON_SACK_PREFIX.add(UnusualModifierLib.COOLDOWN_REDUCTION);
        NON_SACK_PREFIX.add(UnusualModifierLib.AXE_CLEAVE);
        PLUSHIE_PREFIX.addAll(NON_SACK_PREFIX);
        NON_SACK_PREFIX.add(UnusualModifierLib.ABILITY_POWER);
        NON_SACK_PREFIX.add(UnusualModifierLib.CHAINING);
        NON_SACK_PREFIX.add(UnusualModifierLib.CRITICAL_HIT_MITIGATION);
        NON_SACK_SUFFIX.add(UnusualModifierLib.ITEM_QUANTITY);
        NON_SACK_SUFFIX.add(UnusualModifierLib.ITEM_RARITY);
        NON_SACK_SUFFIX.add(UnusualModifierLib.COPIOUSLY);
        NON_SACK_SUFFIX.add(UnusualModifierLib.TRAP_DISARMING);
        NON_SACK_SUFFIX.add(UnusualModifierLib.EFFECT_DURATION);
        NON_SACK_SUFFIX.add(UnusualModifierLib.EFFECT_RADIUS);
        NON_SACK_SUFFIX.add(UnusualModifierLib.KINETIC_IMMUNITY);
        NON_SACK_SUFFIX.add(UnusualModifierLib.KNOCKBACK_RESISTANCE);
        NON_SACK_SUFFIX.add(UnusualModifierLib.DURABILITY_WEAR_REDUCTION);
        NON_SACK_SUFFIX.add(UnusualModifierLib.HEALING_EFFECTIVENESS);
        PLUSHIE_SUFFIX.addAll(NON_SACK_SUFFIX);

        NON_SACK_SUFFIX.add(UnusualModifierLib.ABILITY_POWER_PERCENTILE);
        NON_SACK_PREFIX.add(UnusualModifierLib.DAMAGE_INCREASE);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("plushie"), PLUSHIE_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("plushie"), PLUSHIE_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("focus"), NON_SACK_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("focus"), NON_SACK_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("wand"), NON_SACK_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("wand"), NON_SACK_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("shield"), NON_SACK_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("shield"), NON_SACK_SUFFIX);

        SACK_PREFIX.add(UnusualModifierLib.RESISTANCE);
        SACK_PREFIX.add(UnusualModifierLib.DAMAGE_INCREASE);
        SACK_PREFIX.add(UnusualModifierLib.DAMAGE_BABY);
        SACK_PREFIX.add(UnusualModifierLib.LEECH);
        SACK_PREFIX.add(UnusualModifierLib.CRITICAL_HIT_MITIGATION);
        SACK_PREFIX.add(UnusualModifierLib.ABILITY_POWER);
        SACK_PREFIX.add(UnusualModifierLib.MANA_ADDITIVE);
        SACK_PREFIX.add(UnusualModifierLib.MANA_REGEN);
        SACK_PREFIX.add(UnusualModifierLib.COOLDOWN_REDUCTION);
        SACK_PREFIX.add(UnusualModifierLib.CHAINING);
        SACK_SUFFIX.add(UnusualModifierLib.EFFECT_DURATION);
        SACK_SUFFIX.add(UnusualModifierLib.EFFECT_RADIUS);
        SACK_SUFFIX.add(UnusualModifierLib.KINETIC_IMMUNITY);
        SACK_SUFFIX.add(UnusualModifierLib.KNOCKBACK_RESISTANCE);
        SACK_SUFFIX.add(UnusualModifierLib.DURABILITY_WEAR_REDUCTION);
        SACK_SUFFIX.add(UnusualModifierLib.ABILITY_POWER_PERCENTILE);
        SACK_SUFFIX.add(UnusualModifierLib.HEALING_EFFECTIVENESS);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("loot_sack"), SACK_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("loot_sack"), SACK_SUFFIX);
        UNUSUAL_MODIFIERS_MAP_PREFIX.put(VaultMod.id("magnet"), SACK_PREFIX);
        UNUSUAL_MODIFIERS_MAP_SUFFIX.put(VaultMod.id("magnet"), SACK_SUFFIX);
    }
}