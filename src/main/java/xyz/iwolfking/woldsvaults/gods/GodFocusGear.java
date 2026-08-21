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

/**
 * Reads every rolled attribute off an offhand focus item (plushie, loot sack) and emits a second
 * copy of each. Adding the copy alongside the gear pass's own contribution is what "doubles the
 * applied stats" means, and it reuses the exact iteration the snapshot calculator performs for
 * the vault charm multiplier.
 *
 * <p>Shared rather than per tree: Wendarr's Plushie Lover and Tenos's Sacked shipped
 * byte-identical private copies of this, which is the class of duplication the god node
 * rearchitecture exists to remove. It lives in the god core beside {@link GodCarryover} because
 * both callers are stat contributions folded into the same snapshot.
 */
public final class GodFocusGear {
    private GodFocusGear() {
    }

    /** Emits the second copy into a {@code StatContributor}'s sink. */
    public static void copyAttributes(ItemStack stack, GodStatSink sink) {
        copyAttributes(stack, sink::add);
    }

    /** Emits the second copy into a plain instance list, for a caller outside the sink seam. */
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
