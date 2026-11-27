package xyz.iwolfking.woldsvaults.gods.node;

/**
 * Base type of everything a god node effect can do: built once per effect at config load, and
 * implementing zero or more of {@link StatContributor}, {@link VaultContributor},
 * {@link CombatContributor} and {@link TickContributor}, or else {@link ListenerBoundHandler}.
 */
public interface GodNodeHandler {
}
