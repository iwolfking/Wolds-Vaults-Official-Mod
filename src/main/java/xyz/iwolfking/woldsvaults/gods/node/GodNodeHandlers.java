package xyz.iwolfking.woldsvaults.gods.node;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The one registry of god node handler types, keyed by the type string config binds to. Every type
 * must be registered before the god tree configs are read.
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

    /** Registers a handler type and the record its extra config fields decode into; twice is fatal. */
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

    /** Builds the handler instance for one effect; fails if its handler type is not registered. */
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
