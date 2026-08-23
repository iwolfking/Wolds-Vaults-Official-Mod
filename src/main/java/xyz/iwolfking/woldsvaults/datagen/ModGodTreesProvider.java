package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeBuilder;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeDefaults;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Emits the god constellation topology and layout as {@code god_tree_<god>.json} and
 * {@code god_tree_<god>_gui_styles.json}; coordinates are screen pixels with +y down. What a node's
 * effect does is not written here - {@code god_node_effects_<god>.json} is hand-authored.
 */
public class ModGodTreesProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String PACK_DIR_PROPERTY = "woldsvaults.packDir";

    @Override
    public void run(HashCache cache) throws IOException {
        Path gods = packGodsDirectory();
        Files.createDirectories(gods);
        save(gods, "idona", idona());
        save(gods, "wendarr", wendarr());
        save(gods, "velara", velara());
        save(gods, "tenos", tenos());
    }

    private static void save(Path gods, String god, GodTreeBuilder tree) throws IOException {
        write(gods.resolve("god_tree_" + god + ".json"), tree.buildTree());
        write(gods.resolve("god_tree_" + god + "_gui_styles.json"), tree.buildStyles());
    }

    private static void write(Path path, JsonObject json) throws IOException {
        Files.writeString(path, GSON.toJson(json) + "\n", StandardCharsets.UTF_8);
    }

    /** The pack's {@code config/the_vault/gods} folder from {@code woldsvaults.packDir}; throws if unset. */
    private static Path packGodsDirectory() {
        String packDir = System.getProperty(PACK_DIR_PROPERTY);
        if (packDir == null || packDir.isBlank()) {
            throw new IllegalStateException("God tree datagen needs -D" + PACK_DIR_PROPERTY
                    + "=<pack repo root>, which the build sets from the wvPackDir Gradle property or"
                    + " the WV_PACK_DIR environment variable");
        }
        return Path.of(packDir, "config", "the_vault", "gods");
    }

    @Override
    public String getName() {
        return "Wolds Vaults God Trees";
    }

    private static GodTreeBuilder idona() {
        return GodTreeDefaults.idona();
    }

    private static GodTreeBuilder wendarr() {
        return GodTreeDefaults.wendarr();
    }

    private static GodTreeBuilder velara() {
        return GodTreeDefaults.velara();
    }

    private static GodTreeBuilder tenos() {
        return GodTreeDefaults.tenos();
    }
}
