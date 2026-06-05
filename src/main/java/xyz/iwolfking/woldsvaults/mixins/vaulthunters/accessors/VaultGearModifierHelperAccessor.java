package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;

@Mixin(value = VaultGearModifierHelper.class, remap = false)
public interface VaultGearModifierHelperAccessor {
    @Invoker("reforgeBaseAttributesForNewLevel")
    static void callReforgeBaseAttributesForNewLevel(VaultGearData data, VaultGearTierConfig cfg, int newLevel, Random random) {

    }

    @Invoker("reforgeModifiersOfTypeForNewLevel")
    static void callReforgeModifiersOfTypeForNewLevel(VaultGearData data, VaultGearTierConfig cfg, VaultGearModifier.AffixType type, int newLevel, Random random) {

    }
}
