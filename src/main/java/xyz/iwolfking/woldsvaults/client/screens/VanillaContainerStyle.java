package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Draws container backgrounds from the vanilla chest texture so code-drawn screens match every other inventory:
 * the rounded light-grey panel, bevelled slots and recessed wells. Follows the active resource pack.
 */
public final class VanillaContainerStyle {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private static final int TEX = 256;
    private static final int SRC_W = 176;
    private static final int SRC_H = 222;
    private static final int CORNER = 4;
    private static final int SLOT_U = 7;
    private static final int SLOT_V = 17;
    private static final int BODY = 0xFFC6C6C6;
    private static final int WELL_SHADOW = 0xFF373737;
    private static final int WELL_FILL = 0xFF8B8B8B;
    private static final int WELL_LIGHT = 0xFFFFFFFF;

    private VanillaContainerStyle() {
    }

    private static void bind() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void part(PoseStack pose, int x, int y, int w, int h, int u, int v, int uw, int vh) {
        if (w > 0 && h > 0) {
            GuiComponent.blit(pose, x, y, w, h, u, v, uw, vh, TEX, TEX);
        }
    }

    /** The vanilla container panel at any size: texture corners and edges, flat body. */
    public static void panel(PoseStack pose, int x, int y, int w, int h) {
        GuiComponent.fill(pose, x + CORNER, y + CORNER, x + w - CORNER, y + h - CORNER, BODY);
        bind();
        int r = SRC_W - CORNER;
        int b = SRC_H - CORNER;
        int innerW = w - 2 * CORNER;
        int innerH = h - 2 * CORNER;
        part(pose, x, y, CORNER, CORNER, 0, 0, CORNER, CORNER);
        part(pose, x + w - CORNER, y, CORNER, CORNER, r, 0, CORNER, CORNER);
        part(pose, x, y + h - CORNER, CORNER, CORNER, 0, b, CORNER, CORNER);
        part(pose, x + w - CORNER, y + h - CORNER, CORNER, CORNER, r, b, CORNER, CORNER);
        part(pose, x + CORNER, y, innerW, CORNER, CORNER, 0, 1, CORNER);
        part(pose, x + CORNER, y + h - CORNER, innerW, CORNER, CORNER, b, 1, CORNER);
        part(pose, x, y + CORNER, CORNER, innerH, 0, CORNER, CORNER, 1);
        part(pose, x + w - CORNER, y + CORNER, CORNER, innerH, r, CORNER, CORNER, 1);
    }

    /** One vanilla slot background for an item drawn at (x, y). */
    public static void slot(PoseStack pose, int x, int y) {
        bind();
        GuiComponent.blit(pose, x - 1, y - 1, 0, SLOT_U, SLOT_V, 18, 18, TEX, TEX);
    }

    /** Slot backgrounds for every slot of the menu, relative to the screen's top-left corner. */
    public static void slots(PoseStack pose, AbstractContainerMenu menu, int left, int top) {
        for (Slot slot : menu.slots) {
            slot(pose, left + slot.x, top + slot.y);
        }
    }

    /** A recessed area with the slot bevel (dark top-left, white bottom-right), for lists and bars. */
    public static void well(PoseStack pose, int x0, int y0, int x1, int y1) {
        GuiComponent.fill(pose, x0, y0, x1, y1, WELL_FILL);
        GuiComponent.fill(pose, x0, y0, x1 - 1, y0 + 1, WELL_SHADOW);
        GuiComponent.fill(pose, x0, y0, x0 + 1, y1 - 1, WELL_SHADOW);
        GuiComponent.fill(pose, x0 + 1, y1 - 1, x1, y1, WELL_LIGHT);
        GuiComponent.fill(pose, x1 - 1, y0 + 1, x1, y1, WELL_LIGHT);
    }
}
