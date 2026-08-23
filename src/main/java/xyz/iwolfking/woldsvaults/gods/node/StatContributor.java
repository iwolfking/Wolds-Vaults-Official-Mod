package xyz.iwolfking.woldsvaults.gods.node;

/**
 * The default capability: a node that adds gear attributes to the player's attribute snapshot.
 * {@link #contribute} runs during snapshot construction, so its value is computed at query time.
 */
public interface StatContributor extends GodNodeHandler {
    void contribute(GodNodeContext context, GodStatSink sink);
}
