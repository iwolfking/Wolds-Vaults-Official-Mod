package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;

public class WoldVaultUtils {
    public static Objective getObjective(Vault vault, Class<? extends Objective> objectiveClass) {
        if (vault == null) {
            return null;
        } else {
            Objectives objectives = vault.get(Vault.OBJECTIVES);
            if(objectives == null) {
                return null;
            }

            for(Objective objective : objectives.get(Objectives.LIST)) {
                if(objective.getClass().equals(objectiveClass)) {
                    return objective;
                }
            }
        }

        return null;
    }
}
