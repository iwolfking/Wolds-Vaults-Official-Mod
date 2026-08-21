package xyz.iwolfking.woldsvaults.gods.node;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeCache;
import xyz.iwolfking.woldsvaults.gods.GodNodeGate;

import java.util.List;

/**
 * The one periodic pass every god tree shares. It drops the gate cache once a second - which is
 * what makes a node purchase, a refund or a piety change take effect without every mutator having
 * to know the cache exists - and then runs every {@link TickContributor} that is live for each
 * player.
 *
 * <p>No node registers its own tick listener. One pass per second for the whole server is the
 * budget; anything that needs finer granularity belongs on {@link CombatContributor} or on the
 * attribute snapshot, not here.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GodNodeTicker {
    private static final int PERIOD_TICKS = 20;

    private GodNodeTicker() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % PERIOD_TICKS != 0) {
            return;
        }
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            GodNodeCache.refresh(player);
        }
        List<GodEffect> ticking = GodNodeRegistry.effectsWith(TickContributor.class);
        if (ticking.isEmpty()) {
            return;
        }
        for (ServerPlayer player : players) {
            for (GodEffect effect : ticking) {
                TickContributor handler = GodNodeRegistry.handler(effect.id(), TickContributor.class);
                if (handler != null) {
                    GodNodeGate.context(player, effect).ifPresent(handler::tick);
                }
            }
        }
    }
}
