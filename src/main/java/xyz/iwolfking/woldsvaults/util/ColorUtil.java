package xyz.iwolfking.woldsvaults.util;

import net.minecraft.util.Mth;

import java.util.List;

public class ColorUtil {
    public static int brightenColor(int color, float factor) {
        int a = (color >>> 24) & 0xFF;
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;

        r = Mth.clamp(Math.round(r * factor), 0, 255);
        g = Mth.clamp(Math.round(g * factor), 0, 255);
        b = Mth.clamp(Math.round(b * factor), 0, 255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int mixColors(List<Integer> colors) {
        if (colors == null || colors.isEmpty()) {
            return 0xFFFFFF;
        }

        int r = 0, g = 0, b = 0;
        int size = colors.size();

        for (int color : colors) {
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }

        r /= size;
        g /= size;
        b /= size;

        return (r << 16) | (g << 8) | b;
    }
}
