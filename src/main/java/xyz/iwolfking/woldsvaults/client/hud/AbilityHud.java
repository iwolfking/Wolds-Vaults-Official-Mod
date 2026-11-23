package xyz.iwolfking.woldsvaults.client.hud;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;

import java.util.function.Consumer;

/**
 * The bar a form shows in place of the hotbar while it's active, so each form only has to say what goes in it. A form's
 * RenderGameOverlayEvent.PreLayer handler calls {@link #replaceHotbar} while the form is up and draws on the AbilityHud
 * it's given: a panel and up to three ability slots drawn like item tooltips (dark purple, with the fading blue to purple
 * border), the slots where the hotbar's first, middle and last slots would be, each with its
 * key in the corner, a small label either side of the middle slot, and a time bar under the labels in two halves that
 * empties right to left. The hotbar and the held item's name are hidden while it shows.
 */
public final class AbilityHud {
    public enum Slot {
        LEFT, MIDDLE, RIGHT
    }

    // vanilla's tooltip colours
    public static final int BACKGROUND = 0xF0100010;
    private static final int BORDER_TOP = 0x505000FF;
    private static final int BORDER_BOTTOM = 0x5028007F;
    // for slot(): keep the tooltip's fading border
    public static final int TOOLTIP_BORDER = 0;
    private static final int SLOT_SIZE = 22;
    // the space between a side slot and the middle one
    private static final int GAP = 58;
    private static final float LABEL_SCALE = 0.75F;

    public final Minecraft mc;
    public final float partialTick;
    private final PoseStack pose;
    private final int left;
    private final int right;
    private final int middle;
    private final int top;

    private AbilityHud(Minecraft mc, PoseStack pose, int width, int height, float partialTick) {
        this.mc = mc;
        this.pose = pose;
        this.partialTick = partialTick;
        this.left = width / 2 - 91;
        this.right = this.left + 182;
        this.middle = this.left + 91;
        this.top = height - SLOT_SIZE;
    }

