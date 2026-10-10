package xyz.iwolfking.woldsvaults.api.lib;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

public enum ElementalType {
    FIRE(ModGearAttributes.FIRE_ELEMENT, WoldActiveFlags.FIRE_ELEMENT_ATTACK);

    private final VaultGearAttribute<Boolean> attribute;
    private final WoldActiveFlags flag;

    ElementalType(VaultGearAttribute<Boolean> associatedGearAttribute, WoldActiveFlags associatedFlag) {
        this.attribute = associatedGearAttribute;
        this.flag = associatedFlag;
    }

    public VaultGearAttribute<Boolean> getAttribute() {
        return attribute;
    }

    public WoldActiveFlags getFlag() {
        return flag;
    }
}
