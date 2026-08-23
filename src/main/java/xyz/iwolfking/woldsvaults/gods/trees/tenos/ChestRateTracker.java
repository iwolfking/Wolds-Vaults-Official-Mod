package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.Optional;

/**
 * Per-player chests-per-minute rate over a five minute sliding window, feeding Looting Engine.
 *
 * <p>A fixed ring of one-second buckets, advanced lazily from the wall clock the next time the
 * player is read or credited. That is deliberate: the base mod's
 * {@code SlidingTimedTargetTaskCounter} registers a server-tick listener per instance, which would
 * mean one listener per tracked player. Nothing here ticks; the window is derived from timestamps.
 *
 * <p>The window is one player's live scratch for Looting Engine, so it lives in
 * {@link GodNodeState} under that effect rather than in a map of this class's own, and the god
 * core's own teardown on logout and vault-listener leave is what drops it. Vault entry clears it
 * too, from the join listener below: a rate carried in from the last run or from the loot the
 * player was opening outside one would pay Looting Engine for chests this vault never saw.
 *
 * <p>Fed from {@code CHEST_LOOT_GENERATION}, the same event the milestone engine listens to, but
 * through its own independent subscription so neither system can disturb the other.
 */
public final class ChestRateTracker {
    public static final int WINDOW_SECONDS = 300;

    private static final Object OWNER = new Object();

    private ChestRateTracker() {
    }

    static void register() {
        CommonEvents.CHEST_LOOT_GENERATION.post().register(OWNER, data -> {
            ServerPlayer player = data.getPlayer();
            if (player != null) {
                credit(player);
            }
        });
        CommonEvents.LISTENER_JOIN.register(OWNER, data -> data.getListener().getPlayer().ifPresent(
                player -> GodNodeState.clear(player.getUUID(), TenosNodes.LOOTING_ENGINE)));
    }

    public static void credit(ServerPlayer player) {
        GodNodeState.get(player.getUUID(), TenosNodes.LOOTING_ENGINE, Window::new).add();
    }

    /** Chests opened per minute, averaged over the last {@value #WINDOW_SECONDS} seconds. */
    public static float getChestsPerMinute(ServerPlayer player) {
        Optional<Window> window = GodNodeState.peek(player.getUUID(), TenosNodes.LOOTING_ENGINE);
        return window.map(Window::perMinute).orElse(0.0F);
    }

    private static final class Window {
        private final int[] buckets = new int[WINDOW_SECONDS];
        private long lastSecond = seconds();

        private static long seconds() {
            return System.currentTimeMillis() / 1000L;
        }

        private synchronized void advance() {
            long now = seconds();
            long elapsed = now - this.lastSecond;
            if (elapsed <= 0) {
                return;
            }
            if (elapsed >= WINDOW_SECONDS) {
                java.util.Arrays.fill(this.buckets, 0);
            } else {
                for (long i = 1; i <= elapsed; i++) {
                    this.buckets[(int) ((this.lastSecond + i) % WINDOW_SECONDS)] = 0;
                }
            }
            this.lastSecond = now;
        }

        private synchronized void add() {
            advance();
            this.buckets[(int) (this.lastSecond % WINDOW_SECONDS)]++;
        }

        private synchronized int total() {
            advance();
            int sum = 0;
            for (int bucket : this.buckets) {
                sum += bucket;
            }
            return sum;
        }

        private synchronized float perMinute() {
            return total() * 60.0F / WINDOW_SECONDS;
        }
    }
}
