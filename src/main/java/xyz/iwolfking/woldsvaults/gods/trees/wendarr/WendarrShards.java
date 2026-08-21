package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

/** Tuning for Clock Artificier (r81), kept out of the mixin so the number lives with the tree. */
public final class WendarrShards {
    private WendarrShards() {
    }

    public static float clockArtificierMultiplier() {
        return WendarrNodeHandlers.params(WendarrNodes.CLOCK_ARTIFICIER,
                WendarrNodeHandlers.ClockArtificierParams.class).multiplier();
    }
}
