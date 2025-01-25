package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.antique.Antique;
import iskallia.vault.config.AntiquesConfig;
import iskallia.vault.item.AntiqueItem;
import iskallia.vault.item.OfferingItem;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;

public class AntiqueAttribute extends StringAttribute {
    public AntiqueAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof AntiqueItem) {
            Antique antique = AntiqueItem.getAntique(itemStack);

            if(antique == null) {
                return null;
            }

            AntiquesConfig.Entry entry = antique.getConfig();

            if(entry != null) {
                if(entry.getInfo() != null) {
                    return entry.getInfo().getName();
                }
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "antique_name";
    }
}
