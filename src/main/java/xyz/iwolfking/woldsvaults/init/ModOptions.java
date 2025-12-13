package xyz.iwolfking.woldsvaults.init;

import com.google.gson.JsonElement;
import iskallia.vault.VaultMod;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.core.data.adapter.IJsonAdapter;
import iskallia.vault.options.VaultOption;
import iskallia.vault.options.VaultOptionsRegistry;
import iskallia.vault.options.types.InventoryHudElementOptions;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.function.Consumer;

public class ModOptions {
    public static final VaultOption<InventoryHudElementOptions> GREEN_TRINKET = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_trinket_green"), InventoryHudElementOptions.createDefault(new HudPosition(0.5F, 0.5F)), null, InventoryHudElementOptions.ADAPTER);
    public static final VaultOption<InventoryHudElementOptions> TRINKET_POUCH = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_trinket_pouch"), InventoryHudElementOptions.createDefault(new HudPosition(0.5F, 0.5F)), null, InventoryHudElementOptions.ADAPTER);
    public static final VaultOption<InventoryHudElementOptions> SHARD_POUCH = VaultOptionsRegistry.register("woldsOptions.json", WoldsVaults.id("inv_hud_shard_pouch"), InventoryHudElementOptions.createDefault(new HudPosition(0.5F, 0.5F)), null, InventoryHudElementOptions.ADAPTER);

    public static void init() {
        VaultOptionsRegistry.loadOptions();
    }
}
