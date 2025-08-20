package xyz.iwolfking.woldsvaults.integration.vaultfilters.companion;

import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.item.CompanionSeries;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CompanionSeriesAttribute extends StringAttribute {
    public CompanionSeriesAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof CompanionItem)) {
            return null;
        }

        CompanionSeries series = CompanionItem.getPetSeries(itemStack);


        return series.getDisplayName();
    }

    @Override
    public String getTranslationKey() {
        return "companion_series";
    }
}
