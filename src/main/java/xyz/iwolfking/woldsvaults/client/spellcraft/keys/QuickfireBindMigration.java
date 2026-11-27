package xyz.iwolfking.woldsvaults.client.spellcraft.keys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import iskallia.vault.init.ModKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.loading.FMLPaths;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Gives back the Vault Hunters quick-fire binds an earlier version unbound, once, on the first spell bar sync. A player
 * whose binds were saved but never moved gets the saved binds back; a player whose binds were moved onto spell slot
 * keys gets each bound slot key's bind copied onto the quick-fire key of the ability on that default-bar slot, when
 * that quick-fire key is unbound. The slot keys stay bound. The client file then records that the restore is done.
 * Players without the file keep their quick-fire keys as they are.
 */
final class QuickfireBindMigration {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String APPLIED = "applied";
    private static final String RESTORED = "restored";
    private static final String BINDS = "binds";
    private static final String KEY = "key";
    private static final String MODIFIER = "modifier";
    private static boolean checked;

    private QuickfireBindMigration() {
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("woldsvaults").resolve("quickfire_binds.json");
    }

    /** Restores the binds once the spell bar has synced, then saves the options and marks the file done. */
    static void restoreIfReady(Minecraft minecraft) {
        if (checked || !ClientSpellBar.isSynced() || minecraft.options == null) {
            return;
        }
        checked = true;
        Path file = file();
        if (!Files.isRegularFile(file)) {
            return;
        }
        JsonObject root = read(file);
        if (root == null || flag(root, RESTORED)) {
            return;
        }
        int restored = flag(root, APPLIED) ? fromSlotKeys() : fromSavedBinds(root);
        KeyMapping.resetMapping();
        minecraft.options.save();
        root.addProperty(RESTORED, true);
        write(file, root);
        WoldLogger.debug("Quick-fire binds: restored {} bind(s)", restored);
    }

    private static int fromSavedBinds(JsonObject root) {
        int restored = 0;
        JsonObject saved = root.has(BINDS) ? root.getAsJsonObject(BINDS) : new JsonObject();
        for (Map.Entry<String, JsonElement> entry : saved.entrySet()) {
            try {
                JsonObject bind = entry.getValue().getAsJsonObject();
                InputConstants.Key key = InputConstants.getKey(bind.get(KEY).getAsString());
                KeyModifier modifier = KeyModifier.valueFromString(bind.get(MODIFIER).getAsString());
                if (copy(entry.getKey(), modifier, key, "the saved bind")) {
                    restored++;
                }
            } catch (RuntimeException e) {
                WoldLogger.error("Quick-fire binds: the saved bind of {} could not be read ({}); it was not restored", entry.getKey(), e.toString());
            }
        }
        return restored;
    }

    private static int fromSlotKeys() {
        int restored = 0;
        KeyMapping[] slots = xyz.iwolfking.woldsvaults.client.init.ModKeybinds.spellSlots;
        for (int i = 0; i < slots.length; i++) {
            KeyMapping slot = slots[i];
            if (slot == null || slot.isUnbound()) {
                continue;
            }
            String group = ClientSpellBar.defaultSlot(i);
            if (group.isEmpty()) {
                WoldLogger.error("Quick-fire binds: spell slot {} is bound to {} but holds no ability on the default bar; nothing was copied",
                        i + 1, slot.getKey().getName());
                continue;
            }
            if (copy(group, slot.getKeyModifier(), slot.getKey(), "spell slot " + (i + 1))) {
                restored++;
            }
        }
        return restored;
    }

    private static boolean copy(String group, KeyModifier modifier, InputConstants.Key key, String from) {
        KeyMapping quickfire = ModKeybinds.abilityQuickfireKey.get(group);
        if (quickfire == null) {
            WoldLogger.error("Quick-fire binds: {} has no quick-fire key, so {} ({}) was not copied", group, from, key.getName());
            return false;
        }
        if (!quickfire.isUnbound()) {
            WoldLogger.error("Quick-fire binds: the quick-fire key of {} is already bound to {}, so {} ({}) was not copied",
                    group, quickfire.getKey().getName(), from, key.getName());
            return false;
        }
        quickfire.setKeyModifierAndCode(modifier, key);
        return true;
    }

    private static boolean flag(JsonObject root, String name) {
        return root.has(name) && root.get(name).getAsBoolean();
    }

    @Nullable
    private static JsonObject read(Path file) {
        try {
            return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            WoldLogger.error("Quick-fire binds: could not read {} ({}); no binds were restored", file, e.toString());
            return null;
        }
    }

    private static void write(Path file, JsonObject root) {
        try {
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            WoldLogger.error("Quick-fire binds: could not write {} ({}); the restore may repeat on the next launch", file, e.toString());
        }
    }
}
