package xyz.iwolfking.woldsvaults.config.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraftforge.data.loading.DatagenModLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Finds and applies the config patches shipped in the jar under {@value #RESOURCE_ROOT}, keyed by the config's path
 * below {@code config/the_vault/}. Both sides read the same jar, so client and server patch their own disk copies
 * identically. A data run uses the in-memory table the patch provider hands over instead, so datagen readers see
 * this run's patches rather than the previous run's output.
 */
public final class ConfigPatches {
    public static final String RESOURCE_ROOT = "data/woldsvaults/config_patches/";
    private static final String CONFIG_ROOT = "config/the_vault/";
    private static final Map<String, Optional<ConfigPatch>> CACHE = new ConcurrentHashMap<>();
    @Nullable
    private static Map<String, ConfigPatch> dataRunTable;

    private ConfigPatches() {
    }

    /** Hands the patch provider's table to this data run; datagen never reads patches from the classpath. */
    public static void useDataRunTable(Map<String, ConfigPatch> table) {
        dataRunTable = Map.copyOf(table);
        CACHE.clear();
    }

    /** The config's path below {@code config/the_vault/}, with forward slashes, or null for a file outside it. */
    @Nullable
    public static String relativePath(File file) {
        return relativePath(file.getPath());
    }

    @Nullable
    public static String relativePath(String path) {
        String normalized = path.replace('\\', '/');
        int at = normalized.indexOf(CONFIG_ROOT);
        return at < 0 ? null : normalized.substring(at + CONFIG_ROOT.length());
    }

    public static Optional<ConfigPatch> find(@Nullable String path) {
        if (path == null) {
            return Optional.empty();
        }
        return CACHE.computeIfAbsent(path, ConfigPatches::load);
    }

    private static Optional<ConfigPatch> load(String path) {
        if (DatagenModLoader.isRunningDataGen()) {
            return dataRunTable == null ? Optional.empty() : Optional.ofNullable(dataRunTable.get(path));
        }
        try (InputStream stream = ConfigPatches.class.getClassLoader().getResourceAsStream(RESOURCE_ROOT + path)) {
            if (stream == null) {
                return Optional.empty();
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return Optional.of(ConfigPatch.fromJson(JsonParser.parseReader(reader).getAsJsonObject()));
            }
        } catch (Exception e) {
            WoldLogger.error("Config patch for {} could not be read; the upstream config loads unpatched", path, e);
            return Optional.empty();
        }
    }

    /** Applies the patch to the parsed disk config, logging an error for each op that no longer resolves. */
    public static JsonElement apply(String path, ConfigPatch patch, JsonElement base) {
        ConfigPatch.Result result = patch.apply(base);
        for (String failure : result.failures()) {
            WoldLogger.error("Config patch for {} did not apply {}; the upstream value stays", path, failure);
        }
        WoldLogger.debug("Applied config patch to {} ({} of {} ops)", path, patch.ops().size() - result.failures().size(), patch.ops().size());
        return result.json();
    }

    /** The patched content of a config upstream does not ship, from the patch's {@code create} plus its ops. */
    public static JsonElement created(String path, ConfigPatch patch) {
        return apply(path, patch, patch.create());
    }

    /**
     * Reads the disk config, applies its patch and hands the patched JSON to the base mod's own deserializer. When
     * the patched tree does not deserialize, the unpatched file is used instead and the failure is logged, so a bad
     * patch never sends the base mod into its write-defaults fallback.
     */
    public static Object deserializePatched(String path, ConfigPatch patch, Reader reader, Function<Reader, Object> deserialize) {
        JsonElement base = JsonParser.parseReader(reader);
        JsonElement patched = apply(path, patch, base);
        try {
            return deserialize.apply(new StringReader(patched.toString()));
        } catch (Exception e) {
            WoldLogger.error("Config patch for {} produced a config the base mod cannot read; loading the unpatched file", path, e);
            return deserialize.apply(new StringReader(base.toString()));
        }
    }
}
