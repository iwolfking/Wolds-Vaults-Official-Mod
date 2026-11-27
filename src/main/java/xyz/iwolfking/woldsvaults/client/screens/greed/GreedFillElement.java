package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

/** Flat filled rectangle with an optional one pixel border. */
public class GreedFillElement extends AbstractSpatialElement<GreedFillElement> implements IRenderedElement {
    private final IntSupplier fillColor;
    private final IntSupplier borderColor;
    private boolean visible = true;

    public GreedFillElement(ISpatial spatial, int fillColor) {
        this(spatial, () -> fillColor, null);
    }

    public GreedFillElement(ISpatial spatial, int fillColor, int borderColor) {
        this(spatial, () -> fillColor, () -> borderColor);
    }

    public GreedFillElement(ISpatial spatial, IntSupplier fillColor, IntSupplier borderColor) {
        super(spatial);
        this.fillColor = fillColor;
        this.borderColor = borderColor;
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
        renderer.renderColoredQuad(poseStack, this.fillColor.getAsInt(), this.getWorldSpatial());
        if (this.borderColor != null) {
            renderer.renderColoredHollowRect(poseStack, this.borderColor.getAsInt(), this.getWorldSpatial());
        }
    }
}
