package xyz.iwolfking.woldsvaults.armory;

/**
 * The armory's top tabs, in strip order. Gear, Inventory and Excess are views of the one {@code ArmoryMenu};
 * the tree tabs open their own read-only menus.
 */
public enum ArmoryTab {
    GEAR,
    INVENTORY,
    ABILITIES,
    TALENTS,
    EXPERTISES,
    PRESTIGE,
    GODS,
    EXCESS;

    public boolean isContainerView() {
        return this == GEAR || this == INVENTORY || this == EXCESS;
    }

    public static ArmoryTab byIndex(int index) {
        ArmoryTab[] values = values();
        return index >= 0 && index < values.length ? values[index] : GEAR;
    }
}
