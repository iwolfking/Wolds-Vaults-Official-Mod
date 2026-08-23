package xyz.iwolfking.woldsvaults.gods.node;

/** A node in the damage pipeline, placed by {@link #order()}: damage math, on-hit and enemy effects. */
public interface CombatContributor extends GodNodeHandler {
    int ORDER_DEFAULT = 100;

    /** The player in {@code context} is the one dealing the hit. */
    default void onOutgoing(GodNodeContext context, GodDamageContext damage) {
    }

    /** The player in {@code context} is the one taking the hit. */
    default void onIncoming(GodNodeContext context, GodDamageContext damage) {
    }

    /** Ascending pipeline position. */
    default int order() {
        return ORDER_DEFAULT;
    }
}
