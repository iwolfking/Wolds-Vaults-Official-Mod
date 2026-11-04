package xyz.iwolfking.woldsvaults.datagen.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatch;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatches;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes field-level patches for base Vault Hunters configs to {@value ConfigPatches#RESOURCE_ROOT}. A patch holds
 * only the values Wold's Vaults changes; everything else in the file stays upstream's. Before writing, each patch is
 * read back from the JSON it serializes to and applied to the seeded base config, and any op that does not resolve
 * fails the run, so a patch that no longer matches upstream never ships.
 */
public abstract class AbstractConfigPatchProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    private static final String SEEDED_CONFIG_ROOT = "config/the_vault/";
    private final DataGenerator generator;
    private final Map<String, Patch> patches = new LinkedHashMap<>();
    private Map<String, ConfigPatch> built;

    protected AbstractConfigPatchProvider(DataGenerator generator) {
        this.generator = generator;
    }

    protected abstract void registerPatches();

    /** Every patch this provider writes, keyed by the config's path below {@code config/the_vault/}. */
    public final Map<String, ConfigPatch> patches() {
        if (this.built == null) {
            this.registerPatches();
            Map<String, ConfigPatch> out = new LinkedHashMap<>();
            this.patches.forEach((path, patch) -> out.put(path, patch.build()));
            this.built = Collections.unmodifiableMap(out);
        }
        return this.built;
    }

    /** The patch for one config file, such as {@code talents.json} or {@code gear_modifiers/sword.json}. */
    protected final Patch patch(String path) {
        return this.patches.computeIfAbsent(path, p -> new Patch());
    }

    @Override
    public void run(HashCache cache) throws IOException {
        for (Map.Entry<String, ConfigPatch> entry : this.patches().entrySet()) {
            String path = entry.getKey();
            JsonObject written = JsonParser.parseString(GSON.toJson(entry.getValue().toJson())).getAsJsonObject();
            ConfigPatch patch = ConfigPatch.fromJson(written);
            String unhooked = unhookedReason(path, patch);
            if (unhooked != null) {
                throw new IllegalStateException("Config patch for " + path + " would never apply: " + unhooked);
            }
            File seeded = new File(SEEDED_CONFIG_ROOT + path);
            JsonElement base;
            if (patch.create() != null) {
                if (seeded.exists()) {
                    throw new IllegalStateException("Config patch for " + path + " creates a file the seeded pack already ships");
                }
                base = patch.create();
            } else {
                if (!seeded.exists()) {
                    throw new IllegalStateException("Config patch for " + path + " targets a file the seeded pack does not ship");
                }
                try (Reader reader = new FileReader(seeded)) {
                    base = JsonParser.parseReader(reader);
                }
            }
            List<String> failures = patch.apply(base).failures();
            if (!failures.isEmpty()) {
                throw new IllegalStateException("Config patch for " + path + " does not apply to the seeded pack:\n  " + String.join("\n  ", failures));
            }
            DataProvider.save(GSON, cache, written, this.generator.getOutputFolder().resolve(ConfigPatches.RESOURCE_ROOT + path));
        }
    }

    /**
     * Why the runtime hooks would never apply this patch, or null. Ops apply to files the base mod reads through
     * {@code Config.readConfig}, generation loot tables and void crucible item files; {@code create} works only for
     * {@code Config}-read files. Locale variants and the other generation folders are not hooked.
     */
    private static String unhookedReason(String path, ConfigPatch patch) {
        if (path.startsWith("lang/")) {
            return "locale variants are read without the patch hook";
        }
        boolean lootTable = path.startsWith("gen/1.0/loot_tables/");
        boolean crucibleItem = path.startsWith("void_crucible/");
        if (path.startsWith("gen/") && !lootTable) {
            return "only generation loot tables are hooked under gen/";
        }
        if (patch.create() != null && (lootTable || crucibleItem)) {
            return "create works only for files read through Config.readConfig";
        }
        return null;
    }

    /** A selector for the single array element whose {@code id} is {@code id}. */
    public static JsonObject id(String id) {
        return where("id", id);
    }

    /** A selector for the single array element whose {@code field} equals {@code value}. */
    public static JsonObject where(String field, Object value) {
        JsonObject selector = new JsonObject();
        selector.add(field, json(value));
        return selector;
    }

    /** A selector for the single array element equal to {@code value}. */
    public static JsonObject value(Object value) {
        return where(ConfigPatch.VALUE_SELECTOR, value);
    }

    public static JsonElement json(Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof JsonElement element) {
            return element;
        }
        if (value instanceof Number number) {
            return new JsonPrimitive(number);
        }
        if (value instanceof Boolean bool) {
            return new JsonPrimitive(bool);
        }
        if (value instanceof String string) {
            return new JsonPrimitive(string);
        }
        throw new IllegalArgumentException("Not a JSON value: " + value);
    }

    /** Parses a JSON literal, for values whose shape is easier to read as JSON than as builder calls. */
    public static JsonElement parse(String text) {
        return JsonParser.parseString(text);
    }

    public static final class Patch {
        private JsonElement create;
        private final List<ConfigPatch.Op> ops = new ArrayList<>();

        /** Supplies the whole file for a config upstream does not ship. */
        public Patch create(JsonElement content) {
            this.create = content;
            return this;
        }

        /** The node at {@code path}; each step is an object key, an array index or a selector. */
        public Node at(Object... path) {
            return new Node(this, steps(new JsonArray(), path));
        }

        private void op(String type, JsonArray path, JsonElement value) {
            this.ops.add(new ConfigPatch.Op(type, path, value));
        }

        private ConfigPatch build() {
            return new ConfigPatch(this.create, this.ops);
        }
    }

    public static final class Node {
        private final Patch patch;
        private final JsonArray path;

        private Node(Patch patch, JsonArray path) {
            this.patch = patch;
            this.path = path;
        }

        public Node at(Object... more) {
            return new Node(this.patch, steps(this.path.deepCopy(), more));
        }

        /** Replaces an existing value: an object key or an array element. */
        public Node set(Object step, Object value) {
            this.patch.op(ConfigPatch.SET, steps(this.path.deepCopy(), step), json(value));
            return this;
        }

        /** Adds an object key upstream does not have. */
        public Node add(String key, Object value) {
            this.patch.op(ConfigPatch.ADD, steps(this.path.deepCopy(), key), json(value));
            return this;
        }

        /** Removes an existing object key or array element. */
        public Node remove(Object step) {
            this.patch.op(ConfigPatch.REMOVE, steps(this.path.deepCopy(), step), null);
            return this;
        }

        /** Appends to the array at this node. */
        public Node append(Object value) {
            this.patch.op(ConfigPatch.APPEND, this.path.deepCopy(), json(value));
            return this;
        }
    }

    private static JsonArray steps(JsonArray path, Object... steps) {
        for (Object step : steps) {
            if (step instanceof Integer || step instanceof String || step instanceof JsonObject) {
                path.add(json(step));
            } else {
                throw new IllegalArgumentException("A patch path step is a key, an index or a selector, got " + step);
            }
        }
        return path;
    }
}
