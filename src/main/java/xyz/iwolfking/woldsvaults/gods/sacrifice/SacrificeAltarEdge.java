package xyz.iwolfking.woldsvaults.gods.sacrifice;

/**
 * The sacrificial altar's memory of the redstone state it last saw, carried on the cauldron tile
 * itself through its mixin so that it lives and dies with the block - no table of positions to
 * prune when an altar is broken or its chunk unloads.
 */
public interface SacrificeAltarEdge {
    /**
     * Records the current signal and reports whether it is a rising edge. The first sample after
     * the tile is created is never an edge: a signal already present when the altar wakes up is
     * not a transition the altar witnessed, so an altar left powered does not re-fire on every
     * chunk load.
     */
    boolean woldsvaults$observePower(boolean powered);
}
