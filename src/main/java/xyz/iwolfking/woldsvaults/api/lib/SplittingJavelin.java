package xyz.iwolfking.woldsvaults.api.lib;

/** Marks a thrown javelin produced by the ancient Helm of the Warbound pre-split; such copies do not split again. */
public interface SplittingJavelin {
    boolean woldsvaults$hasPreSplit();

    void woldsvaults$setPreSplit(boolean preSplit);
}
