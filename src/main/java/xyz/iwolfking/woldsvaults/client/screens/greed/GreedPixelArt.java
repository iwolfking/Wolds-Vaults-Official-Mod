package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.function.IntUnaryOperator;

/**
 * Small drawing helpers shared by the greed screen's navy row styles: the navy palette, Minecraft-style bevels, little
 * pixel sprites drawn as one quad per pixel (pushpin, tick, coin, cog), colour mixing and text at a whole-number scale.
 */
public final class GreedPixelArt {
    public static final int NAVY_ROW = 0xFF1B2231;
    public static final int NAVY_ROW_ALT = 0xFF161C28;
    public static final int NAVY_FACE = 0xFF232A3A;
    public static final int NAVY_LIGHT = 0xFF2C3549;
    public static final int NAVY_HIGHLIGHT = 0xFF3E4A63;
    public static final int NAVY_DARK = 0xFF10141C;
    public static final int NAVY_OUTLINE = 0xFF05070B;
    public static final int NAVY_TRACK = 0xFF0B0E15;
    public static final int NAVY_TEXT = 0xFFB8C3D9;
    public static final int NAVY_DIM = 0xFF6E7891;
    public static final int NAVY_OFF_TEXT = 0xFF5A6478;
    public static final int GREEN = 0xFF6FD36F;
    public static final int CREAM = 0xFFFFF4D0;
    public static final int PALE = 0xFFFFE9A8;

    /** A pixel sprite; each character is a palette colour (see {@link #colour(char)}) and '.' is clear. */
    public record Sprite(String... rows) {
        public int width() {
            return this.rows[0].length();
        }

        public int height() {
            return this.rows.length;
        }
    }

    public static final Sprite PIN = new Sprite(".hhh.", ".hhh.", ".hhh.", "hhhhh", "..n..", "..n..", "..n..");
    public static final Sprite CHECK = new Sprite("......c", ".....c.", "c...c..", ".c.c...", "..c....");
    public static final Sprite COIN = new Sprite("..ggg..", ".gGGgg.", "gGggggd", "gGggggd", "gggggdd", ".gggdd.", "..ddd..");
    public static final Sprite COG = new Sprite(
            "....o....",
            ".o.ooo.o.",
            "..ooooo..",
            ".oo...oo.",
            "ooo...ooo",
            ".oo...oo.",
            "..ooooo..",
            ".o.ooo.o.",
            "....o....");

    private GreedPixelArt() {
    }

    /** The colour a sprite character stands for. */
    public static int colour(char ch) {
        return switch (ch) {
            case 'h', 'g' -> GreedTheme.GOLD;
            case 'G' -> PALE;
            case 'd' -> GreedTheme.GOLD_DIM;
            case 'n' -> 0xFF8A8F99;
            case 'c' -> GREEN;
            case 'o' -> NAVY_TEXT;
            default -> 0;
        };
    }

    public static void sprite(IElementRenderer renderer, PoseStack poseStack, Sprite sprite, int x, int y, int z,
                              IntUnaryOperator tint) {
        for (int row = 0; row < sprite.height(); row++) {
            String line = sprite.rows()[row];
            for (int column = 0; column < line.length(); column++) {
                int colour = colour(line.charAt(column));
                if (colour != 0) {
                    fill(renderer, poseStack, x + column, y + row, z, 1, 1, tint == null ? colour : tint.applyAsInt(colour));
                }
            }
        }
    }

    public static void fill(IElementRenderer renderer, PoseStack poseStack, int x, int y, int z, int w, int h, int colour) {
        if (w > 0 && h > 0) {
            renderer.renderColoredQuad(poseStack, colour, Spatials.positionXYZ(x, y, z).size(w, h));
        }
    }

    public static void hollow(IElementRenderer renderer, PoseStack poseStack, int x, int y, int z, int w, int h, int colour) {
        fill(renderer, poseStack, x, y, z, w, 1, colour);
        fill(renderer, poseStack, x, y + h - 1, z, w, 1, colour);
        fill(renderer, poseStack, x, y, z, 1, h, colour);
        fill(renderer, poseStack, x + w - 1, y, z, 1, h, colour);
    }

    /** Minecraft's raised look: an outline, a light top and left edge and a dark bottom and right one. */
    public static void bevel(IElementRenderer renderer, PoseStack poseStack, int x, int y, int z, int w, int h,
                             int face, int light, int dark, int outline) {
        fill(renderer, poseStack, x, y, z, w, h, outline);
        fill(renderer, poseStack, x + 1, y + 1, z, w - 2, h - 2, face);
        fill(renderer, poseStack, x + 1, y + 1, z, w - 2, 1, light);
        fill(renderer, poseStack, x + 1, y + 1, z, 1, h - 2, light);
        fill(renderer, poseStack, x + 1, y + h - 2, z, w - 2, 1, dark);
        fill(renderer, poseStack, x + w - 2, y + 1, z, 1, h - 2, dark);
    }

    public static int mix(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = Math.round(((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = Math.round((a & 255) + ((b & 255) - (a & 255)) * t);
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    public static int scale(int colour, float k) {
        return mix(0xFF000000, colour, k);
    }

    // the same colour greyed out and darkened, for icons that are off
    public static int greyed(int colour) {
        int luma = Math.round((((colour >> 16) & 255) * 0.3F + ((colour >> 8) & 255) * 0.59F + (colour & 255) * 0.11F) * 0.55F);
        return 0xFF000000 | luma << 16 | luma << 8 | Math.min(255, luma + 10);
    }

    /** Text with its drop shadow at a whole-number scale, its top left at (x, y) and depth z. */
    public static void text(PoseStack poseStack, Component text, float x, float y, int z, int colour, int scale) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, 1.0F);
        Minecraft.getInstance().font.drawShadow(poseStack, text, 0.0F, 0.0F, colour);
        poseStack.popPose();
    }

    public static int width(Component text, int scale) {
        return Minecraft.getInstance().font.width(text) * scale;
    }

    /** The molten gold of the reputation bar for row j of a fill h rows tall, for fills that don't animate. */
    public static int moltenRow(int j, int h) {
        float v = h > 1 ? j / (float) (h - 1) : 0.0F;
        return v < 0.15F ? 0xFFFFD978 : mix(0xFFD39D33, 0xFF6E4E14, v);
    }
}
