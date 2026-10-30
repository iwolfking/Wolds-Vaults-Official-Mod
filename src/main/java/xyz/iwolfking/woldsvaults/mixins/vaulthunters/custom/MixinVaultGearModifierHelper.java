package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Mixin(value = VaultGearModifierHelper.class, remap = false)
public class MixinVaultGearModifierHelper {
    @WrapOperation(method = "generateChaoticModifiers", at = @At(value = "INVOKE", target = "Liskallia/vault/config/gear/VaultGearTierConfig;getRandomModifier(Liskallia/vault/gear/attribute/VaultGearModifier$AffixType;ILjava/util/Random;Ljava/util/Set;)Ljava/util/Optional;"))
    private static Optional<VaultGearModifier<?>> addChaos(VaultGearTierConfig instance, VaultGearModifier.AffixType type, int level, Random random, Set<String> excludedModGroups, Operation<Optional<VaultGearModifier<?>>> original) {

        float value = random.nextFloat();

        if(value <= 0.5F) {
            return original.call(instance, type, level, random, excludedModGroups);
        }
        else if(value <= 0.65F) {
            return instance.getRandomModifier(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT, level, random, excludedModGroups);
        }
        else if(value <= 0.8F) {
            if(type.equals(VaultGearModifier.AffixType.PREFIX)) {
                return instance.getRandomModifier(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), level, random, excludedModGroups);
            }
            else if(type.equals(VaultGearModifier.AffixType.SUFFIX)) {
                return instance.getRandomModifier(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), level, random, excludedModGroups);
            }
            else {
                return original.call(instance, type, level, random, excludedModGroups);
            }
        }

        return original.call(instance, type, level, random, excludedModGroups);
    }
}