    // Call on every layer while the form is active: hides the hotbar and item name, and draws the content in their place.
    public static void replaceHotbar(RenderGameOverlayEvent.PreLayer event, Consumer<AbilityHud> content) {
        if (event.getOverlay() == ForgeIngameGui.ITEM_NAME_ELEMENT) {
            event.setCanceled(true);
        } else if (event.getOverlay() == ForgeIngameGui.HOTBAR_ELEMENT) {
            event.setCanceled(true);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && !mc.player.isSpectator()) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                content.accept(new AbilityHud(mc, event.getMatrixStack(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), event.getPartialTicks()));
                RenderSystem.disableBlend();
            }
        }
    }

    // for forms that draw their own extras over a slot (Lightning Fiend's charge ring)
    public PoseStack pose() {
        return this.pose;
    }

    public int slotX(Slot slot) {
        return x(slot);
    }

    public int top() {
        return this.top;
    }

    // the panel behind the whole bar, drawn like an item tooltip
    public void panel() {
        box(this.left - 3, this.top - 3, this.right + 3, this.top + SLOT_SIZE, BACKGROUND, TOOLTIP_BORDER);
    }

    // a tooltip-style slot with a pale veil over the part of its cooldown (0 to 1) still to run, as items show theirs
    public void cooldownSlot(Slot slot, ResourceLocation icon, float cooldown, KeyMapping key) {
        int x = x(slot);
        box(x, this.top, x + SLOT_SIZE, this.top + SLOT_SIZE, BACKGROUND, TOOLTIP_BORDER);
        icon(x, icon, 1.0F);
        if (cooldown > 0.0F) {
            GuiComponent.fill(this.pose, x + 3, this.top + 3 + Mth.floor(16.0F * (1.0F - cooldown)), x + 19, this.top + 19, 0x7FFFFFFF);
        }
        keyCorner(x, key);
    }

    // a tooltip-style slot; border TOOLTIP_BORDER keeps the tooltip's own fading border, anything else lights it in that colour
    public void slot(Slot slot, ResourceLocation icon, int border, int inside, float iconAlpha, KeyMapping key) {
        int x = x(slot);
        box(x, this.top, x + SLOT_SIZE, this.top + SLOT_SIZE, inside, border);
        icon(x, icon, iconAlpha);
        keyCorner(x, key);
    }

    private void icon(int x, ResourceLocation icon, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.setShaderTexture(0, icon);
        GuiComponent.blit(this.pose, x + 3, this.top + 3, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // vanilla's tooltip box: the background with its corners clipped, and a border one pixel in that fades top to bottom
    private void box(int x0, int y0, int x1, int y1, int inside, int border) {
        GuiComponent.fill(this.pose, x0 + 1, y0, x1 - 1, y1, inside);
        GuiComponent.fill(this.pose, x0, y0 + 1, x0 + 1, y1 - 1, inside);
        GuiComponent.fill(this.pose, x1 - 1, y0 + 1, x1, y1 - 1, inside);
        int upper = border == TOOLTIP_BORDER ? BORDER_TOP : border;
        int lower = border == TOOLTIP_BORDER ? BORDER_BOTTOM : border;
        GuiComponent.fill(this.pose, x0 + 1, y0 + 1, x1 - 1, y0 + 2, upper);
        GuiComponent.fill(this.pose, x0 + 1, y1 - 2, x1 - 1, y1 - 1, lower);
        int rows = y1 - y0 - 4;
        for (int i = 0; i < rows; i++) {
            int colour = lerpArgb(upper, lower, rows > 1 ? i / (float) (rows - 1) : 0.0F);
            GuiComponent.fill(this.pose, x0 + 1, y0 + 2 + i, x0 + 2, y0 + 3 + i, colour);
            GuiComponent.fill(this.pose, x1 - 2, y0 + 2 + i, x1 - 1, y0 + 3 + i, colour);
        }
    }

    private static int lerpArgb(int from, int to, float amount) {
        int out = 0;
        for (int shift = 0; shift < 32; shift += 8) {
            out |= ((int) Mth.lerp(amount, from >>> shift & 255, to >>> shift & 255) & 255) << shift;
        }
        return out;
    }

    public void labelLeft(String text, int colour) {
        label(text, this.left + 25, colour, false);
    }

    public void labelRight(String text, int colour) {
        label(text, this.right - 25, colour, true);
    }

    // how wide a label can be before it runs into the middle slot, for trimming ones that vary
    public int labelRoom() {
        return Mth.floor((GAP - 6) / LABEL_SCALE);
    }

    // the form's time left, 0 to 1
    public void timeBar(float remaining, int track, int fill, int shine) {
        int half = GAP - 6;
        int filled = Math.round(half * 2 * remaining);
        timeBarHalf(this.left + 25, half, Math.min(filled, half), track, fill, shine);
        timeBarHalf(this.middle + 14, half, Math.max(0, filled - half), track, fill, shine);
    }

    private void timeBarHalf(int x, int length, int filled, int track, int fill, int shine) {
        GuiComponent.fill(this.pose, x, this.top + 13, x + length, this.top + 18, track);
        if (filled > 2) {
            GuiComponent.fill(this.pose, x + 1, this.top + 14, x + filled - 1, this.top + 17, fill);
            GuiComponent.fill(this.pose, x + 1, this.top + 14, x + filled - 1, this.top + 15, shine);
        }
    }

    private int x(Slot slot) {
        return switch (slot) {
            case LEFT -> this.left;
            case MIDDLE -> this.middle - 11;
            case RIGHT -> this.right - SLOT_SIZE;
        };
    }

    // the slot's key, small in its bottom right corner
    private void keyCorner(int x, KeyMapping key) {
        String name = keyName(key);
        this.pose.pushPose();
        this.pose.translate(x + 20, this.top + 15, 0.0);
        this.pose.scale(0.5F, 0.5F, 1.0F);
        this.mc.font.drawShadow(this.pose, name, -this.mc.font.width(name), 0, 0xFFFFFF);
        this.pose.popPose();
    }

    private void label(String text, int x, int colour, boolean alignRight) {
        Font font = this.mc.font;
        this.pose.pushPose();
        this.pose.translate(alignRight ? x - font.width(text) * LABEL_SCALE : x, this.top + 4, 0.0);
        this.pose.scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
        font.drawShadow(this.pose, text, 0, 0, colour);
        this.pose.popPose();
    }

    // short enough for a slot's corner: the mouse buttons as LMB / RMB / MMB, anything else cut to four letters
    private static String keyName(KeyMapping key) {
        InputConstants.Key bound = key.getKey();
        if (bound.getType() == InputConstants.Type.MOUSE) {
            return switch (bound.getValue()) {
                case 0 -> "LMB";
                case 1 -> "RMB";
                case 2 -> "MMB";
                default -> "M" + (bound.getValue() + 1);
            };
        }
        String name = key.getTranslatedKeyMessage().getString();
        return name.length() > 4 ? name.substring(0, 4) : name;
    }
}
