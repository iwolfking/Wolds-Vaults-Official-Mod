package xyz.iwolfking.woldsvaults.gods.node;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Set;

/**
 * Decodes and encodes {@link GodEffectParams} records against an effect's config object, through
 * their canonical constructor and the base mod's configured Gson.
 */
public final class GodEffectParamsCodec {
    private static final Set<String> RESERVED = Set.of("handler", "values");

    private GodEffectParamsCodec() {
    }

    /**
     * Builds {@code type} from every config key but {@code handler} and {@code values}; absent is fatal unless
     * {@link Nullable}.
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

    /** Whether a component may be absent; the backing field is checked as well as the component. */
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
