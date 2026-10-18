package xyz.iwolfking.woldsvaults.integration.vaultfilters.etched_layout;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.joseph.vaultfilters.attributes.abstracts.IntAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicRingsCrystalLayout;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;

public class EtchedLayoutRingsAttribute extends IntAttribute {
    public EtchedLayoutRingsAttribute(Integer value) {
        super(value);
    }

    @Override
    protected NumComparator getComparator() {
        return NumComparator.AT_LEAST;
    }


    @Override
    public Integer getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof LayoutModificationItem) {
            CrystalLayout layout = LayoutModificationItem.getLayout(itemStack).orElse(null);
            if(layout == null) {
                return null;
            }

            if(layout instanceof ClassicRingsCrystalLayout classicLayout) {
              return classicLayout.getRadius();
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "etched_layout_rings_radius";
    }
}
