package xyz.iwolfking.woldsvaults.gods.tree;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import iskallia.vault.core.vault.influence.VaultGod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches the datagen-authored god tree definitions from the mod jar. The definitions
 * ship inside the addon at {@code data/woldsvaults/god_trees/<god>.json}, so both logical sides
 * read identical trees with no sync step; only per-player purchase state travels the network.
 * A god with no shipped tree resolves to empty - the screen shows an uncharted sky for it.
 */
public final class GodTrees {
    private static final Map<VaultGod, Optional<GodTreeDefinition>> CACHE = new ConcurrentHashMap<>();

    private GodTrees() {
    }

    public static Optional<GodTreeDefinition> get(VaultGod god) {
        return CACHE.computeIfAbsent(god, GodTrees::load);
    }

    private static Optional<GodTreeDefinition> load(VaultGod god) {
        String path = "/data/woldsvaults/god_trees/" + god.getName().toLowerCase(java.util.Locale.ROOT) + ".json";
        try (InputStream stream = GodTrees.class.getResourceAsStream(path)) {
            if (stream == null) {
                return Optional.empty();
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            GodTreeDefinition definition = GodTreeDefinition.parse(json);
            WoldsVaults.LOGGER.info("Loaded god tree for {} ({} nodes, {} edges)",
                    god.getName(), definition.getNodes().size(), definition.getEdges().size());
            return Optional.of(definition);
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Failed to load god tree definition {}; the {} tree will be unavailable",
                    path, god.getName(), e);
            return Optional.empty();
        }
    }
}
