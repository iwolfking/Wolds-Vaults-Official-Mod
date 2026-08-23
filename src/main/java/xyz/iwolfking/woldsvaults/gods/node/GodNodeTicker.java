package xyz.iwolfking.woldsvaults.gods.node;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The one periodic pass every god tree shares: once a second it drops the gate cache, runs every
 * live {@link TickContributor}, then the {@link TreePass} passes. An effect that has lost its gate
 * gets exactly one {@link TickContributor#onDeactivated} call.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GodNodeTicker {
    private static final int PERIOD_TICKS = 20;

    private static final Map<UUID, Set<String>> LIVE = new ConcurrentHashMap<>();
    private static final List<TreePass> TREE_PASSES = new CopyOnWriteArrayList<>();

    /** One god tree's cross-player periodic pass: once per ticker pass, with every online player. */
    public interface TreePass {
        void run(MinecraftServer server, List<ServerPlayer> players);
    }

    private GodNodeTicker() {
    }

    /** Adds a tree pass to the shared ticker; passes run in registration order. */
    public static void registerTreePass(TreePass pass) {
        TREE_PASSES.add(pass);
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
        if (!ticking.isEmpty()) {
            for (ServerPlayer player : players) {
                pass(player, ticking, true);
            }
        }
        for (TreePass treePass : TREE_PASSES) {
            try {
                treePass.run(server, players);
            } catch (RuntimeException e) {
                WoldsVaults.LOGGER.error("God tree pass {} threw; this pass was skipped.",
                        treePass.getClass().getName(), e);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        LIVE.clear();
    }

    /** Runs the deactivation diff for one player right now, without ticking anything. */
    public static void reconcile(ServerPlayer player) {
        List<GodEffect> ticking = GodNodeRegistry.effectsWith(TickContributor.class);
        if (!ticking.isEmpty()) {
            pass(player, ticking, false);
        }
    }

    /** Deactivates every effect still live for a player and forgets them; call while they are still live. */
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
