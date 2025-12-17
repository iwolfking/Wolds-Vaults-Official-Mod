package xyz.iwolfking.woldsvaults.integration.bettercombat;

import me.fallenbreath.conditionalmixin.api.mixin.ConditionTester;
import net.minecraftforge.fml.loading.LoadingModList;

public class NoBetterCombatTester implements ConditionTester {
    @Override
    public boolean isSatisfied(String s) {
        return LoadingModList.get().getModFileById("bettercombat") == null;
    }

}
