package xyz.iwolfking.woldsvaults.gods.node;

/**
 * The handler of a node the tree places but has no behaviour for yet.
 *
 * <p>Binding a deliberately unbuilt node to a type of its own rather than to
 * {@link ListenerBoundHandler} is what lets load-time validation tell "not built yet" apart from
 * "built, but reached through this god's own listeners". Without the distinction a node whose
 * mechanic was never written would be indistinguishable from one that works.
 */
public record DeferredHandler(GodEffect effect) implements GodNodeHandler {
}
