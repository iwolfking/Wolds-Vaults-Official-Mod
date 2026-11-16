package xyz.iwolfking.woldsvaults.api.util;

import com.mojang.math.Vector3f;
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

    public static Vector3f vector(int rgb) {
        return new Vector3f((rgb >> 16 & 255) / 255.0F, (rgb >> 8 & 255) / 255.0F, (rgb & 255) / 255.0F);
    }

    public static int rgb(Vector3f color) {
        return (Mth.clamp((int) (color.x() * 255), 0, 255) << 16) | (Mth.clamp((int) (color.y() * 255), 0, 255) << 8) | Mth.clamp((int) (color.z() * 255), 0, 255);
    }

    // moves each channel towards white by the amount
    public static Vector3f lighten(Vector3f color, float amount) {
        return new Vector3f(Mth.lerp(amount, color.x(), 1.0F), Mth.lerp(amount, color.y(), 1.0F), Mth.lerp(amount, color.z(), 1.0F));
    }
}
