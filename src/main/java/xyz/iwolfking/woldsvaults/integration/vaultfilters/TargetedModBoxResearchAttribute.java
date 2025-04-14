package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.item.BossRuneItem;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.TargetedModBox;

public class TargetedModBoxResearchAttribute extends StringAttribute {
    public TargetedModBoxResearchAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof TargetedModBox) {
            if(itemStack.getOrCreateTag().contains("research")) {
               return TargetedModBox.getResearchTag(itemStack);
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "targeted_mod_box_research";
    }
}
