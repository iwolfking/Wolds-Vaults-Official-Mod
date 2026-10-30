package xyz.iwolfking.woldsvaults.client.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModKeybinds {
    public static KeyMapping isFeatherFixed;
    public static KeyMapping openWoldsVaultsConfig;
    public static KeyMapping toggleBetterCombat;
    public static KeyMapping openInventoryHUD;
    public static KeyMapping configureTrinket;
    public static KeyMapping openTrinketPouch;

    public static void registerKeyBinds() {
        isFeatherFixed = registerKeyMapping("is_feather_fixed", -1);
        openWoldsVaultsConfig = registerKeyMapping("open_wolds_vaults_config", GLFW.GLFW_KEY_DELETE);
        toggleBetterCombat = registerKeyMapping("toggle_better_combat", GLFW.GLFW_KEY_PERIOD);
        openInventoryHUD = registerKeyMapping("open_inventory_hud", GLFW.GLFW_KEY_O);
        configureTrinket = registerKeyMapping("configure_trinket", GLFW.GLFW_KEY_G, KeyConflictContext.GUI);
        openTrinketPouch = registerKeyMapping("open_trinket_pouch", GLFW.GLFW_KEY_T, KeyConflictContext.IN_GAME, KeyModifier.CONTROL);
    }

    private static KeyMapping registerKeyMapping(String name, int keyCode) {
        KeyMapping key = new KeyMapping("key." + WoldsVaults.MOD_ID + "." + name, keyCode, "key.category." + WoldsVaults.MOD_ID);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }

    private static KeyMapping registerKeyMapping(String name, int keyCode, IKeyConflictContext conflictContext) {
        KeyMapping key = new KeyMapping("key." + WoldsVaults.MOD_ID + "." + name, conflictContext, InputConstants.Type.KEYSYM, keyCode, "key.category." + WoldsVaults.MOD_ID);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }

    private static KeyMapping registerKeyMapping(String name, int keyCode, IKeyConflictContext conflictContext, KeyModifier modifier) {
        KeyMapping key = new KeyMapping("key." + WoldsVaults.MOD_ID + "." + name, conflictContext, modifier, InputConstants.Type.KEYSYM, keyCode, "key.category." + WoldsVaults.MOD_ID);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
