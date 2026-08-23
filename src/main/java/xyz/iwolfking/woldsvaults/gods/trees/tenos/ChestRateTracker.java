package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.Optional;

/**
 * Per-player chests-per-minute over a sliding window, feeding Looting Engine. It is advanced lazily
 * from the wall clock, lives in {@link GodNodeState} and is cleared on vault entry.
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
