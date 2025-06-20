package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

public class HasDivineAttribute extends BooleanAttribute {
    public HasDivineAttribute(Boolean value) {
        super(true);
    }

    @Override
    public Boolean getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof VaultGearItem)) {
            return null;
        }

        VaultGearData data = VaultGearData.read(itemStack);
        return data.hasAttribute(ModGearAttributes.DIVINE);
    }

    public String getTranslationKey() {
        return "has_divine";
    }
}
