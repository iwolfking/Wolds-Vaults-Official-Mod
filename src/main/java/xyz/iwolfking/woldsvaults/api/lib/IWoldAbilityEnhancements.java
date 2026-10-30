package xyz.iwolfking.woldsvaults.api.lib;

import java.util.List;

public interface IWoldAbilityEnhancements {
    void setTierLevel(int level);

    int getTierLevel();

    void setElementTypes(List<ElementalType> elementalTypes);
    List<ElementalType> getElementTypes();
}
