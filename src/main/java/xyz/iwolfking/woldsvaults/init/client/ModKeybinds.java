package xyz.iwolfking.woldsvaults.init.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModKeybinds {
    public static KeyMapping isFeatherFixed;
    public static KeyMapping openWoldsVaultsConfig;

    public static void registerKeyBinds() {
        isFeatherFixed = registerKeyMapping("is_feather_fixed", -1);
        openWoldsVaultsConfig = registerKeyMapping("open_wolds_vaults_config", GLFW.GLFW_KEY_DELETE);
    }

    private static KeyMapping registerKeyMapping(String name, int keyCode) {
        KeyMapping key = new KeyMapping("key." + WoldsVaults.MOD_ID + "." + name, keyCode, "key.category." + WoldsVaults.MOD_ID);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
