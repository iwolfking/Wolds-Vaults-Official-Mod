package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Code-drawn machine chrome for the extraction stations (Vault Fabricator, Extraction Forge): a chamfered steel
 * housing with a hard outline and two-tone bevel, raised bolted plates, engraved labels, sharp slots, dark readout
 * windows, vertical gauges with a requirement marker, full-height metal buttons and the forge's ember glow.
 * The layout spec is {@code library/ui_preview/screens/industrial.py} in the pack repo.
 */
public final class IndustrialStyle {
    public static final int OUTLINE = 0xFF101214;
    public static final int BODY = 0xFFA3A8AE;
    public static final int BODY_HI = 0xFFDDE1E5;
    public static final int BODY_LO = 0xFF5E646A;
    public static final int PLATE = 0xFF8D9399;
    public static final int PLATE_HI = 0xFFC6CBD0;
    public static final int PLATE_LO = 0xFF474C52;
    public static final int SEAM_DARK = 0xFF6B7177;
    public static final int SEAM_LIGHT = 0xFFC9CDD2;
    public static final int SLOT_DARK = 0xFF222528;
    public static final int SLOT_LIGHT = 0xFFF0F2F4;
    public static final int SLOT_FILL = 0xFF71767C;
    public static final int TRAY_FILL = 0xFF7E848A;
    public static final int DISPLAY_BG = 0xFF1C2023;
    public static final int DISPLAY_SCAN = 0xFF20252A;
    public static final int ENGRAVE_DARK = 0xFF2B2F33;
    public static final int ENGRAVE_LIGHT = 0xFFCDD1D5;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_DIM = 0xFF8C969E;
    public static final int AMBER = 0xFFFFD27A;
    public static final int GREEN = 0xFF6EF08C;
    public static final int RED_TEXT = 0xFFFF7468;
    public static final int BLUE_TEXT = 0xFF7FB2FF;
    public static final int EMBER_TEXT = 0xFFFF9A3C;
    public static final int GHOST_WASH = 0xA071767C;
    public static final int[] FE_GAUGE = {0xFFC62E2E, 0xFFFF6F61, 0xFF7E1A1A};
    public static final int[] WATER_GAUGE = {0xFF2F6FD6, 0xFF74A6FF, 0xFF1A3F86};
    public static final int[] PROGRESS_BAR = {0xFF4FE3C4, 0xFFA6FFEA, 0xFF2A8F7A};
    public static final int[] EMBER_BAR = {0xFFFF8A2A, 0xFFFFC27A, 0xFFB8380E};
    public static final int[] PAID_BAR = {0xFF3FBF5A, 0xFF8BEA8F, 0xFF1F6E30};

    private IndustrialStyle() {
    }

    private static void fill(PoseStack pose, int x0, int y0, int x1, int y1, int color) {
        GuiComponent.fill(pose, x0, y0, x1, y1, color);
    }

    /** A rectangle with 45-degree corner cuts of {@code k} pixels. */
    public static void chamfer(PoseStack pose, int x0, int y0, int x1, int y1, int k, int color) {
        for (int i = 0; i < k; i++) {
            fill(pose, x0 + k - i, y0 + i, x1 - (k - i), y0 + i + 1, color);
            fill(pose, x0 + k - i, y1 - i - 1, x1 - (k - i), y1 - i, color);
        }
        fill(pose, x0, y0 + k, x1, y1 - k, color);
    }

    /** The machine housing: outline, chamfered corners, two-tone bevel and an engraved seam three pixels in. */
    public static void panel(PoseStack pose, int x0, int y0, int x1, int y1) {
        chamfer(pose, x0, y0, x1, y1, 3, OUTLINE);
        chamfer(pose, x0 + 1, y0 + 1, x1 - 2, y1 - 2, 2, BODY_HI);
        chamfer(pose, x0 + 2, y0 + 2, x1 - 1, y1 - 1, 2, BODY_LO);
        chamfer(pose, x0 + 2, y0 + 2, x1 - 2, y1 - 2, 1, BODY);
        groove(pose, x0 + 4, y0 + 4, x1 - 4, y1 - 4);
    }

