package xyz.iwolfking.woldsvaults.client.spellcraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import iskallia.vault.init.ModKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import xyz.iwolfking.woldsvaults.mixins.accessors.OptionsKeyMappingsAccessor;

import java.util.ArrayList;
import java.util.List;

final class QuickfireKeys {
    private static boolean retired;

    private QuickfireKeys() {
    }

    static boolean retired() {
        return retired;
    }

    /**
     * Saves the quick-fire binds for the one-time move onto spell slots, then unbinds Vault Hunters' quick-fire keys and
     * removes them from the Controls list, since abilities are cast from spell slots.
     */
    static void hide(Minecraft minecraft) {
        retired = true;
        QuickfireBindMigration.capture(ModKeybinds.abilityQuickfireKey);
        List<KeyMapping> kept = new ArrayList<>();
        int hidden = 0;
        for (KeyMapping mapping : minecraft.options.keyMappings) {
            if (ModKeybinds.QUICKFIRE_CATEGORY.equals(mapping.getCategory())) {
                mapping.setKey(InputConstants.UNKNOWN);
                hidden++;
            } else {
                kept.add(mapping);
            }
        }
        if (hidden > 0) {
            KeyMapping.resetMapping();
            ((OptionsKeyMappingsAccessor) minecraft.options)
                    .woldsvaults$setKeyMappings(kept.toArray(new KeyMapping[0]));
        }
    }
}
