package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;

import java.util.List;
import java.util.function.Consumer;

/** Emits a second copy of every rolled attribute on an offhand focus item, doubling its applied stats. */
public final class GodFocusGear {
    private GodFocusGear() {
    }

    public static void copyAttributes(ItemStack stack, GodStatSink sink) {
        copyAttributes(stack, sink::add);
    }

    public static void copyAttributes(ItemStack stack, List<VaultGearAttributeInstance<?>> out) {
        copyAttributes(stack, out::add);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyAttributes(ItemStack stack, Consumer<VaultGearAttributeInstance<?>> out) {
        AttributeGearData data = AttributeGearData.read(stack);
        for (VaultGearAttribute attribute : VaultGearAttributeRegistry.getRegistry()) {
            List<?> values = (List<?>) data.get(attribute, VaultGearAttributeTypeMerger.asList());
            for (Object value : values) {
                try {
                    out.accept(VaultGearAttributeInstance.cast(attribute, value));
                } catch (ClassCastException e) {
                    WoldsVaults.LOGGER.error("Skipping focus attribute {} while doubling {}: value {} did not cast.",
                            attribute.getRegistryName(), stack.getItem().getRegistryName(), value, e);
                }
            }
        }
    }
}