    public static void groove(PoseStack pose, int x0, int y0, int x1, int y1) {
        fill(pose, x0, y0, x1 - 1, y0 + 1, SEAM_DARK);
        fill(pose, x0, y0, x0 + 1, y1 - 1, SEAM_DARK);
        fill(pose, x0 + 1, y0 + 1, x1, y0 + 2, SEAM_LIGHT);
        fill(pose, x0 + 1, y0 + 1, x0 + 2, y1, SEAM_LIGHT);
        fill(pose, x0, y1 - 1, x1, y1, SEAM_DARK);
        fill(pose, x1 - 1, y0, x1, y1, SEAM_DARK);
    }

    /** A raised sub-plate (rails, nameplates). */
    public static void plate(PoseStack pose, int x0, int y0, int x1, int y1, int color) {
        fill(pose, x0, y0, x1, y1, OUTLINE);
        fill(pose, x0 + 1, y0 + 1, x1 - 1, y1 - 1, PLATE_HI);
        fill(pose, x0 + 2, y0 + 2, x1 - 1, y1 - 1, PLATE_LO);
        fill(pose, x0 + 2, y0 + 2, x1 - 2, y1 - 2, color);
    }

    public static void plate(PoseStack pose, int x0, int y0, int x1, int y1) {
        plate(pose, x0, y0, x1, y1, PLATE);
    }

    /** A 4x4 bolt head with its top-left corner at (x, y). */
    public static void bolt(PoseStack pose, int x, int y) {
        fill(pose, x + 1, y, x + 3, y + 4, 0xFF2E3236);
        fill(pose, x, y + 1, x + 4, y + 3, 0xFF2E3236);
        fill(pose, x + 1, y + 1, x + 3, y + 3, 0xFFB5BBC1);
        fill(pose, x + 1, y + 1, x + 2, y + 2, 0xFFF2F4F6);
        fill(pose, x + 2, y + 2, x + 3, y + 3, 0xFF5D6369);
    }

    /** A 6x6 bolt head for corner brackets. */
    public static void bigBolt(PoseStack pose, int x, int y) {
        fill(pose, x + 1, y, x + 5, y + 6, 0xFF2A2E32);
        fill(pose, x, y + 1, x + 6, y + 5, 0xFF2A2E32);
        fill(pose, x + 1, y + 1, x + 5, y + 5, 0xFFB5BBC1);
        fill(pose, x + 1, y + 1, x + 3, y + 2, 0xFFF2F4F6);
        fill(pose, x + 1, y + 1, x + 2, y + 3, 0xFFF2F4F6);
        fill(pose, x + 3, y + 4, x + 5, y + 5, 0xFF5D6369);
        fill(pose, x + 4, y + 3, x + 5, y + 5, 0xFF5D6369);
        fill(pose, x + 2, y + 2, x + 4, y + 4, 0xFF7A8086);
    }

    /**
     * A corner bracket: two six-pixel steel arms, 18 pixels long, with a big bolt at the joint. (x, y) is the outer
     * corner and (dx, dy) point into the panel.
     */
    public static void bracket(PoseStack pose, int x, int y, int dx, int dy) {
        int ax0 = dx > 0 ? x : x - 18;
        int ax1 = dx > 0 ? x + 18 : x;
        int ay0 = dy > 0 ? y : y - 18;
        int ay1 = dy > 0 ? y + 18 : y;
        int hy0 = dy > 0 ? ay0 : ay1 - 6;
        int vx0 = dx > 0 ? ax0 : ax1 - 6;
        bracketArm(pose, ax0, hy0, ax1, hy0 + 6);
        bracketArm(pose, vx0, ay0, vx0 + 6, ay1);
        fill(pose, vx0 + 1, hy0 + 1, vx0 + 5, hy0 + 5, 0xFF6F757B);
        bigBolt(pose, vx0, hy0);
    }

    private static void bracketArm(PoseStack pose, int x0, int y0, int x1, int y1) {
        fill(pose, x0, y0, x1, y1, OUTLINE);
        fill(pose, x0 + 1, y0 + 1, x1 - 1, y1 - 1, 0xFF6F757B);
        fill(pose, x0 + 1, y0 + 1, x1 - 1, y0 + 2, 0xFFB8BDC2);
    }

