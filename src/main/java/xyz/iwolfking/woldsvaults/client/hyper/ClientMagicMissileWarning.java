package xyz.iwolfking.woldsvaults.client.hyper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Client-side mirror of the hyperboss Magic Missile charge, fed by
 * MagicMissileWarningMessage each server tick while the volley telegraphs. The boss-bar
 * mixin reads it to drive the Wave-Blast-style countdown. Freshness is judged against the
 * client world's game time, so the display survives a singleplayer pause (both sides freeze
 * together) but self-hides within a few ticks once the server stops sending — no explicit
 * teardown packet is required for boss death or vault exit.
 */
public final class ClientMagicMissileWarning {
    private static final int FRESH_TICKS = 4;

    private static int remainingTicks;
    private static int windowTicks;
    private static long receivedGameTime = Long.MIN_VALUE;

    private ClientMagicMissileWarning() {
    }

    public static void update(int remaining, int window) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || window <= 0) {
            windowTicks = 0;
            return;
        }
        remainingTicks = remaining;
        windowTicks = window;
        receivedGameTime = level.getGameTime();
    }

    public static boolean isActive() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || windowTicks <= 0) {
            return false;
        }
        long age = level.getGameTime() - receivedGameTime;
        return age >= 0L && age <= FRESH_TICKS;
    }

    public static int getRemainingTicks() {
        return remainingTicks;
    }

    public static int getWindowTicks() {
        return windowTicks;
    }
}
