package xyz.iwolfking.woldsvaults.client.screens.stats;

import iskallia.vault.client.gui.screen.player.StatisticsElementContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.stats.ServerboundStatSyncRequestMessage;

import java.util.Map;

/**
 * Server-computed Statistics tab values for the local player. While the tab is open the client asks for fresh values
 * every second; rows show a synced value while it is recent and fall back to their local calculation otherwise.
 */
public final class StatSyncClient {
    private static final int REQUEST_INTERVAL_TICKS = 20;
    private static final long MAX_AGE_MILLIS = 3000L;

    private static volatile Map<String, Double> values = Map.of();
    private static volatile long receivedAt;
    private static int ticksOpen = -1;

    private StatSyncClient() {
    }

    public static void accept(Map<String, Double> synced) {
        values = Map.copyOf(synced);
        receivedAt = System.currentTimeMillis();
    }

    /** The synced value for a key converted to the row's own number type, or the local value when none is recent. */
    @SuppressWarnings("unchecked")
    public static <V> V value(String key, V local) {
        if (System.currentTimeMillis() - receivedAt > MAX_AGE_MILLIS) {
            return local;
        }
        Double synced = values.get(key);
        if (synced == null) {
            return local;
        }
        if (local instanceof Float) {
            return (V) Float.valueOf(synced.floatValue());
        }
        if (local instanceof Integer) {
            return (V) Integer.valueOf((int) Math.round(synced));
        }
        if (local instanceof Double) {
            return (V) synced;
        }
        if (local instanceof Long) {
            return (V) Long.valueOf(Math.round(synced));
        }
        return local;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.screen instanceof StatisticsElementContainerScreen)) {
            ticksOpen = -1;
            return;
        }
        ticksOpen++;
        if (ticksOpen % REQUEST_INTERVAL_TICKS == 0) {
            ModNetwork.sendToServer(new ServerboundStatSyncRequestMessage());
        }
    }

    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        values = Map.of();
        receivedAt = 0L;
        ticksOpen = -1;
    }
}
