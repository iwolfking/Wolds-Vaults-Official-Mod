package xyz.iwolfking.woldsvaults.api.core;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WoldLogger {
    private static final Set<String> ONCE_KEYS = ConcurrentHashMap.newKeySet();

    public static void info(String message, Object... args) {
        WoldsVaults.LOGGER.info(message, args);
    }

    public static void error(String message, Object... args) {
        WoldsVaults.LOGGER.error(message, args);
    }

    /** Logs through {@link #error} only the first time {@code key} is seen. */
    public static void errorOnce(String key, String message, Object... args) {
        if (ONCE_KEYS.add(key)) {
            error(message, args);
        }
    }

    public static void debug(String message, Object... args) {
        if(WoldsVaultsConfig.COMMON.enableDebugMode.get()) {
            WoldsVaults.LOGGER.debug(message, args);
        }
    }

    public static void warn(String message, Object... args) {
        if(WoldsVaultsConfig.COMMON.enableDebugMode.get()) {
            WoldsVaults.LOGGER.warn(message, args);
        }
    }
}