    /** Text stamped into the steel: a light lip one pixel below, dark text on top. */
    public static void engraved(PoseStack pose, Font font, String text, int x, int y) {
        font.draw(pose, text, x, y + 1, ENGRAVE_LIGHT);
        font.draw(pose, text, x, y, ENGRAVE_DARK);
    }

    public static void engravedCentered(PoseStack pose, Font font, String text, int cx, int y) {
        engraved(pose, font, text, cx - font.width(text) / 2, y);
    }

    /** A sharp slot frame for an item drawn at (x, y). */
    public static void slot(PoseStack pose, int x, int y, int color) {
        fill(pose, x - 1, y - 1, x + 17, y + 17, color);
        fill(pose, x - 1, y - 1, x + 16, y, SLOT_DARK);
        fill(pose, x - 1, y - 1, x, y + 16, SLOT_DARK);
        fill(pose, x, y + 16, x + 17, y + 17, SLOT_LIGHT);
        fill(pose, x + 16, y, x + 17, y + 17, SLOT_LIGHT);
    }

    public static void slot(PoseStack pose, int x, int y) {
        slot(pose, x, y, SLOT_FILL);
    }

    /** A result slot: the 16x16 item area at (x, y) inside a 26x26 recessed frame. */
    public static void bigSlot(PoseStack pose, int x, int y) {
        recess(pose, x - 5, y - 5, x + 21, y + 21, SLOT_FILL);
        fill(pose, x - 2, y - 2, x + 18, y - 1, SLOT_DARK);
        fill(pose, x - 2, y - 2, x - 1, y + 18, SLOT_DARK);
        fill(pose, x - 1, y + 17, x + 18, y + 18, SLOT_LIGHT);
        fill(pose, x + 17, y - 1, x + 18, y + 18, SLOT_LIGHT);
    }

    public static void recess(PoseStack pose, int x0, int y0, int x1, int y1, int color) {
        fill(pose, x0, y0, x1, y1, color);
        fill(pose, x0, y0, x1 - 1, y0 + 1, SLOT_DARK);
        fill(pose, x0, y0, x0 + 1, y1 - 1, SLOT_DARK);
        fill(pose, x0 + 1, y1 - 1, x1, y1, SLOT_LIGHT);
        fill(pose, x1 - 1, y0 + 1, x1, y1, SLOT_LIGHT);
    }

    /** A dark readout window with faint scanlines; draw white shadowed text on it. */
    public static void display(PoseStack pose, int x0, int y0, int x1, int y1) {
        recess(pose, x0, y0, x1, y1, DISPLAY_BG);
        for (int y = y0 + 2; y < y1 - 1; y += 2) {
            fill(pose, x0 + 1, y, x1 - 1, y + 1, DISPLAY_SCAN);
        }
    }

    /** A recessed tray behind a slot grid. */
    public static void tray(PoseStack pose, int x0, int y0, int x1, int y1) {
        recess(pose, x0, y0, x1, y1, TRAY_FILL);
    }

    /**
     * A vertical gauge filled from the bottom to {@code fraction}, with quarter ticks on the right and, when
     * {@code marker} is not negative, a white requirement line at that fraction.
     */
    public static void gauge(PoseStack pose, int x0, int y0, int x1, int y1, float fraction, int[] colors, float marker) {
        display(pose, x0, y0, x1, y1);
        int top = y0 + 1;
        int bottom = y1 - 1;
        int span = bottom - top;
        int fillH = Math.round(span * Math.max(0.0F, Math.min(1.0F, fraction)));
        if (fillH > 0) {
            fill(pose, x0 + 1, bottom - fillH, x1 - 1, bottom, colors[0]);
            fill(pose, x0 + 1, bottom - fillH, x0 + 2, bottom, colors[1]);
            fill(pose, x1 - 2, bottom - fillH, x1 - 1, bottom, colors[2]);
            fill(pose, x0 + 1, bottom - fillH, x1 - 1, bottom - fillH + 1, colors[1]);
        }
        for (int q = 1; q <= 3; q++) {
            int ty = bottom - Math.round(span * q / 4.0F);
            fill(pose, x1 - 3, ty, x1 - 1, ty + 1, fillH > bottom - ty ? 0xFF000000 : 0xFF4A5056);
        }
        if (marker >= 0.0F) {
            int my = bottom - Math.max(1, Math.round(span * Math.min(1.0F, marker)));
            fill(pose, x0 + 1, my, x1 - 1, my + 1, 0xFFFFFFFF);
        }
    }

