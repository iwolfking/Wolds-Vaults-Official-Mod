package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.joseph.vaultfilters.attributes.abstracts.IntAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicInfiniteCrystalLayoutAccessor;

public class EtchedLayoutTunnelAttribute extends IntAttribute {
    public EtchedLayoutTunnelAttribute(Integer value) {
        super(value);
    }

    @Override
    protected NumComparator getComparator() {
        return NumComparator.EQUAL;
    }

    @Override
    public Integer getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof LayoutModificationItem) {
            CrystalLayout layout = LayoutModificationItem.getLayout(itemStack).orElse(null);
            if(layout != null) {
                return ((ClassicInfiniteCrystalLayoutAccessor)layout).getTunnelSpan();
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "etched_layout_tunnel";
    }
}
