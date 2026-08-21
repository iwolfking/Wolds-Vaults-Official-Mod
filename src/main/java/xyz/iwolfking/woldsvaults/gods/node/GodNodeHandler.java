package xyz.iwolfking.woldsvaults.gods.node;

/**
 * Base type of everything a god node effect can do. A handler is built once per effect at config
 * load and implements zero or more of {@link StatContributor}, {@link VaultContributor},
 * {@link CombatContributor} and {@link TickContributor} - those four capabilities are the whole
 * answer to how a node's effect reaches the game.
 */
public interface GodNodeHandler {
}
