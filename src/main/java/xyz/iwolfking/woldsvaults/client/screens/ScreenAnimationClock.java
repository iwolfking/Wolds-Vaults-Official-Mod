package xyz.iwolfking.woldsvaults.client.screens;

import net.minecraft.Util;

/**
 * The seconds the greed and H menu animations run on. The clock starts from zero whenever an animated screen is drawn
 * after a pause of more than a second, so its float value stays small and precise, and it only wraps after six hours
 * of one screen staying open.
 */
public final class ScreenAnimationClock {
    private static final long RESTART_AFTER_MILLIS = 1_000L;
    private static final long WRAP_MILLIS = 6L * 60L * 60L * 1_000L;

    private static long origin = Util.getMillis();
    private static long lastRead = origin;

    private ScreenAnimationClock() {
    }

    /** Seconds since the animated screen being drawn was opened. */
    public static float seconds() {
        long now = Util.getMillis();
        if (now - lastRead > RESTART_AFTER_MILLIS || now - origin >= WRAP_MILLIS) {
            origin = now;
        }
        lastRead = now;
        return (now - origin) / 1000.0F;
    }
}
