package xyz.iwolfking.woldsvaults.api.lib;

import java.util.UUID;

/** A Storm Arrow storm that follows one mob (Stormcrown storm tracking). */
public interface TrackingStorm {
    void woldsvaults$track(UUID target, float blocksPerSecond);
}
