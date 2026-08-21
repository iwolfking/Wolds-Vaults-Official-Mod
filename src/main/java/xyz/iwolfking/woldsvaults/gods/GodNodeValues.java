package xyz.iwolfking.woldsvaults.gods;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.core.vault.influence.VaultGod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.gods.GodNodeEffectDefaults;
import xyz.iwolfking.woldsvaults.config.gods.GodNodeEffectsConfig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every tuned number a god node reads, resolved from
 * {@code config/the_vault/gods/god_node_effects_<god>.json}.
 *
 * <p>Values are read through methods rather than held in {@code static final} fields because the
 * god modules are class-loaded during mod construction, before the config pass runs: a constant
 * initialised from config would bake whatever was loaded at class-init time and never see a
 * config reload.
 *
 * <p>A lookup that the loaded config cannot answer falls back to
 * {@link GodNodeEffectDefaults} - the same table the config file is generated from - and logs the
 * miss once, naming the effect and the field. That keeps a hand-edited file that drops an entry
 * playing at shipped values instead of silently at zero.
 */
public final class GodNodeValues {
    private record Values(float[] table, Map<String, Double> fields) {
    }

    private static final float[] NO_VALUES = new float[0];
    private static final Set<String> RESERVED = Set.of("handler", "values");
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private static final Map<String, Values> DEFAULTS = readDefaults();

    private static volatile Map<String, Values> values = DEFAULTS;

    private GodNodeValues() {
    }

    /**
     * Rebuilds the value table from the loaded configs. Called from the config pass before the
     * node registry is built, so a registry failure cannot leave the god modules reading nothing.
     */
    public static void load(Map<VaultGod, GodNodeEffectsConfig> configs) {
        Map<String, Values> loaded = new LinkedHashMap<>();
        for (Map.Entry<VaultGod, GodNodeEffectsConfig> config : configs.entrySet()) {
            if (config.getValue() == null) {
                continue;
            }
            for (Map.Entry<String, GodNodeEffectsConfig.Entry> entry : config.getValue().getEffects().entrySet()) {
                loaded.put(entry.getKey(), read(entry.getValue()));
            }
        }
        values = Collections.unmodifiableMap(loaded);
        WARNED.clear();
        WoldsVaults.LOGGER.info("Loaded values for {} god node effects", loaded.size());
    }

    /** The per-point table of an effect, empty when the effect carries only named scalars. */
    public static float[] table(String effectId) {
        Values entry = values.get(effectId);
        if (entry != null) {
            return entry.table();
        }
        return fallback(effectId, "values").table();
    }

    /** The first entry of an effect's per-point table, for the many tables that hold exactly one. */
    public static float value(String effectId) {
        float[] table = table(effectId);
        return table.length == 0 ? 0.0F : table[0];
    }

    /**
     * The shallow and deep bands of a stat that pays a different amount per star by depth, as the
     * pair {@code {shallow, deep}} the banded stat providers expect.
     */
    public static float[] bands(String shallowEffectId, String deepEffectId) {
        return new float[]{value(shallowEffectId), value(deepEffectId)};
    }

    public static float number(String effectId, String field) {
        return (float) precise(effectId, field);
    }

    public static int count(String effectId, String field) {
        return (int) precise(effectId, field);
    }

    /** A named scalar at full double precision, for the few nodes whose maths is done in doubles. */
    public static double precise(String effectId, String field) {
        Values entry = values.get(effectId);
        if (entry != null) {
            Double configured = entry.fields().get(field);
            if (configured != null) {
                return configured;
            }
        }
        Double shipped = fallback(effectId, field).fields().get(field);
        return shipped == null ? 0.0D : shipped;
    }

    private static Values fallback(String effectId, String field) {
        Values shipped = DEFAULTS.get(effectId);
        if (WARNED.add(effectId + "#" + field)) {
            WoldsVaults.LOGGER.error("God node effect '{}' has no configured '{}' in "
                            + "config/the_vault/gods/god_node_effects_*.json; falling back to the shipped {}",
                    effectId, field, shipped == null ? "table, which does not define it either" : "value");
        }
        return shipped == null ? new Values(NO_VALUES, Collections.emptyMap()) : shipped;
    }

    private static Map<String, Values> readDefaults() {
        Map<String, Values> defaults = new LinkedHashMap<>();
        for (VaultGod god : VaultGod.values()) {
            GodNodeEffectsConfig.EffectMap map =
                    GodNodeEffectDefaults.effects(god.getName().toLowerCase(Locale.ROOT));
            map.forEach((id, entry) -> defaults.put(id, read(entry)));
        }
        return Collections.unmodifiableMap(defaults);
    }

    private static Values read(GodNodeEffectsConfig.Entry entry) {
        if (entry == null) {
            return new Values(NO_VALUES, Collections.emptyMap());
        }
        float[] table = entry.values() == null ? NO_VALUES : entry.values();
        Map<String, Double> fields = new LinkedHashMap<>();
        JsonObject json = entry.json();
        if (json != null) {
            for (Map.Entry<String, JsonElement> member : json.entrySet()) {
                if (RESERVED.contains(member.getKey()) || !member.getValue().isJsonPrimitive()
                        || !member.getValue().getAsJsonPrimitive().isNumber()) {
                    continue;
                }
                fields.put(member.getKey(), member.getValue().getAsDouble());
            }
        }
        return new Values(table, Collections.unmodifiableMap(fields));
    }
}
