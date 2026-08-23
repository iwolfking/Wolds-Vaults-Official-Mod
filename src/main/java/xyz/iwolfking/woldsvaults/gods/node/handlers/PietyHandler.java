package xyz.iwolfking.woldsvaults.gods.node.handlers;

import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandler;

/**
 * The shared handler of every god's Pious Devotion node: {@code values[0]} piety per invested point,
 * read by each god's own {@code PietyBonusSource} rather than through a capability.
 */
public record PietyHandler(GodEffect effect) implements GodNodeHandler {
    public static final String TYPE = "piety";

    /** Piety per invested point, unscaled by any carryover the caller applies. */
    public float perPoint() {
        return this.effect.value(0);
    }
}
