package xyz.iwolfking.woldsvaults.gods.node;

/**
 * The handler of an effect whose numbers live in config but whose behaviour reaches the game
 * through its own god module's listeners and mixins rather than through one of the four
 * capability seams.
 *
 * <p>It implements none of the capabilities on purpose. Binding an effect here is a statement that
 * its mechanic is not expressible as a {@link StatContributor}, {@link VaultContributor},
 * {@link CombatContributor} or {@link TickContributor} - cancelling a hit outright, reviving on
 * death, attributing a heal, or composing several nodes across a whole party into one value are
 * all shapes those four interfaces cannot carry. Load-time validation still checks the effect's
 * params and value table exactly as it does for a ported handler.
 *
 * <p>This is a different statement from {@link DeferredHandler}, which marks a node that has no
 * behaviour at all yet.
 */
public record ListenerBoundHandler(GodEffect effect) implements GodNodeHandler {
}
