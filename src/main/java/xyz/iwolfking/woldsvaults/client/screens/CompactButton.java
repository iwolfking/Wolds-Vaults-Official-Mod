package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

/**
 * A vanilla button shorter than 20 px: the top rows of the button texture plus its black bottom row, with the label
 * centered on whole pixels. Vanilla draws only the top rows at these heights, which leaves the bottom edge open.
 */
public class CompactButton extends Button {
    public static final int STATE_DISABLED = 0;
    public static final int STATE_NORMAL = 1;
    public static final int STATE_HOVERED = 2;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DISABLED = 0xFFA0A0A0;
    private static final float LIT_R = 1.0F;
    private static final float LIT_G = 0.85F;
    private static final float LIT_B = 0.25F;

    public CompactButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress);
    }

    public CompactButton(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, width, height, message, onPress, onTooltip);
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        int state = !this.active ? STATE_DISABLED : this.isHoveredOrFocused() ? STATE_HOVERED : STATE_NORMAL;
        draw(pose, Minecraft.getInstance().font, this.x, this.y, this.width, this.height, this.getMessage().getString(),
                state, this.active ? TEXT : TEXT_DISABLED, 1.0F, false);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(pose, mouseX, mouseY);
        }
    }

    /** Draws a compact button; the label is drawn at {@code scale} in {@code color}, and a lit button is tinted yellow. */
    public static void draw(PoseStack pose, Font font, int x, int y, int w, int h, String label, int state, int color,
                            float scale, boolean lit) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
        if (lit) {
            RenderSystem.setShaderColor(LIT_R, LIT_G, LIT_B, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int v = 46 + state * 20;
        int half = w / 2;
        GuiComponent.blit(pose, x, y, 0, v, half, h - 1, 256, 256);
        GuiComponent.blit(pose, x + half, y, 200 - (w - half), v, w - half, h - 1, 256, 256);
        GuiComponent.blit(pose, x, y + h - 1, 0, v + 19, half, 1, 256, 256);
        GuiComponent.blit(pose, x + half, y + h - 1, 200 - (w - half), v + 19, w - half, 1, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        String text = font.plainSubstrByWidth(label, (int) ((w - 4) / scale));
        pose.pushPose();
        pose.translate(x + (w - font.width(text) * scale) / 2.0F, y + (float) Math.ceil((h - 8 * scale) / 2.0F), 0.0F);
        pose.scale(scale, scale, 1.0F);
        font.drawShadow(pose, text, 0.0F, 0.0F, color);
        pose.popPose();
    }
}
