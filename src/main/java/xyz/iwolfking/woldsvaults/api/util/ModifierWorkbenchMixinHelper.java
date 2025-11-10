package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public class ModifierWorkbenchMixinHelper {
    public static int getCraftedModifierCount(ItemStack gear) {
        if (!gear.isEmpty() && AttributeGearData.hasData(gear)) {
            VaultGearData data = VaultGearData.read(gear);
            Iterator<VaultGearModifier<?>> modifiers = data.getAllModifierAffixes().iterator();
            int modCount = 0;

            while (modifiers.hasNext()) {
                VaultGearModifier<?> mod = modifiers.next();
                if (mod.hasCategory(VaultGearModifier.AffixCategory.CRAFTED)) {
                    modCount++;
                }
            }

            return modCount;
        }
        return 0;
    }
}
