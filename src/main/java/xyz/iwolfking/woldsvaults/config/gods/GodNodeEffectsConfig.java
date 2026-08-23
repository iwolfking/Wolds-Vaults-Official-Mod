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
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.PackAuthoredConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What one god's node effects do - {@code config/the_vault/gods/god_node_effects_<god>.json}: a handler
 * type, a per-point value table and the handler's parameters per entry. The deserializer never throws.
 */
public class GodNodeEffectsConfig extends PackAuthoredConfig {
    private static final Gson GSON = Config.GSON.newBuilder()
            .registerTypeAdapter(EffectMap.class, EffectMap.Serializer.INSTANCE)
            .create();

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

    /** Restores the god name, which Gson does not carry across into the loaded instance. */
    @Override
    protected void onLoad(@Nullable Config oldConfigInstance) {
        if (oldConfigInstance instanceof GodNodeEffectsConfig previous) {
            this.god = previous.god;
        }
    }

    /** Refuses an entry with no handler or a non-numeric {@code values}, falling back to the shipped table. */
    @Override
    protected boolean isValid() {
        if (this.effects == null || this.effects.isEmpty()) {
            return this.invalid("it defines no effects");
        }
        for (Map.Entry<String, Entry> entry : this.effects.entrySet()) {
            Entry effect = entry.getValue();
            if (effect == null || effect.handler() == null || effect.handler().isBlank()) {
                return this.invalid("effect '" + entry.getKey() + "' names no handler");
            }
            if (effect.values() == null) {
                return this.invalid("effect '" + entry.getKey() + "' has a 'values' key that is not an array of "
                        + "numbers");
            }
        }
        return true;
    }

    private boolean invalid(String reason) {
        WoldsVaults.LOGGER.error("God node effect config {} is unusable: {}. Falling back to the shipped effect "
                + "table.", this.getName(), reason);
        return false;
    }

    @Override
    protected void reset() {
        this.effects = GodNodeEffectDefaults.effects(this.god);
    }

    public Map<String, Entry> getEffects() {
        return this.effects == null ? Collections.emptyMap() : this.effects;
    }
}
