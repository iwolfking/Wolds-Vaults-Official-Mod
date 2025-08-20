package xyz.iwolfking.woldsvaults.integration.vaultfilters.companion;

import iskallia.vault.item.CompanionItem;
import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;

public class CompanionCooldownAttribute extends BooleanAttribute {


    public CompanionCooldownAttribute(Boolean value) {
        super(value);
    }

    @Override
    public String getTranslationKey() {
        return "companion_cooldown";
    }

    @Override
    public Boolean getValue(ItemStack itemStack) {
        if(!(itemStack.getItem() instanceof CompanionItem)) {
            return null;
        }

        return CompanionItem.isOnCooldown(itemStack);
    }
}
