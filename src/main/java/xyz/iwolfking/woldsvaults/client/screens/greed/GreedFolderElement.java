package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import org.jetbrains.annotations.NotNull;

/**
 * The body of the greed folder: the navy panel under the tab strip that holds a tab's search row and list, in the
 * same colour as the selected tab so that tab reads as part of it. A Minecraft-style bevel, lit along the top and left
 * and shaded along the bottom and right; the selected tab draws over its top edge to open into it.
 */
public class GreedFolderElement extends AbstractSpatialElement<GreedFolderElement> implements IRenderedElement {
    static final int OUTLINE = 0xFF05070B;
    static final int FACE = 0xFF182032;
    static final int LIGHT = 0xFF3E4A63;
    static final int SIDE = 0xFF2C3549;
    static final int DARK = 0xFF10141C;

    private boolean visible = true;

    public GreedFolderElement(ISpatial spatial) {
        super(spatial);
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ISpatial at = this.getWorldSpatial();
        int x = at.x();
        int y = at.y();
        int z = at.z();
        int w = at.width();
        int h = at.height();
        fill(renderer, poseStack, x, y, z, w, h, OUTLINE);
        fill(renderer, poseStack, x + 1, y + 1, z, w - 2, h - 2, FACE);
        fill(renderer, poseStack, x + 1, y + 1, z, w - 2, 1, LIGHT);
        fill(renderer, poseStack, x + 1, y + 1, z, 1, h - 2, SIDE);
        fill(renderer, poseStack, x + 2, y + h - 2, z, w - 3, 1, DARK);
        fill(renderer, poseStack, x + w - 2, y + 2, z, 1, h - 3, DARK);
    }

    static void fill(IElementRenderer renderer, PoseStack poseStack, int x, int y, int z, int w, int h, int colour) {
        renderer.renderColoredQuad(poseStack, colour, Spatials.positionXYZ(x, y, z).size(w, h));
    }
}
