package xyz.iwolfking.woldsvaults.api.util.ducks;

/**
 * Exposes whether a hyper rune boss fight is frozen because its boss is unloaded with its
 * chunk rather than dead or discarded.
 */
public interface DuckRuneBossFightFreeze {

    default boolean isBossUnloaded() {
        return false;
    }
}
