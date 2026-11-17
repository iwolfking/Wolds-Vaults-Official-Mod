package xyz.iwolfking.woldsvaults.client.spellcraft.keys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.loading.FMLPaths;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.init.ModKeybinds;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Moves each player's Vault Hunters quick-fire binds onto the spell slot keys once. The binds are saved to a client file
 * before the quick-fire keys are retired, then copied onto the slot keys of the slots holding the same abilities when
 * the first spell bar arrives. The file then records that the move is done, so it never runs again.
 */
final class QuickfireBindMigration {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String APPLIED = "applied";
    private static final String BINDS = "binds";
    private static final String KEY = "key";
    private static final String MODIFIER = "modifier";

    @Nullable
    private static Map<String, Bind> pending;

    private record Bind(KeyModifier modifier, InputConstants.Key key) {
        boolean sameAs(KeyMapping mapping) {
            return mapping.getKeyModifier() == this.modifier && mapping.getKey().equals(this.key);
        }
    }

    private QuickfireBindMigration() {
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("woldsvaults").resolve("quickfire_binds.json");
    }

    /**
     * Reads the quick-fire binds still waiting to move, or saves the current ones if no file exists yet. Called before
     * the quick-fire keys are unbound.
     */
    static void capture(Map<String, KeyMapping> quickfireKeys) {
        Path file = file();
        if (Files.isRegularFile(file)) {
            pending = read(file);
            return;
        }
        Map<String, Bind> binds = new LinkedHashMap<>();
        quickfireKeys.forEach((group, mapping) -> {
            if (!mapping.isUnbound()) {
                binds.put(group, new Bind(mapping.getKeyModifier(), mapping.getKey()));
            }
        });
        write(file, binds, binds.isEmpty());
        pending = binds.isEmpty() ? null : binds;
        WoldLogger.debug("Quick-fire binds: saved {} bind(s) to move onto spell slots", binds.size());
    }

    /** Copies the waiting binds onto the slot keys once the spell bar has synced, then saves the options. */
    static void applyIfReady(Minecraft minecraft) {
        if (pending == null || !ClientSpellBar.isSynced() || minecraft.options == null) {
            return;
        }
        Map<String, Bind> binds = pending;
        pending = null;
        int moved = 0;
        for (int i = 0; i < ModKeybinds.spellSlots.length; i++) {
            String group = ClientSpellBar.defaultSlot(i);
            Bind bind = group.isEmpty() ? null : binds.remove(group);
            if (bind == null) {
                continue;
            }
            KeyMapping slot = ModKeybinds.spellSlots[i];
            if (!slot.isUnbound()) {
                WoldLogger.error("Quick-fire binds: {} was bound to {}, but spell slot {} already has {}; the old bind was dropped",
                        group, bind.key().getName(), i + 1, slot.getKey().getName());
                continue;
            }
            if (boundToAnotherSlot(bind)) {
                WoldLogger.error("Quick-fire binds: {} was bound to {}, which another spell slot already uses; the old bind was dropped",
                        group, bind.key().getName());
                continue;
            }
            slot.setKeyModifierAndCode(bind.modifier(), bind.key());
            moved++;
        }
        binds.forEach((group, bind) -> WoldLogger.error(
                "Quick-fire binds: {} was bound to {}, but it is on no default spell slot; the old bind was dropped",
                group, bind.key().getName()));
        KeyMapping.resetMapping();
        minecraft.options.save();
        write(file(), Map.of(), true);
        WoldLogger.debug("Quick-fire binds: moved {} bind(s) onto spell slots", moved);
    }

    private static boolean boundToAnotherSlot(Bind bind) {
        for (KeyMapping slot : ModKeybinds.spellSlots) {
            if (bind.sameAs(slot)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Map<String, Bind> read(Path file) {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has(APPLIED) && root.get(APPLIED).getAsBoolean()) {
                return null;
            }
            Map<String, Bind> binds = new LinkedHashMap<>();
            JsonObject saved = root.has(BINDS) ? root.getAsJsonObject(BINDS) : new JsonObject();
            for (Map.Entry<String, JsonElement> entry : saved.entrySet()) {
                JsonObject bind = entry.getValue().getAsJsonObject();
                InputConstants.Key key = InputConstants.getKey(bind.get(KEY).getAsString());
                KeyModifier modifier = KeyModifier.valueFromString(bind.get(MODIFIER).getAsString());
                binds.put(entry.getKey(), new Bind(modifier, key));
            }
            return binds.isEmpty() ? null : binds;
        } catch (IOException | RuntimeException e) {
            WoldLogger.error("Quick-fire binds: could not read {} ({}); no binds will be moved onto spell slots", file, e.toString());
            return null;
        }
    }

    private static void write(Path file, Map<String, Bind> binds, boolean applied) {
        JsonObject root = new JsonObject();
        root.addProperty(APPLIED, applied);
        JsonObject saved = new JsonObject();
        binds.forEach((group, bind) -> {
            JsonObject entry = new JsonObject();
            entry.addProperty(KEY, bind.key().getName());
            entry.addProperty(MODIFIER, bind.modifier().name());
            saved.add(group, entry);
        });
        root.add(BINDS, saved);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            WoldLogger.error("Quick-fire binds: could not write {} ({}); the move may repeat or be lost on the next launch", file, e.toString());
        }
    }
}
