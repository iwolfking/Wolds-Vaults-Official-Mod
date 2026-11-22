package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import org.jetbrains.annotations.NotNull;

/**
 * A milestone row's background in its rarity's animated frame (see {@link MilestoneFrame}), on the grey plate or the
 * navy one, with or without its flourishes.
 */
public class MilestoneFrameElement extends AbstractSpatialElement<MilestoneFrameElement> implements IRenderedElement {
    private final int rarity;
    private final int seed;
    private final boolean navy;
    private final boolean ornaments;
    private boolean visible = true;

    public MilestoneFrameElement(ISpatial spatial, int rarity, int seed) {
        this(spatial, rarity, seed, false, true);
    }

    public MilestoneFrameElement(ISpatial spatial, int rarity, int seed, boolean navy, boolean ornaments) {
        super(spatial);
        this.rarity = rarity;
        this.seed = seed;
        this.navy = navy;
        this.ornaments = ornaments;
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
        MilestoneFrame.draw(poseStack, at.x(), at.y(), at.z(), at.width(), at.height(), this.rarity, this.seed,
                this.navy, this.ornaments);
    }
}
