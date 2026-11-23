package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * The tier a milestone has reached as a roman numeral on a badge in its rarity's colour (navy before the first tier):
 * a shield, a bevelled square like a vanilla advancement frame, or a small tag for one-line rows. The numeral is drawn
 * at double size where it fits.
 */
public class GreedTierBadgeElement extends AbstractSpatialElement<GreedTierBadgeElement> implements IRenderedElement {
    public enum Shape { SHIELD, SQUARE, TAG }

    private final Shape shape;
    private final int rarity;
    private final int completed;
    private boolean visible = true;

    public GreedTierBadgeElement(ISpatial spatial, Shape shape, int rarity, int completed) {
        super(spatial);
        this.shape = shape;
        this.rarity = rarity;
        this.completed = completed;
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
        int colour = this.rarity > MilestoneFrame.NONE ? 0xFF000000 | MilestoneFrame.colour(this.rarity) : GreedPixelArt.NAVY_HIGHLIGHT;
        int faceHeight;
        switch (this.shape) {
            case SHIELD -> {
                faceHeight = at.height() - 4;
                GreedPixelArt.fill(renderer, poseStack, x, y, z, w, faceHeight, GreedPixelArt.NAVY_OUTLINE);
                GreedPixelArt.fill(renderer, poseStack, x + 1, y + 1, z, w - 2, faceHeight - 2,
                        GreedPixelArt.mix(GreedPixelArt.NAVY_ROW, colour, 0.18F));
                GreedPixelArt.hollow(renderer, poseStack, x + 1, y + 1, z, w - 2, faceHeight - 2, colour);
                for (int k = 0; k < 4; k++) {
                    GreedPixelArt.fill(renderer, poseStack, x + 4 + k, y + faceHeight + k, z, w - 8 - 2 * k, 1,
                            k == 3 ? GreedPixelArt.NAVY_OUTLINE : colour);
                }
            }
            case SQUARE -> {
                faceHeight = at.height();
                GreedPixelArt.bevel(renderer, poseStack, x, y, z, w, faceHeight, GreedPixelArt.mix(GreedPixelArt.NAVY_ROW, colour, 0.15F),
                        colour, GreedPixelArt.scale(colour, 0.4F), GreedPixelArt.NAVY_OUTLINE);
            }
            default -> {
                faceHeight = at.height();
                GreedPixelArt.fill(renderer, poseStack, x, y, z, w, faceHeight, GreedPixelArt.mix(GreedPixelArt.NAVY_ROW, colour, 0.2F));
                GreedPixelArt.hollow(renderer, poseStack, x, y, z, w, faceHeight, colour);
            }
        }
        Component numeral = GreedTheme.text(this.completed > 0 ? GreedTheme.roman(this.completed) : "-", 0xFFFFFFFF);
        int scale = this.shape != Shape.TAG && GreedPixelArt.width(numeral, 2) <= w - 6 ? 2 : 1;
        int width = GreedPixelArt.width(numeral, scale);
        GreedPixelArt.text(poseStack, numeral, x + (w - width) / 2.0F, y + (faceHeight - 8 * scale) / 2.0F + 0.5F, z + 1,
                this.completed > 0 ? 0xFFFFFFFF : GreedPixelArt.NAVY_DIM, scale);
    }
}
