package xyz.iwolfking.woldsvaults.integration.vaultfilters.etched_layout;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.joseph.vaultfilters.attributes.abstracts.DoubleAttribute;
import net.joseph.vaultfilters.attributes.abstracts.IntAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveCrystalLayout;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;

public class EtchedLayoutWaveFrequencyAttribute extends DoubleAttribute {
    public EtchedLayoutWaveFrequencyAttribute(Double value) {
        super(value);
    }

    @Override
    protected IntAttribute.NumComparator getComparator() {
        return IntAttribute.NumComparator.AT_LEAST;
    }


    @Override
    public Double getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof LayoutModificationItem) {
            CrystalLayout layout = LayoutModificationItem.getLayout(itemStack).orElse(null);
            if(layout == null) {
                return null;
            }

            if(layout instanceof ClassicWaveCrystalLayout classicLayout) {
              return classicLayout.getFrequency();
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "etched_layout_wave_frequency";
    }
}
