package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player chests-per-minute rate over a five minute sliding window, feeding Looting Engine.
 *
 * <p>A fixed ring of one-second buckets, advanced lazily from the wall clock the next time the
 * player is read or credited. That is deliberate: the base mod's
 * {@code SlidingTimedTargetTaskCounter} registers a server-tick listener per instance, which would
 * mean one listener per tracked player. Nothing here ticks; the window is derived from timestamps.
 *
 * <p>Fed from {@code CHEST_LOOT_GENERATION}, the same event the milestone engine listens to, but
 * through its own independent subscription so neither system can disturb the other.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ChestRateTracker {
    public static final int WINDOW_SECONDS = 300;

    private static final Map<UUID, Window> WINDOWS = new ConcurrentHashMap<>();
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
    }

    public static void credit(ServerPlayer player) {
        WINDOWS.computeIfAbsent(player.getUUID(), id -> new Window()).add();
    }

    /** Chests opened per minute, averaged over the last {@value #WINDOW_SECONDS} seconds. */
    public static float getChestsPerMinute(ServerPlayer player) {
        Window window = WINDOWS.get(player.getUUID());
        return window == null ? 0.0F : window.perMinute();
    }

    public static void clearPlayer(UUID playerId) {
        WINDOWS.remove(playerId);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayer(event.getPlayer().getUUID());
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
