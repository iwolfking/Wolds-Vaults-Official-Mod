package xyz.iwolfking.woldsvaults.integration.cctweaked;

import dan200.computercraft.api.ComputerCraftAPI;

public class CCTweakedSetup {
    public static void init() {
        ComputerCraftAPI.registerPeripheralProvider(new VaultPortalPeripheralProvider());
    }
}
