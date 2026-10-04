package xyz.iwolfking.woldsvaults.objectives.hyper;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

/**
 * Diagnostic logging for the HYPER objective, gated behind {@code enableHyperVerboseLogging}
 * in woldsvaults-common.toml (default false). Fight, cycle, damage and policy traces go through
 * here so a production server's log stays quiet; genuine failures still log directly through
 * {@link WoldsVaults#LOGGER} at error level.
 */
public final class HyperLog {
    private HyperLog() {
    }

    /**
     * True when the server operator has turned on verbose hyper logging.
     */
    public static boolean enabled() {
        return WoldsVaultsConfig.COMMON.enableHyperVerboseLogging.get();
    }

    public static void info(String message, Object... args) {
        if (enabled()) {
            WoldsVaults.LOGGER.info(message, args);
        }
    }

    public static void warn(String message, Object... args) {
        if (enabled()) {
            WoldsVaults.LOGGER.warn(message, args);
        }
    }
}
