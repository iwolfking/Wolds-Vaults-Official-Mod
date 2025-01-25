package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.etching.EtchingSet;
import iskallia.vault.gear.VaultGearClassification;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.OfferingItem;
import iskallia.vault.item.gear.EtchingItem;
import iskallia.vault.util.StringUtils;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;

public class EtchingSetAttribute extends StringAttribute {
    public EtchingSetAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof VaultGearItem vaultGearItem) {
            if(vaultGearItem.getClassification(itemStack).equals(VaultGearClassification.ARMOR)) {
                VaultGearData data = VaultGearData.read(itemStack);
                EtchingSet<?> etchingSet = data.getFirstValue(ModGearAttributes.ETCHING).orElse(null);
                if(!(etchingSet == null)) {
                    return StringUtils.convertToTitleCase(etchingSet.getRegistryName().getPath());
                }
            }
        }
        else if(itemStack.getItem() instanceof EtchingItem etchingItem) {
            AttributeGearData data = AttributeGearData.read(itemStack);
            EtchingSet<?> etchingSet = data.getFirstValue(ModGearAttributes.ETCHING).orElse(null);
            if(!(etchingSet == null)) {
                return StringUtils.convertToTitleCase(etchingSet.getRegistryName().getPath());
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "etching_set";
    }
}
