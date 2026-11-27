package xyz.iwolfking.woldsvaults.api.lib;

/** A Scatter split child of a Helm of the Warbound throw, which tracks its own hits and homes in on the nearest enemy. */
public interface HomingJavelin {
    boolean woldsvaults$isHoming();

    void woldsvaults$startHoming();
}
