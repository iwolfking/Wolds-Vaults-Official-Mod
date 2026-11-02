package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.client.render.HudPosition;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.options.VaultOption;
import iskallia.vault.options.VaultOptionsRegistry;
import iskallia.vault.options.types.InventoryHudElementOptions;
import iskallia.vault.util.Alignment;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.invhud.BossDpsHudOptions;
import xyz.iwolfking.woldsvaults.client.invhud.LightmanWalletHudOptions;

public class ModOptions {
    public static final VaultOption<InventoryHudElementOptions> GREEN_TRINKET = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_trinket_green"), InventoryHudElementOptions.createDefault(HudPosition.of(Alignment.Horizontal.CENTER, Alignment.Vertical.BOTTOM, 139.0F, -42.0F)), null, InventoryHudElementOptions.ADAPTER);
    public static final VaultOption<InventoryHudElementOptions> TRINKET_POUCH = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_trinket_pouch"), InventoryHudElementOptions.createDefault(HudPosition.of(Alignment.Horizontal.CENTER, Alignment.Vertical.BOTTOM, 160.0F, -42.0F)), null, InventoryHudElementOptions.ADAPTER);
    public static final VaultOption<LightmanWalletHudOptions> LIGHTMAN_WALLET = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_lightman_wallet"), LightmanWalletHudOptions.createDefault(HudPosition.of(Alignment.Horizontal.CENTER, Alignment.Vertical.BOTTOM, 271.0F, -14.0F)), null, LightmanWalletHudOptions.ADAPTER);
    public static final VaultOption<BossDpsHudOptions> BOSS_DPS = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_boss_dps"), BossDpsHudOptions.createDefault(HudPosition.of(Alignment.Horizontal.LEFT, Alignment.Vertical.MIDDLE, 4.0F, 0.0F)), null, BossDpsHudOptions.ADAPTER);
    public static final VaultOption<Integer> PICKUP_NOTIFIER_ITEM_COUNT = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("pickup_notifier_item_count"), 5, null, Adapters.INT);

    public static void init() {
        VaultOptionsRegistry.loadOptions();
    }
}
