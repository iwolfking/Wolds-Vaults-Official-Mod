package xyz.iwolfking.woldsvaults.data.gear.util;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.config.DoubleAttributeGenerator;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.gear.attribute.config.IntegerAttributeGenerator;
import iskallia.vault.gear.attribute.custom.effect.EffectCloudAttribute;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ModifierGearTierAccessor;

import java.util.ArrayList;
import java.util.List;

public class GearModifierRegistryHelper {
    private static final String INVALID_MODIFIER_TIER = "Attempted to create an invalid modifier tier, skipping!";

    public static VaultGearTierConfig.ModifierTierGroup create(ResourceLocation modifierTypeId, String modifierGroup, ResourceLocation modifierId) {
        return create(modifierTypeId, modifierGroup, modifierId, false);
    }

    public static VaultGearTierConfig.ModifierTierGroup create(ResourceLocation modifierTypeId, String modifierGroup, ResourceLocation modifierId, boolean hasLegendary) {
        VaultGearTierConfig.ModifierTierGroup group = new VaultGearTierConfig.ModifierTierGroup(modifierTypeId, modifierGroup, modifierId);
        if(!hasLegendary) {
            group.getTags().add("noLegendary");
        }
        return group;
    }

    public static <T> VaultGearTierConfig.ModifierTier<T> createTier(Integer minLevel, Integer maxLevel, Integer weight, Integer modifierTier, T config) {
        VaultGearTierConfig.ModifierTier<T> tier = new VaultGearTierConfig.ModifierTier<T>(minLevel, weight, config);

        if(maxLevel != null) {
            ((ModifierGearTierAccessor)tier).setMaxLevel(maxLevel);
        }

        if(modifierTier != null) {
            ((ModifierGearTierAccessor)tier).setModifierTier(modifierTier);
        }

        return tier;
    }

    public static List<VaultGearTierConfig.ModifierTier<?>> createEffectCloudTiers(String tooltipDisplayName, ResourceLocation potion, List<Integer> minLevels, List<Integer> maxLevels, List<Integer> durationValues, List<Float> radiusValues, int color, boolean affectsOwner, List<Float> triggerChanceValues, ResourceLocation additionalEffect, List<Integer> additionalEffectDurationValues, List<Integer> additionalEffectAmplifierValues, int weight) {
        if (minLevels.isEmpty() || maxLevels.isEmpty() || radiusValues.isEmpty() || (minLevels.size() != maxLevels.size()) || (minLevels.size() != radiusValues.size())) {
            logInvalidModifierTier();
            return List.of();
        }

        List<VaultGearTierConfig.ModifierTier<?>> modifierTiers = new ArrayList<>();

        for(int i =0; i < maxLevels.size(); i++) {
            EffectCloudAttribute.CloudConfig config = new EffectCloudAttribute.CloudConfig(tooltipDisplayName, potion, durationValues.get(i), radiusValues.get(i), color, affectsOwner, triggerChanceValues.get(i));
            config.setAdditionalEffect(new EffectCloudAttribute.CloudEffectConfig(additionalEffect, additionalEffectDurationValues.get(i), additionalEffectAmplifierValues.get(i)));
            VaultGearTierConfig.ModifierTier<?> tier = createTier(minLevels.get(i), maxLevels.get(i), weight, i, config);
            modifierTiers.add(tier);
        }

        return modifierTiers;
    }

    public static List<VaultGearTierConfig.ModifierTier<AbilityLevelAttribute.Config>> createAbilityLevelTiers(String abilityName, List<Integer> minLevels, List<Integer> maxLevels, List<Integer> values, int weight) {
        if (minLevels.isEmpty() || maxLevels.isEmpty() || values.isEmpty() || (minLevels.size() != maxLevels.size()) || (minLevels.size() != values.size())) {
            logInvalidModifierTier();
            return List.of();
        }

        List<VaultGearTierConfig.ModifierTier<AbilityLevelAttribute.Config>> modifierTiers = new ArrayList<>();

        for(int i =0; i < maxLevels.size(); i++) {
            VaultGearTierConfig.ModifierTier<AbilityLevelAttribute.Config> tier = createTier(minLevels.get(i), maxLevels.get(i), weight, i, new AbilityLevelAttribute.Config(abilityName, values.get(i)));
            modifierTiers.add(tier);
        }

        return modifierTiers;
    }

    public static List<VaultGearTierConfig.ModifierTier<?>> createNumberRangeTiers(List<Integer> levels, List<Integer> maxLevels, List<Number> minValues, List<Number> maxValues, Number step, int weight) {
        List<VaultGearTierConfig.ModifierTier<?>> modifierTiers = new ArrayList<>();
        if (levels.isEmpty() || minValues.isEmpty() || maxValues.isEmpty() || (minValues.size() != maxValues.size()) || (levels.size() != maxLevels.size())) {
            logInvalidModifierTier();
            return List.of();
        }

        for (int i = 0; i < levels.size(); i++) {
            VaultGearTierConfig.ModifierTier<?> tier = null;
            if (step instanceof Integer) {
                tier = createTier(levels.get(i), maxLevels.get(i), weight, i, new IntegerAttributeGenerator.Range(minValues.get(i).intValue(), maxValues.get(i).intValue(), step.intValue()));
            } else if (step instanceof Float) {
                tier = createTier(levels.get(i), maxLevels.get(i), weight, i, new FloatAttributeGenerator.Range(minValues.get(i).floatValue(), maxValues.get(i).floatValue(), step.floatValue()));
            } else if (step instanceof Double) {
                tier = createTier(levels.get(i), maxLevels.get(i), weight, i, new DoubleAttributeGenerator.Range(minValues.get(i).doubleValue(), maxValues.get(i).doubleValue(), step.doubleValue()));
            }

            if (tier == null) {
                continue;
            }

            modifierTiers.add(tier);
        }

        return modifierTiers;
    }

    private static void logInvalidModifierTier() {
        WoldsVaults.LOGGER.warn(INVALID_MODIFIER_TIER);
    }

}
