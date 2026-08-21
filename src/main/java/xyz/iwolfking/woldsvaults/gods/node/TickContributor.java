package xyz.iwolfking.woldsvaults.gods.node;

import net.minecraft.server.level.ServerPlayer;

/**
 * A node with a periodic effect. Every tick contributor of every god runs on the one shared
 * ticker in {@link GodNodeTicker}, at that ticker's cadence - no node registers its own tick
 * listener.
 */
public interface TickContributor extends GodNodeHandler {
    void tick(GodNodeContext context);

    /**
     * Called once when this effect stops being live for a player, with whatever it last applied
     * still in place. This is the half {@link #tick} cannot express: a contributor that reaches
     * outside the attribute snapshot - a global damage factor, a vanilla attribute modifier, a mob
     * effect, a vault modifier - owns removing it, and once the gate is gone there is no further
     * tick in which to notice.
     *
     * <p>{@link GodNodeTicker} fires it on every real deactivation path: the node refunded, the
     * charm swapped, the gate lost for any other reason, and logout. On logout the player is still
     * a live entity, which is what makes removing an applied modifier there work.
     *
     * <p>It is never called for a player who never held the effect, and never twice without an
     * intervening {@link #tick}. Contributions that live purely in the attribute snapshot need
     * nothing here - they disappear the moment the snapshot is rebuilt without their gate.
     */
    default void onDeactivated(ServerPlayer player, String effectId) {
    }
}
