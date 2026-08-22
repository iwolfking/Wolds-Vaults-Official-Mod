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
 * Emits the god constellation topology and layout as pack config, the pair
 * {@code config/the_vault/gods/god_tree_<god>.json} and
 * {@code config/the_vault/gods/god_tree_<god>_gui_styles.json} that {@code ModConfigs} reads. The
 * Idona layout is the a30 draft exported from
 * {@code redesign/greed-rework/tree-drafts/idona_tree_v4.py} in the pack repo, and the Wendarr
 * hourglass, Velara wreath and Tenos hoard-and-chest are the drafts from {@code wendarr_tree.py},
 * {@code velara_tree.py} and {@code tenos_tree.py} next to it, exported by the matching
 * {@code export_<god>_wiring.py}; coordinates are screen pixels with +y down, and edges are the
 * undirected lattice connections. Stat helper methods keep each node's display name, effect id and
 * icon in exactly one place, and a stat the sheet lists as a pair is two helpers, the shallow band
 * and the {@code _ii} deep one. Ultra Rampaging and the two shelved extraction nodes are declared
 * through the disabled builder methods until their mechanics land.
 *
 * <p>What a node's effect DOES is not written here: {@code god_node_effects_<god>.json} is
 * hand-authored balance data, and overwriting it from the layout source would destroy it.
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

    /**
     * The pack repo's {@code config/the_vault/gods} folder, which is where these files ship from.
     * The pack checkout is machine-local, so the build passes its location in; an unset property
     * is fatal rather than quietly writing the trees somewhere the game will never read them.
     */
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
