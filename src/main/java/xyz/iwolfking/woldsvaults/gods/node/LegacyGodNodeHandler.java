package xyz.iwolfking.woldsvaults.gods.node;

/**
 * The handler of an effect whose numbers have moved into config but whose behaviour still lives
 * in its own god module, reached through that module's own listeners rather than through a
 * capability seam.
 *
 * <p>It implements none of the four capabilities on purpose: it exists so config can name a
 * registered handler type for an effect that has not been ported yet, and it disappears one
 * effect at a time as each god's nodes move onto real handlers.
 */
public record LegacyGodNodeHandler(GodEffect effect) implements GodNodeHandler {
}
