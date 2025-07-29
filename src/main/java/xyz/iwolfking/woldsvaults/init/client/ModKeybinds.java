package xyz.iwolfking.woldsvaults.init.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModKeybinds {
    public static KeyMapping isFeatherFixed;
    public static KeyMapping openWoldsVaultsConfig;

    public static void registerKeyBinds() {
        isFeatherFixed = registerKeyMapping("is_feather_fixed", -1);
        openWoldsVaultsConfig = registerKeyMapping("open_wolds_vaults_config", 127);
    }

    private static KeyMapping registerKeyMapping(String name, int keyCode) {
        KeyMapping key = new KeyMapping("key." + WoldsVaults.MOD_ID + "." + name, keyCode, "key.category." + WoldsVaults.MOD_ID);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
