package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

/** Tuning for Clock Artificier, read by its mixin. */
public final class WendarrShards {
    private WendarrShards() {
    }

    public static float clockArtificierMultiplier() {
        return WendarrNodeHandlers.params(WendarrNodes.CLOCK_ARTIFICIER,
                WendarrNodeHandlers.ClockArtificierParams.class).multiplier();
    }
}
