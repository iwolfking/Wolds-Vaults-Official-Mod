package xyz.iwolfking.woldsvaults.api.util;

public class TimeUtils {
    public static String formatTime(long ticks) {
        return String.format("%.2f s", ticks / 20.0);
    }

    public static String formatRemaining(long millis) {
        long s = millis / 1000;
        return (s / 3600) + "h " + ((s % 3600) / 60) + "m";
    }
}
