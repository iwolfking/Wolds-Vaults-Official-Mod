package xyz.iwolfking.woldsvaults.data.gear.util;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.config.DoubleAttributeGenerator;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.gear.attribute.config.IntegerAttributeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ModifierGearTierAccessor;

import java.util.ArrayList;
import java.util.List;

public class GearModifierRegistryHelper {
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

    public static List<VaultGearTierConfig.ModifierTier<?>> createTiers(List<Integer> levels, List<Integer> maxLevels, List<Number> minValues, List<Number> maxValues, Number step, int weight) {
        List<VaultGearTierConfig.ModifierTier<?>> modifierTiers = new ArrayList<>();
        if (levels.isEmpty() || minValues.isEmpty() || maxValues.isEmpty() || (minValues.size() != maxValues.size())) {
            return List.of();
        }

        for (int i = 0; i < levels.size(); i++) {
            VaultGearTierConfig.ModifierTier<?> tier = null;
            if (step instanceof Integer) {
                tier = new VaultGearTierConfig.ModifierTier<>(levels.get(i), weight, new IntegerAttributeGenerator.Range(minValues.get(i).intValue(), maxValues.get(i).intValue(), step.intValue()));
            } else if (step instanceof Float) {
                tier = new VaultGearTierConfig.ModifierTier<>(levels.get(i), weight, new FloatAttributeGenerator.Range(minValues.get(i).floatValue(), maxValues.get(i).floatValue(), step.floatValue()));
            } else if (step instanceof Double) {
                tier = new VaultGearTierConfig.ModifierTier<>(levels.get(i), weight, new DoubleAttributeGenerator.Range(minValues.get(i).doubleValue(), maxValues.get(i).doubleValue(), step.doubleValue()));
            }

            if (tier == null) {
                continue;
            }

            if(maxLevels.get(i) != null) {
                ((ModifierGearTierAccessor)tier).setMaxLevel(maxLevels.get(i));
            }

            modifierTiers.add(tier);
        }

        return modifierTiers;
    }
}
