package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.item.tool.LeanJewels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LeanJewels.class, remap = false)
public class MixinLeanJewels {

    @WrapOperation(
        method = "lambda$craftedAffix$11",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/gear/attribute/VaultGearModifier;cast(Liskallia/vault/gear/attribute/VaultGearAttribute;Ljava/lang/Object;)Liskallia/vault/gear/attribute/VaultGearModifier;"
        )
    )
    private static <T> VaultGearModifier<T> woldsVaults$safeCraftedAffixFallback(
            VaultGearAttribute<T> attribute,
            Object value,
            Operation<VaultGearModifier<T>> original
    ) {
        if (value instanceof Boolean) {
            Object fallback = woldsVaults$resolveNonBooleanDefault(attribute, (Boolean) value);
            if (fallback != null) {
                return original.call(attribute, fallback);
            }
        }
        return original.call(attribute, value);
    }

    @Unique
    private static <T> Object woldsVaults$resolveNonBooleanDefault(VaultGearAttribute<T> attribute, boolean boolFallback) {
        Object[] candidates = { 0.0F, 0, 0.0D, 0L, boolFallback };
        for (Object candidate : candidates) {
            try {
                attribute.getType().cast(candidate);
                return candidate;
            } catch (Exception ignored) {
            }
        }
        return boolFallback;
    }
}