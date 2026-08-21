package xyz.iwolfking.woldsvaults.gods.node;

/**
 * A node with a periodic effect. Every tick contributor of every god runs on the one shared
 * ticker in {@link GodNodeTicker}, at that ticker's cadence - no node registers its own tick
 * listener.
 */
public interface TickContributor extends GodNodeHandler {
    void tick(GodNodeContext context);
}
