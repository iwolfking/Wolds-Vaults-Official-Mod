package xyz.iwolfking.woldsvaults.api.core;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

public class WoldLogger {
    public static void info(String message, Object... args) {
        WoldsVaults.LOGGER.info(message, args);
    }

    public static void error(String message, Object... args) {
        WoldsVaults.LOGGER.error(message, args);
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
