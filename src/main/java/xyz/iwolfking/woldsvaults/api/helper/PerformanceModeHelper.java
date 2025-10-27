package xyz.iwolfking.woldsvaults.api.helper;

import iskallia.vault.core.vault.ClientVaults;
import net.minecraftforge.common.ForgeConfigSpec;

public class PerformanceModeHelper {
    public static boolean shouldShowInVault(ForgeConfigSpec.ConfigValue<Boolean> configValue) {
        if(!configValue.get()) {
            return false;
        }

        return ClientVaults.getActive().isPresent();
    }
}
