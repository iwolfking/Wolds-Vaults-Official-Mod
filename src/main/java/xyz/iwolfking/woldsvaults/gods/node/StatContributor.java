package xyz.iwolfking.woldsvaults.gods.node;

/**
 * The default capability: a node that adds gear attributes to the player's attribute snapshot.
 *
 * <p>{@link #contribute} runs during snapshot construction and receives the live player, so the
 * value is computed at query time and never baked at purchase time. That single property is what
 * gives stat nodes their three required behaviours at once - they update inside a vault, they
 * show on the stats screen, and they disappear the moment the snapshot is rebuilt without their
 * gate.
 */
public interface StatContributor extends GodNodeHandler {
    void contribute(GodNodeContext context, GodStatSink sink);
}
