package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.client.render.HudPosition;
import iskallia.vault.options.VaultOption;
import iskallia.vault.options.VaultOptionsRegistry;
import iskallia.vault.options.types.InventoryHudElementOptions;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModOptions {
    public static final VaultOption<InventoryHudElementOptions> GREEN_TRINKET = VaultOptionsRegistry.registerInventoryHudElementOptions(WoldsVaults.id("inv_hud_trinket_green"), InventoryHudElementOptions.createDefault(new HudPosition(0.058333334F, 0.46640316F)).setDisplayMode(InventoryHudElementOptions.DisplayMode.DURABILITY_LOW));

}
