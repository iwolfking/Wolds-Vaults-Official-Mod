package xyz.iwolfking.woldsvaults.gods.node;

/**
 * Base type of everything a god node effect can do. A handler is built once per effect at config
 * load and implements zero or more of {@link StatContributor}, {@link VaultContributor},
 * {@link CombatContributor} and {@link TickContributor}.
 *
 * <p>Those four capabilities are not the whole answer to how a node reaches the game, and were
 * never meant to be. They cover the shapes that are per-player and per-effect: contributing to the
 * attribute snapshot, changing the vault being entered, scaling a hit, and doing something
 * periodically. Mechanics outside those shapes - cancelling a hit outright, reviving on death,
 * attributing a heal to its caster, or composing several nodes across a whole party into one value
 * - stay on their god's own listeners and bind {@link ListenerBoundHandler}. Velara is almost
 * entirely the latter, which is a property of a defensive party tree rather than unfinished work.
 *
 * <p>What every effect does share, ported or not, is that its numbers live in config and are
 * validated at load.
 */
public interface GodNodeHandler {
}
