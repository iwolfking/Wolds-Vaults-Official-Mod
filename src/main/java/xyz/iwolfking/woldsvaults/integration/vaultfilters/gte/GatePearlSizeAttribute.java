package xyz.iwolfking.woldsvaults.integration.vaultfilters.gte;

import iskallia.vault.item.OfferingItem;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.minecraft.world.item.ItemStack;
import shadows.gateways.gate.Gateway;
import shadows.gateways.item.GatePearlItem;

public class GatePearlSizeAttribute extends StringAttribute {
    public GatePearlSizeAttribute(String value) {
        super(value);
    }

    @Override
    public String getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof GatePearlItem) {
            Gateway gate = GatePearlItem.getGate(itemStack);
            if(gate == null) {
                return null;
            }
            else {
                return gate.getSize().name();
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey() {
        return "gte_gate_pearl_size";
    }
}
