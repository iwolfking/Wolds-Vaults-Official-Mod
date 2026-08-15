package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;

/**
 * Reads every rolled attribute off an offhand focus item (plushie, loot sack) and emits a second
 * copy of each. Adding the copy alongside the gear pass's own contribution is what "doubles the
 * applied stats" means, and it reuses the exact iteration the snapshot calculator performs for
 * the vault charm multiplier.
 */
final class WendarrFocusGear {
    private WendarrFocusGear() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void copyAttributes(ItemStack stack, List<VaultGearAttributeInstance<?>> out) {
        AttributeGearData data = AttributeGearData.read(stack);
        for (VaultGearAttribute attribute : VaultGearAttributeRegistry.getRegistry()) {
            List<?> values = (List<?>) data.get(attribute, VaultGearAttributeTypeMerger.asList());
            for (Object value : values) {
                try {
                    out.add(VaultGearAttributeInstance.cast(attribute, value));
                } catch (ClassCastException e) {
                    WoldsVaults.LOGGER.error("Skipping focus attribute {} while doubling {}: value {} did not cast.",
                            attribute.getRegistryName(), stack.getItem().getRegistryName(), value, e);
                }
            }
        }
    }
}
