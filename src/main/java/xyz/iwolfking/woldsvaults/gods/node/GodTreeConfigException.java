package xyz.iwolfking.woldsvaults.gods.node;

import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Fatal god tree configuration error. Every load-time assertion throws this rather than logging
 * and continuing: a tree that catches its own parse failure renders an uncharted sky and takes a
 * whole god's progression with it, which is exactly the silent failure this architecture exists
 * to remove.
 */
public class GodTreeConfigException extends RuntimeException {
    public GodTreeConfigException(String message) {
        super(message);
    }

    public GodTreeConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Logs {@code message} and returns the exception to throw, so the offending id reaches the
     * log even when a caller further up decides to wrap or rethrow.
     */
    public static GodTreeConfigException fail(String message) {
        WoldsVaults.LOGGER.error("God tree config error: {}", message);
        return new GodTreeConfigException(message);
    }

    /** As {@link #fail(String)}, keeping the cause of a parse or reflection failure attached. */
    public static GodTreeConfigException fail(String message, Throwable cause) {
        WoldsVaults.LOGGER.error("God tree config error: {}", message, cause);
        return new GodTreeConfigException(message, cause);
    }
}
