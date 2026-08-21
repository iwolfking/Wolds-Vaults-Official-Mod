package xyz.iwolfking.woldsvaults.gods.node;

/**
 * A node that takes part in damage math, on-hit effects or enemy effects.
 *
 * <p>Contributors run inside one ordered pipeline rather than as their own Forge listeners, so
 * the outcome of a hit does not depend on event priority, registration order or class-loading
 * order. {@link #order()} places a contributor within that pipeline.
 */
public interface CombatContributor extends GodNodeHandler {
    int ORDER_DEFAULT = 100;

    /** The player in {@code context} is the one dealing the hit. */
    default void onOutgoing(GodNodeContext context, GodDamageContext damage) {
    }

    /** The player in {@code context} is the one taking the hit. */
    default void onIncoming(GodNodeContext context, GodDamageContext damage) {
    }

    /** Ascending pipeline position; ties are broken by effect id so ordering is deterministic. */
    default int order() {
        return ORDER_DEFAULT;
    }
}
