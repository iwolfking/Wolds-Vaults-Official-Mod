package xyz.iwolfking.woldsvaults.client.screens.gods;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/** Logs a god tree screen fallback once per key, so a fallback taken every frame does not flood the log. */
public final class GodTreeLog {
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private GodTreeLog() {
    }

    public static void errorOnce(String key, String message, Object... args) {
        if (WARNED.add(key)) {
            WoldLogger.error(message, args);
        }
    }
}
