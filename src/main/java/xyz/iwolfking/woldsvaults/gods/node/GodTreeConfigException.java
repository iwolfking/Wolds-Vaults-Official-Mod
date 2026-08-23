package xyz.iwolfking.woldsvaults.gods.node;

import xyz.iwolfking.woldsvaults.WoldsVaults;

/** Fatal god tree configuration error; every load-time assertion throws this rather than continuing. */
public class GodTreeConfigException extends RuntimeException {
    public GodTreeConfigException(String message) {
        super(message);
    }

    public GodTreeConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Logs {@code message} and returns the exception to throw. */
    public static GodTreeConfigException fail(String message) {
        WoldsVaults.LOGGER.error("God tree config error: {}", message);
        return new GodTreeConfigException(message);
    }

    public static GodTreeConfigException fail(String message, Throwable cause) {
        WoldsVaults.LOGGER.error("God tree config error: {}", message, cause);
        return new GodTreeConfigException(message, cause);
    }
}
