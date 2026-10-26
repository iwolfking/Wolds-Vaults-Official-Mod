package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;

import java.util.List;
import java.util.function.Predicate;

public class VaultGearAttributeHelper {
    public static List<VaultGearAttribute<?>> getAllRegisteredAttributes(
            Predicate<VaultGearAttribute<?>> attributePredicate,
            Class<?> targetConfigClass) {

        return VaultGearAttributeRegistry.getRegistry().getValues().stream()
                .filter(attributePredicate)
                .filter(attr -> {
                    if (attr == null || attr.getGenerator() == null) {
                        return false;
                    }
                    Class<?> configClass = attr.getGenerator().getConfigurationObjectClass();
                    return configClass != null && targetConfigClass.isAssignableFrom(configClass);
                })
                .toList();
    }
}
