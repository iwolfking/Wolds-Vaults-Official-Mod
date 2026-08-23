package xyz.iwolfking.woldsvaults.gods.node;

/**
 * The handler of an effect whose numbers live in config but whose behaviour reaches the game through
 * its own god module's listeners and mixins, implementing none of the four capabilities.
 */
public record ListenerBoundHandler(GodEffect effect) implements GodNodeHandler {
}
