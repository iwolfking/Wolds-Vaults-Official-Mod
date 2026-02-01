package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.client.render.HudPosition;
import iskallia.vault.options.VaultOption;
import iskallia.vault.options.VaultOptionsRegistry;
import iskallia.vault.options.types.InventoryHudElementOptions;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.invhud.LightmanWalletHudOptions;

public class ModOptions {
    public static final VaultOption<InventoryHudElementOptions> GREEN_TRINKET = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_trinket_green"), InventoryHudElementOptions.createDefault(new HudPosition(0.5F, 0.5F)), null, InventoryHudElementOptions.ADAPTER);
    public static final VaultOption<InventoryHudElementOptions> TRINKET_POUCH = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_trinket_pouch"), InventoryHudElementOptions.createDefault(new HudPosition(0.5F, 0.5F)), null, InventoryHudElementOptions.ADAPTER);
    public static final VaultOption<LightmanWalletHudOptions> LIGHTMAN_WALLET = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_lightman_wallet"), LightmanWalletHudOptions.createDefault(new HudPosition(0.7F, 0.13F)), null, LightmanWalletHudOptions.ADAPTER);

    public static void init() {
        VaultOptionsRegistry.loadOptions();
    }
}
