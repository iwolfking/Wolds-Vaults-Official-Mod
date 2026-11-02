package xyz.iwolfking.woldsvaults.client.screens.gods;

import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Logs a god tree screen fallback once per key, so a fallback taken every frame does not flood the log. */
public final class GodTreeLog {
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private GodTreeLog() {
    }

    public static void warnOnce(String key, String message, Object... args) {
        if (WARNED.add(key)) {
            WoldsVaults.LOGGER.warn(message, args);
        }
    }

    public static void errorOnce(String key, String message, Object... args) {
        if (WARNED.add(key)) {
            WoldsVaults.LOGGER.error(message, args);
        }
    }
}
