package xyz.iwolfking.woldsvaults.objectives.lib;

import iskallia.vault.item.crystal.objective.CrystalObjective;

// maybe this should be in the scaling seals mod
public interface ScalingObjective {
    int getMaxSealCount();
    int getSealCount();
    CrystalObjective increaseBy(int extraSeals);
}
