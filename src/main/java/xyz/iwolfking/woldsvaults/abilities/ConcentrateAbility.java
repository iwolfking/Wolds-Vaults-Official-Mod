package xyz.iwolfking.woldsvaults.abilities;

import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;

public class ConcentrateAbility extends InstantManaAbility {
    public ConcentrateAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
    }

    public ConcentrateAbility() {
    }
}
