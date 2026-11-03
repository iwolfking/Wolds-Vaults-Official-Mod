package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.config.WoldsTable;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What one god's node effects do, generated as {@code vault_configs/gods/effects/<god>.json}: a handler type, a
 * per-point value table and the handler's parameters per entry. The deserializer never throws.
 */
public class GodNodeEffectsConfig extends Config implements WoldsTable {
    /** One configured effect on disk: its handler type, its per-point table, and the whole entry object. */
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

    @Expose @JsonAdapter(EffectMap.Serializer.class) public EffectMap effects;

    @Override
    public String getName() {
        return "gods" + File.separator + "effects";
    }

    /** Refuses an entry with no handler or a non-numeric {@code values}. */
    @Nullable
    @Override
    public String validate() {
        if (this.effects == null || this.effects.isEmpty()) {
            return "it defines no effects";
        }
        for (Map.Entry<String, Entry> entry : this.effects.entrySet()) {
            Entry effect = entry.getValue();
            if (effect == null || effect.handler() == null || effect.handler().isBlank()) {
                return "effect '" + entry.getKey() + "' names no handler";
            }
            if (effect.values() == null) {
                return "effect '" + entry.getKey() + "' has a 'values' key that is not an array of numbers";
            }
        }
        return null;
    }

    @Override
    protected void reset() {
    }

    public Map<String, Entry> getEffects() {
        return this.effects == null ? Collections.emptyMap() : this.effects;
    }
}
