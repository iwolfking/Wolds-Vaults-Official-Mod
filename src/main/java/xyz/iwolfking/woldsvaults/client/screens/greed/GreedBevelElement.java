package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import org.jetbrains.annotations.NotNull;

/** A plain Minecraft-style bevelled panel in the given colours (see {@link GreedPixelArt#bevel}). */
public class GreedBevelElement extends AbstractSpatialElement<GreedBevelElement> implements IRenderedElement {
    private final int face;
    private final int light;
    private final int dark;
    private final int outline;
    private boolean visible = true;

    public GreedBevelElement(ISpatial spatial, int face, int light, int dark, int outline) {
        super(spatial);
        this.face = face;
        this.light = light;
        this.dark = dark;
        this.outline = outline;
    }

    /** The navy plate the navy row styles sit on. */
    public static GreedBevelElement navyRow(ISpatial spatial) {
        return new GreedBevelElement(spatial, GreedPixelArt.NAVY_ROW, GreedPixelArt.NAVY_LIGHT, GreedPixelArt.NAVY_DARK,
                GreedPixelArt.NAVY_OUTLINE);
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
        GreedPixelArt.bevel(renderer, poseStack, at.x(), at.y(), at.z(), at.width(), at.height(),
                this.face, this.light, this.dark, this.outline);
    }
}
