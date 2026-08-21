package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What one god's node effects DO - {@code config/the_vault/gods/god_node_effects_<god>.json}.
 * Each entry binds a registered handler type, the per-point value table that replaces the old
 * per-god Java constant blocks, and the handler's own parameters.
 *
 * <p>The entry map is read through its own Gson serializer rather than field binding, because a
 * handler's parameters are arbitrary extra fields whose shape only the handler type knows, and
 * because the map key is the effect id every failure message has to name.
 *
 * <p>Deliberately total: this deserializer never rejects an entry. {@code Config#readConfig}
 * treats any exception raised while reading as "file missing" and overwrites the file with
 * defaults, so throwing here would silently destroy a mistyped tree. Every assertion lives in
 * {@code GodNodeValidator}, which runs after the read and is allowed to be fatal.
 */
public class GodNodeEffectsConfig extends Config {
    private static final Gson GSON = Config.GSON.newBuilder()
            .registerTypeAdapter(EffectMap.class, EffectMap.Serializer.INSTANCE)
            .create();

    /**
     * One configured effect as it appears on disk: the handler type it binds, its per-point
     * table, and the whole entry object, from which the handler's typed params record is decoded
     * once the handler registry can say which record that is.
     */
    public record Entry(@Nullable String handler, @Nullable float[] values, JsonObject json) {
    }

    /** The {@code effects} object, keyed by effect id. */
    public static class EffectMap extends LinkedHashMap<String, Entry> {
        public static class Serializer implements JsonDeserializer<EffectMap>, JsonSerializer<EffectMap> {
            public static final Serializer INSTANCE = new Serializer();

            @Override
            public EffectMap deserialize(JsonElement element, Type type, JsonDeserializationContext context)
                    throws JsonParseException {
                EffectMap map = new EffectMap();
                if (!element.isJsonObject()) {
                    return map;
                }
                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                    map.put(entry.getKey(), read(entry.getValue()));
                }
                return map;
            }

            @Override
            public JsonElement serialize(EffectMap map, Type type, JsonSerializationContext context) {
                JsonObject root = new JsonObject();
                map.forEach((id, entry) -> root.add(id, entry.json()));
                return root;
            }

            private static Entry read(JsonElement element) {
                if (!element.isJsonObject()) {
                    return new Entry(null, null, new JsonObject());
                }
                JsonObject json = element.getAsJsonObject();
                String handler = json.has("handler") && json.get("handler").isJsonPrimitive()
                        ? json.get("handler").getAsString()
                        : null;
                float[] values = null;
                if (json.has("values")) {
                    try {
                        values = Config.GSON.fromJson(json.get("values"), float[].class);
                    } catch (RuntimeException e) {
                        values = null;
                    }
                }
                return new Entry(handler, values, json);
            }
        }
    }

    private String god;

    @Expose private EffectMap effects;

    public GodNodeEffectsConfig(String god) {
        this.god = god;
    }

    @Override
    public Gson getGson() {
        return GSON;
    }

    @Override
    public String getName() {
        return "gods" + File.separator + "god_node_effects_" + this.god;
    }

    /**
     * Restores the god this config was constructed for. Gson allocates the loaded instance
     * without running the constructor, so the name the file was read under only survives by
     * being copied off the instance that read it.
     */
    @Override
    protected void onLoad(@Nullable Config oldConfigInstance) {
        if (oldConfigInstance instanceof GodNodeEffectsConfig previous) {
            this.god = previous.god;
        }
    }

    /**
     * Restores the shipped effect table of this config's god. {@code Config#readConfig} treats a
     * missing or unreadable file as a request to regenerate from defaults, so this is what a
     * fresh install runs on: it has to reproduce the values the trees ship with, not an empty
     * table, or the whole tree would come up at zero.
     */
    @Override
    protected void reset() {
        this.effects = GodNodeEffectDefaults.effects(this.god);
    }

    public Map<String, Entry> getEffects() {
        return this.effects == null ? Collections.emptyMap() : this.effects;
    }
}
