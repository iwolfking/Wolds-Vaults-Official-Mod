package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

/** Tuning for Clock Artificier (r81), kept out of the mixin so the number lives with the tree. */
public final class WendarrShards {
    public static float clockArtificierMultiplier() {
        return GodNodeValues.number(WendarrNodes.CLOCK_ARTIFICIER, "multiplier");
    }

    private WendarrShards() {
    }
}
