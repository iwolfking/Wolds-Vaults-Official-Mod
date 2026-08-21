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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The one periodic pass every god tree shares. It drops the gate cache once a second - which is
 * what makes a node purchase, a refund or a piety change take effect without every mutator having
 * to know the cache exists - and then runs every {@link TickContributor} that is live for each
 * player.
 *
 * <p>No node registers its own tick listener. One pass per second for the whole server is the
 * budget; anything that needs finer granularity belongs on {@link CombatContributor} or on the
 * attribute snapshot, not here.
 *
 * <p>The ticker also owns the other half of a tick contributor's life. It remembers which effects
 * were live for each player on the previous pass, so an effect that has since lost its gate gets
 * exactly one {@link TickContributor#onDeactivated} call - which is the only chance a contributor
 * has to take back something it applied outside the attribute snapshot. The periodic diff covers a
 * gate lost for any reason; {@link #reconcile(ServerPlayer)} runs the same diff immediately for the
 * paths that already know the gate has moved (charm swap, dimension change, refund), and
 * {@link #deactivateAll(ServerPlayer)} drains a player on logout while they are still a live entity.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GodNodeTicker {
    private static final int PERIOD_TICKS = 20;

    private static final Map<UUID, Set<String>> LIVE = new ConcurrentHashMap<>();

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
            pass(player, ticking, true);
        }
    }

    /**
     * Runs the deactivation diff for one player right now, without ticking anything. Called from
     * the paths that have just moved the gate - a charm swap, a dimension change, a respec - so a
     * contributor's teardown lands on the same event the player sees rather than up to a second
     * later.
     */
    public static void reconcile(ServerPlayer player) {
        List<GodEffect> ticking = GodNodeRegistry.effectsWith(TickContributor.class);
        if (!ticking.isEmpty()) {
            pass(player, ticking, false);
        }
    }

    /**
     * Deactivates every effect still live for a player and forgets them, for logout. The player is
     * still a live entity here, so a contributor removing a vanilla modifier or a mob effect
     * succeeds; forgetting them afterwards is what stops a re-login from firing a second
     * deactivation for state that is already gone.
     */
    public static void deactivateAll(ServerPlayer player) {
        Set<String> live = LIVE.remove(player.getUUID());
        if (live == null || live.isEmpty()) {
            return;
        }
        for (String effectId : live) {
            deactivate(player, effectId);
        }
    }

    private static void pass(ServerPlayer player, List<GodEffect> ticking, boolean tick) {
        Set<String> previous = LIVE.get(player.getUUID());
        Set<String> live = new LinkedHashSet<>();
        for (GodEffect effect : ticking) {
            TickContributor handler = GodNodeRegistry.handler(effect.id(), TickContributor.class);
            if (handler == null) {
                continue;
            }
            GodNodeContext context = GodNodeGate.context(player, effect).orElse(null);
            if (context == null) {
                continue;
            }
            live.add(effect.id());
            if (!tick) {
                continue;
            }
            try {
                handler.tick(context);
            } catch (RuntimeException e) {
                WoldsVaults.LOGGER.error("God tick contributor {} threw for {}; this pass was skipped.",
                        effect.id(), player.getGameProfile().getName(), e);
            }
        }
        if (previous != null) {
            for (String effectId : previous) {
                if (!live.contains(effectId)) {
                    deactivate(player, effectId);
                }
            }
        }
        if (live.isEmpty()) {
            LIVE.remove(player.getUUID());
        } else {
            LIVE.put(player.getUUID(), live);
        }
    }

    private static void deactivate(ServerPlayer player, String effectId) {
        TickContributor handler = GodNodeRegistry.handler(effectId, TickContributor.class);
        if (handler == null) {
            WoldsVaults.LOGGER.warn("God effect {} was live for {} but is no longer a tick contributor; whatever it "
                    + "applied cannot be taken back.", effectId, player.getGameProfile().getName());
            return;
        }
        try {
            handler.onDeactivated(player, effectId);
        } catch (RuntimeException e) {
            WoldsVaults.LOGGER.error("God tick contributor {} threw while deactivating for {}; its state may be left "
                    + "applied.", effectId, player.getGameProfile().getName(), e);
        }
    }
}
