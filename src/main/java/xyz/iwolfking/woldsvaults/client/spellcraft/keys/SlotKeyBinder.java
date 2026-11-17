package xyz.iwolfking.woldsvaults.client.spellcraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.init.ModKeybinds;

/**
 * Binds spell slot keys from the spell bar the way the Controls screen does: Escape clears the slot, and a modifier
 * key pressed alone keeps the slot waiting for the rest of the combination.
 */
public final class SlotKeyBinder {
    private SlotKeyBinder() {
    }

    /** Binds a pressed keyboard key; returns true once the binding is complete. */
    public static boolean bindKey(int slot, int keyCode, int scanCode) {
        return bind(slot, keyCode == GLFW.GLFW_KEY_ESCAPE ? InputConstants.UNKNOWN : InputConstants.getKey(keyCode, scanCode));
    }

    /** Binds a mouse button; returns true once the binding is complete. */
    public static boolean bindMouse(int slot, int button) {
        return bind(slot, InputConstants.Type.MOUSE.getOrCreate(button));
    }

    public static void clear(int slot) {
        bind(slot, InputConstants.UNKNOWN);
    }

    private static boolean bind(int slot, InputConstants.Key key) {
        KeyMapping mapping = slot >= 0 && slot < ModKeybinds.spellSlots.length ? ModKeybinds.spellSlots[slot] : null;
        if (mapping == null) {
            WoldLogger.error("Spell slot {} has no key mapping; the bind was skipped", slot + 1);
            return true;
        }
        mapping.setKeyModifierAndCode(KeyModifier.getActiveModifier(), key);
        Minecraft.getInstance().options.setKey(mapping, key);
        KeyMapping.resetMapping();
        return !KeyModifier.isKeyCodeModifier(mapping.getKey());
    }
}
