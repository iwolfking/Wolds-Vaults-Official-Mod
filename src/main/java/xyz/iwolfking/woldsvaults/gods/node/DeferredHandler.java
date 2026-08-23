package xyz.iwolfking.woldsvaults.gods.node;

/** The handler of a node the tree places but has no behaviour for yet, as opposed to a broken binding. */
public record DeferredHandler(GodEffect effect) implements GodNodeHandler {
}
