package xyz.iwolfking.woldsvaults.gods.node;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The one registry of god node handler types. A type string is the binding key config uses, the
 * thing load-time validation checks, and the only way a node effect reaches the game - there is
 * no second mechanism.
 *
 * <p>Registration must happen before the god tree configs are read, because validation resolves
 * every configured handler against this registry. {@link GodNodeHandlerTypes} is the single
 * place that registers the built-in types and is bootstrapped from the config load itself.
 */
public final class GodNodeHandlers {
    private record Type(String name, @Nullable Class<? extends GodEffectParams> paramsType, Factory factory) {
    }

    @FunctionalInterface
    public interface Factory {
        GodNodeHandler create(GodEffect effect);
    }

    private static final Map<String, Type> TYPES = new LinkedHashMap<>();

    private GodNodeHandlers() {
    }

    /** Registers a handler type that takes no typed parameters beyond its per-point table. */
    public static synchronized void register(String type, Factory factory) {
        register(type, null, factory);
    }

    /**
     * Registers a handler type and the record its effects' extra config fields decode into.
     * Registering the same type twice is a programming error and is fatal, so a merge that
     * duplicates a registration cannot silently pick a winner.
     */
    public static synchronized void register(String type, @Nullable Class<? extends GodEffectParams> paramsType,
                                             Factory factory) {
        if (type == null || type.isBlank()) {
            throw GodTreeConfigException.fail("Refusing to register a god node handler with a blank type string");
        }
        if (TYPES.containsKey(type)) {
            throw GodTreeConfigException.fail("God node handler type '" + type + "' is already registered");
        }
        TYPES.put(type, new Type(type, paramsType, factory));
    }

    public static synchronized boolean isRegistered(String type) {
        return TYPES.containsKey(type);
    }

    @Nullable
    public static synchronized Class<? extends GodEffectParams> paramsType(String type) {
        Type registered = TYPES.get(type);
        return registered == null ? null : registered.paramsType();
    }

    public static synchronized Set<String> types() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(TYPES.keySet()));
    }

    /**
     * Builds the handler instance for one effect. The effect's handler type must be registered;
     * validation has already asserted that, so reaching the failure here means the registry was
     * mutated after load.
     */
    public static GodNodeHandler create(GodEffect effect) {
        Type registered;
        synchronized (GodNodeHandlers.class) {
            registered = TYPES.get(effect.handler());
        }
        if (registered == null) {
            throw GodTreeConfigException.fail("God effect '" + effect.id() + "' uses unregistered handler type '"
                    + effect.handler() + "'");
        }
        GodNodeHandler handler = registered.factory().create(effect);
        if (handler == null) {
            throw GodTreeConfigException.fail("Handler factory for type '" + effect.handler()
                    + "' returned null for effect '" + effect.id() + "'");
        }
        return handler;
    }
}
