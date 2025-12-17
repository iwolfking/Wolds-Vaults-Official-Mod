package xyz.iwolfking.woldsvaults.integration.infernalmobs;

import me.fallenbreath.conditionalmixin.api.mixin.ConditionTester;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class DevEnvTester implements ConditionTester {
    @Override public boolean isSatisfied(String s) {
        return !FMLEnvironment.production;
    }
}
