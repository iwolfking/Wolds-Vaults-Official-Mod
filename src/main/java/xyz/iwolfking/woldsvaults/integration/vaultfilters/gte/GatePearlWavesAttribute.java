package xyz.iwolfking.woldsvaults.integration.vaultfilters.gte;

import net.joseph.vaultfilters.attributes.abstracts.IntAttribute;
import net.joseph.vaultfilters.attributes.abstracts.StringAttribute;
import net.joseph.vaultfilters.attributes.gear.GearRepairSlotAttribute;
import net.joseph.vaultfilters.attributes.jewel.JewelSizeAttribute;
import net.minecraft.world.item.ItemStack;
import shadows.gateways.gate.Gateway;
import shadows.gateways.item.GatePearlItem;

public class GatePearlWavesAttribute extends IntAttribute {
    public GatePearlWavesAttribute(Integer value) {
        super(value);
    }

    @Override
    public Integer getValue(ItemStack itemStack) {
        if(itemStack.getItem() instanceof GatePearlItem) {
            Gateway gate = GatePearlItem.getGate(itemStack);
            if(gate == null) {
                return null;
            }
            else {
                return gate.getNumWaves();
            }
        }
        return null;
    }

    public boolean appliesTo(ItemStack itemStack) {
        Integer value = this.getValue(itemStack);
        return value != null && value <= (Integer)this.value;
    }


    @Override
    public String getTranslationKey() {
        return "gte_gate_wave_count";
    }
}
