package xyz.iwolfking.woldsvaults.gods.sacrifice;

/** The sacrificial altar's memory of the redstone state it last saw, carried on the cauldron tile itself. */
public interface SacrificeAltarEdge {
    /**
     * Records the current signal and reports whether it is a rising edge; the first sample after the tile is
     * created never is.
     */
    boolean woldsvaults$observePower(boolean powered);
}
