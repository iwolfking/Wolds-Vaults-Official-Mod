package xyz.iwolfking.woldsvaults.gods.node;

/**
 * The single place every built-in god node handler type is registered, in the shape the base
 * mod's talent adapters use:
 *
 * <pre>{@code GodNodeHandlers.register("gear_attribute_scaled", GearAttributeScaledParams.class, GearAttributeScaledHandler::new);}</pre>
 *
 * <p>{@link #bootstrap()} is called from the god tree config load, which is the only ordering
 * guarantee that holds: the addon's configs are read from the base mod's parallel common-setup
 * dispatch, before any {@code enqueueWork} tree setup runs, so a tree module that registered its
 * handlers from its own setup would register them after validation had already rejected them.
 */
public final class GodNodeHandlerTypes {
    private static boolean bootstrapped;

    private GodNodeHandlerTypes() {
    }

    /** Registers every built-in handler type exactly once, on whichever thread loads configs first. */
    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
    }
}