    /** A horizontal bar filled from the left to {@code fraction}. */
    public static void hbar(PoseStack pose, int x0, int y0, int x1, int y1, float fraction, int[] colors) {
        display(pose, x0, y0, x1, y1);
        int w = Math.round((x1 - x0 - 2) * Math.max(0.0F, Math.min(1.0F, fraction)));
        if (w > 0) {
            fill(pose, x0 + 1, y0 + 1, x0 + 1 + w, y1 - 1, colors[0]);
            fill(pose, x0 + 1, y0 + 1, x0 + 1 + w, y0 + 2, colors[1]);
        }
    }

    /** The forge-active heat strip down a side edge: orange, hottest and most opaque at the bottom. */
    public static void emberGlow(PoseStack pose, int x0, int y0, int x1, int y1, float pulse) {
        int h = y1 - y0;
        for (int i = 0; i < h; i++) {
            float t = i / (float) Math.max(1, h - 1);
            int g = (int) (70 + 110 * (1 - t));
            int b = (int) (10 + 30 * (1 - t));
            int a = (int) ((110 + 145 * t) * pulse);
            fill(pose, x0, y0 + i, x1, y0 + i + 1, (Math.min(255, a) << 24) | (0xFF << 16) | (g << 8) | b);
        }
    }

    /** An item drawn as a translucent ghost: the icon washed with the slot colour. */
    public static void ghost(PoseStack pose, ItemStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x, y);
        RenderSystem.disableDepthTest();
        fill(pose, x, y, x + 16, y + 16, GHOST_WASH);
        RenderSystem.enableDepthTest();
    }

    /** An item icon at {@code scale} with its top-left corner at (x, y), through the model-view stack. */
    public static void item(ItemStack stack, float x, float y, float scale) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(x, y, 0.0D);
        modelView.scale(scale, scale, 1.0F);
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, 0, 0);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    /** The colour scheme of a {@link MachineButton}. */
    public enum ButtonStyle {
        STEEL(0xFF7C8288, 0xFFC4C9CE, 0xFF42474C, 0xFFFFFFFF),
        GREEN(0xFF2F9440, 0xFF8BEA8F, 0xFF155A22, 0xFFFFFFFF),
        DISABLED(0xFF5E6368, 0xFF7A8085, 0xFF44494E, 0xFFA0A6AB);

        final int face;
        final int hi;
        final int lo;
        final int text;

        ButtonStyle(int face, int hi, int lo, int text) {
            this.face = face;
            this.hi = hi;
            this.lo = lo;
            this.text = text;
        }
    }

    /**
     * A metal button drawn in full at any height (a vanilla Button shorter than 20 loses its bottom edge). Label
     * and style are read every frame, so one button can turn from Stage into a green Fabricate. A DISABLED style
     * also makes it unclickable.
     */
    public static class MachineButton extends Button {
        private final Supplier<String> label;
        private final Supplier<ButtonStyle> style;

        public MachineButton(int x, int y, int w, int h, Supplier<String> label, Supplier<ButtonStyle> style, OnPress onPress) {
            super(x, y, w, h, new TextComponent(label.get()), onPress);
            this.label = label;
            this.style = style;
        }

        @Override
        public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
            ButtonStyle s = this.style.get();
            this.active = s != ButtonStyle.DISABLED;
            int x0 = this.x;
            int y0 = this.y;
            int x1 = x0 + this.width;
            int y1 = y0 + this.height;
            boolean lit = this.active && this.isHoveredOrFocused();
            fill(pose, x0, y0, x1, y1, lit ? 0xFFFFFFFF : OUTLINE);
            fill(pose, x0 + 1, y0 + 1, x1 - 1, y1 - 1, s.hi);
            fill(pose, x0 + 2, y0 + 2, x1 - 1, y1 - 1, s.lo);
            fill(pose, x0 + 2, y0 + 2, x1 - 2, y1 - 2, s.face);
            Font font = Minecraft.getInstance().font;
            String text = this.label.get();
            font.drawShadow(pose, text, x0 + (this.width - font.width(text)) / 2, y0 + (this.height - 8) / 2, s.text);
        }
    }
}
