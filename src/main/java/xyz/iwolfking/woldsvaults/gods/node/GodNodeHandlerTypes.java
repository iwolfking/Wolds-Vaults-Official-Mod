package xyz.iwolfking.woldsvaults.gods.node;

import xyz.iwolfking.woldsvaults.gods.node.handlers.GearAttributeScaledHandler;
import xyz.iwolfking.woldsvaults.gods.node.handlers.PietyHandler;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.trees.tenos.TenosNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.trees.wendarr.WendarrNodeHandlers;

/**
 * The single place every built-in god node handler type is registered. {@link #bootstrap()} must be
 * called from the god tree config load; anything later arrives after validation has rejected it.
 */
public final class GodNodeHandlerTypes {
    private static boolean bootstrapped;

    private GodNodeHandlerTypes() {
    }

    /** Registers every built-in handler type exactly once. */
    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        GodNodeHandlers.register(GearAttributeScaledHandler.TYPE, GearAttributeScaledHandler.Params.class,
                GearAttributeScaledHandler::new);
        GodNodeHandlers.register(PietyHandler.TYPE, PietyHandler::new);
        IdonaNodeHandlers.register();
        VelaraNodeHandlers.register();
        WendarrNodeHandlers.register();
        TenosNodeHandlers.register();
    }
}
