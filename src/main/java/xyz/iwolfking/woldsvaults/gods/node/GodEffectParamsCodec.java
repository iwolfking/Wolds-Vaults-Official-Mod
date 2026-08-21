package xyz.iwolfking.woldsvaults.gods.node;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Set;

/**
 * Decodes and encodes {@link GodEffectParams} records against an effect's config object.
 *
 * <p>Records are read through their canonical constructor rather than through Gson's reflective
 * field binding: that is the only shape that works on every Gson version the pack has shipped,
 * it keeps records immutable, and it is what lets a missing or malformed field be reported by
 * name together with the effect id at load time instead of surfacing as a zero inside a vault.
 * Component values themselves still go through the base mod's configured Gson, so resource
 * locations, enums and item stacks decode exactly as they do everywhere else in config.
 */
public final class GodEffectParamsCodec {
    private static final Set<String> RESERVED = Set.of("handler", "values");

    private GodEffectParamsCodec() {
    }

    /**
     * Builds the params record of {@code type} from the effect's config object, using every key
     * other than {@code handler} and {@code values}. Absent components are fatal unless the
     * component is annotated {@link Nullable}.
     */
    public static GodEffectParams decode(String effectId, String handler,
                                         Class<? extends GodEffectParams> type, JsonObject json) {
        if (!type.isRecord()) {
            throw GodTreeConfigException.fail("Params type " + type.getName() + " of handler '" + handler
                    + "' must be a record");
        }
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] arguments = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            parameterTypes[i] = component.getType();
            arguments[i] = read(effectId, handler, type, component, json);
        }
        GodEffectParams params;
        try {
            Constructor<? extends GodEffectParams> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            params = constructor.newInstance(arguments);
        } catch (ReflectiveOperationException e) {
            throw GodTreeConfigException.fail("Could not build params " + type.getSimpleName() + " for god effect '"
                    + effectId + "'", e);
        }
        params.validate(effectId);
        return params;
    }

    /** Writes a params record back into an effect object, alongside {@code handler} and {@code values}. */
    public static void encode(GodEffectParams params, JsonObject target) {
        if (params == null) {
            return;
        }
        for (RecordComponent component : params.getClass().getRecordComponents()) {
            try {
                Object value = component.getAccessor().invoke(params);
                if (value != null) {
                    target.add(component.getName(), Config.GSON.toJsonTree(value, component.getGenericType()));
                }
            } catch (ReflectiveOperationException e) {
                throw GodTreeConfigException.fail("Could not read params field '" + component.getName() + "' of "
                        + params.getClass().getSimpleName(), e);
            }
        }
    }

    @Nullable
    private static Object read(String effectId, String handler, Class<? extends GodEffectParams> type,
                               RecordComponent component, JsonObject json) {
        String name = component.getName();
        if (RESERVED.contains(name)) {
            throw GodTreeConfigException.fail("Params field '" + name + "' of handler '" + handler
                    + "' collides with a reserved god effect field");
        }
        JsonElement element = json.get(name);
        if (element == null || element.isJsonNull()) {
            if (isOptional(type, component)) {
                return null;
            }
            throw GodTreeConfigException.fail("God effect '" + effectId + "' (handler '" + handler
                    + "') is missing required field '" + name + "'");
        }
        try {
            return Config.GSON.fromJson(element, component.getGenericType());
        } catch (RuntimeException e) {
            throw GodTreeConfigException.fail("God effect '" + effectId + "' (handler '" + handler + "') has an "
                    + "invalid value for field '" + name + "'", e);
        }
    }

    /**
     * Whether a component may be absent. {@code @Nullable} on a record component is only visible
     * on the component itself when the annotation type targets record components, so the backing
     * field is checked as well - that is where the JSR-305 annotation the codebase uses lands.
     */
    private static boolean isOptional(Class<? extends GodEffectParams> type, RecordComponent component) {
        if (component.isAnnotationPresent(Nullable.class)) {
            return true;
        }
        try {
            return type.getDeclaredField(component.getName()).isAnnotationPresent(Nullable.class);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
