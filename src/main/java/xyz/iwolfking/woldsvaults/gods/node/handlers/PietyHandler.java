package xyz.iwolfking.woldsvaults.gods.node.handlers;

import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandler;

/**
 * The shared handler of every god's Pious Devotion node: {@code values[0]} piety per invested
 * point.
 *
 * <p>It implements none of the four capabilities because piety has its own seam - each god ships
 * a {@code PietyBonusSource} that reads this effect's table - and a node whose whole effect is a
 * piety grant must not also land in the attribute snapshot. Binding it to a named type rather
 * than to the catch-all is what lets load-time validation tell a Pious Devotion node from an
 * unported one.
 */
public record PietyHandler(GodEffect effect) implements GodNodeHandler {
    /** The type string config binds; registered from {@code GodNodeHandlerTypes}. */
    public static final String TYPE = "piety";

    /** Piety granted per invested point, before the caller applies its own carryover scale. */
    public float perPoint() {
        return this.effect.value(0);
    }
}
