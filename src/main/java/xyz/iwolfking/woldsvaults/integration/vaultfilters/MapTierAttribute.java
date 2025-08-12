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

        VaultGearData data = VaultGearData.read(itemStack);
        if(data.hasAttribute(ModGearAttributes.MAP_TIER)) {{
            return data.getFirstValue(ModGearAttributes.MAP_TIER).orElse(null);
        }}

        return null;
    }

    public String getTranslationKey() {
        return "map_tier";
    }
}
