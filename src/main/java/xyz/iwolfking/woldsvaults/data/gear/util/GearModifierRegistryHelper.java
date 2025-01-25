package xyz.iwolfking.woldsvaults.data.gear.util;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.resources.ResourceLocation;

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
}
