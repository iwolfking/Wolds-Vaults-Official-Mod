package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.gear.data.VaultGearData;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;

import java.util.ArrayList;
import java.util.List;

public class ElementHelper {
    public static List<ElementalType> getAllElementsFromGear(VaultGearData data) {
        List<ElementalType> elementalTypes = new ArrayList<>();
        for(ElementalType type : ElementalType.values()) {
            if(data.hasAttribute(type.getAttribute())) {
                elementalTypes.add(type);
            }
        }
        return elementalTypes;
    }

    public static void runWithElements(List<ElementalType> elementalTypes, Runnable action) {
        runWithElementsRecursive(elementalTypes, 0, action);
    }

    private static void runWithElementsRecursive(List<ElementalType> types, int index, Runnable action) {
        if (index >= types.size()) {
            action.run();
            return;
        }

        ElementalType type = types.get(index);
        type.getFlag().runWithFlag(() -> runWithElementsRecursive(types, index + 1, action));
    }
}
