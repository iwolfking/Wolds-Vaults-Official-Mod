package xyz.iwolfking.woldsvaults.client.init;

import xyz.iwolfking.vhapi.api.extensions.client.VaultStashTabFactory;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultStashTabs {
    public static Enum<?> FRUIT_TAB;
    public static Enum<?> CRYSTAL_MODIFIERS_TAB;

    public static void init() {
        FRUIT_TAB = VaultStashTabFactory.INSTANCE.create(
                WoldsVaults.id("fruit"),
                "BOTTOM",
                1,
                1,
                false,
                "fruit"
        );

        CRYSTAL_MODIFIERS_TAB = VaultStashTabFactory.INSTANCE.create(
                WoldsVaults.id("crystal_modifiers"),
                "BOTTOM",
                2,
                2,
                false,
                "crystal_modifiers"
        );
    }
}
