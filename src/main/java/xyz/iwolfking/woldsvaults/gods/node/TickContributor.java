package xyz.iwolfking.woldsvaults.gods.node;

import net.minecraft.server.level.ServerPlayer;

/** A node with a periodic effect, run on the one shared ticker in {@link GodNodeTicker}. */
public interface TickContributor extends GodNodeHandler {
    void tick(GodNodeContext context);

    /**
     * Called once when this effect stops being live for a player, with whatever it last applied
     * still in place, and never twice without an intervening {@link #tick}.
     */
    default void onDeactivated(ServerPlayer player, String effectId) {
    }
}
