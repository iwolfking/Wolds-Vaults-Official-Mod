package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshot;

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

    public static <T> float resolveSnapshotValue(AttributeSnapshot snapshot, VaultGearAttribute<T> attribute) {
        Object value = snapshot.getAttributeValue(attribute, (VaultGearAttributeTypeMerger<T, Object>) (Object) VaultGearAttributeTypeMerger.floatSum());
        if (value instanceof Number num) {
            return num.floatValue();
        }
        return 0.0F;
    }
}
