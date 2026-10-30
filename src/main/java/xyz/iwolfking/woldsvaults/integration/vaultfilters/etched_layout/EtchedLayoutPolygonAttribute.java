package xyz.iwolfking.woldsvaults.integration.vaultfilters.etched_layout;

import iskallia.vault.item.crystal.layout.ClassicPolygonCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicPolygonCrystalLayoutAccessor;

import java.util.Arrays;

public class EtchedLayoutPolygonAttribute extends StringAttribute {
    public EtchedLayoutPolygonAttribute(String value) {
        super(value);
    }


    @Override
    public String getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof LayoutModificationItem) {
            CrystalLayout layout = LayoutModificationItem.getLayout(itemStack).orElse(null);
            if(layout == null) {
                return null;
            }

            if(layout instanceof ClassicPolygonCrystalLayout classicPolygonCrystalLayout) {
              return Arrays.toString(((ClassicPolygonCrystalLayoutAccessor) classicPolygonCrystalLayout).getVertices());
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "etched_layout_polygon_value";
    }
}
