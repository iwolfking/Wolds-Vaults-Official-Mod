package xyz.iwolfking.woldsvaults.client.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.spellcraft.keys.SlotKeyMapping;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

public class ModKeybinds {
    public static KeyMapping isFeatherFixed;
    public static KeyMapping openWoldsVaultsConfig;
    public static KeyMapping toggleBetterCombat;
    public static KeyMapping openInventoryHUD;
    public static KeyMapping configureTrinket;
    public static KeyMapping toggleCharmBlessing;
    public static KeyMapping openTrinketPouch;
    public static KeyMapping depositBackpack;
    public static final KeyMapping[] spellSlots = new KeyMapping[SpellcraftItemData.SLOTS];

    public static void registerKeyBinds() {
        isFeatherFixed = registerKeyMapping("is_feather_fixed", -1);
        openWoldsVaultsConfig = registerKeyMapping("open_wolds_vaults_config", GLFW.GLFW_KEY_DELETE);
        toggleBetterCombat = registerKeyMapping("toggle_better_combat", GLFW.GLFW_KEY_PERIOD);
        openInventoryHUD = registerKeyMapping("open_inventory_hud", GLFW.GLFW_KEY_O);
        configureTrinket = registerKeyMapping("configure_trinket", GLFW.GLFW_KEY_G, KeyConflictContext.GUI);
        toggleCharmBlessing = registerKeyMapping("toggle_charm_blessing", GLFW.GLFW_KEY_J, KeyConflictContext.IN_GAME);
        openTrinketPouch = registerKeyMapping("open_trinket_pouch", GLFW.GLFW_KEY_T, KeyConflictContext.IN_GAME, KeyModifier.CONTROL);
        depositBackpack = registerKeyMapping("deposit_backpack", GLFW.GLFW_KEY_D, KeyConflictContext.GUI, KeyModifier.SHIFT);
        for (int i = 0; i < spellSlots.length; i++) {
            spellSlots[i] = new SlotKeyMapping(i, "key." + WoldsVaults.MOD_ID + ".spell_slot_" + (i + 1),
                    "key.category." + WoldsVaults.MOD_ID + ".spell_bar");
            ClientRegistry.registerKeyBinding(spellSlots[i]);
        }
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
