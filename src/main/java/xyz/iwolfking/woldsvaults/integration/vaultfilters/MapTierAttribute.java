package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import net.joseph.vaultfilters.attributes.abstracts.IntAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;

public class MapTierAttribute extends IntAttribute {

    public MapTierAttribute(Integer value) {
        super(value);
    }

    @Override
    public Integer getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof VaultGearItem)) {
            return null;
        }

        if(itemStack.getItem() instanceof VaultMapItem) {
            VaultGearData data = VaultGearData.read(itemStack);
            for (VaultGearModifier<?> modifier : data.getAllModifierAffixes()) {
                if(modifier.getAttribute() != null && modifier.getAttribute().equals(ModGearAttributes.MAP_TIER)) {
                    if(modifier.getValue() instanceof Integer tier) {
                        return tier;
                    }
                }

            }
        }

        return null;
    }

    public boolean appliesTo(ItemStack itemStack) {
        Integer value = this.getValue(itemStack);
        return value != null && value <= this.value;
    }

    public String getTranslationKey() {
        return "map_tier";
    }
}
