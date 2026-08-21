package xyz.iwolfking.woldsvaults.api.lib;

/**
 * Marks a thrown javelin that was produced by the ancient Helm of the Warbound pre-split, so the
 * split copies scatter normally instead of splitting again.
 */
public interface SplittingJavelin {
    boolean woldsvaults$hasPreSplit();

    void woldsvaults$setPreSplit(boolean preSplit);
}
