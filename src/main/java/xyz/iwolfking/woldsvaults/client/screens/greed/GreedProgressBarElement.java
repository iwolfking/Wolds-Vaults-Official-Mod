package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/** Progress bar of arbitrary width: recessed track, gold fill and an optional centred caption. */
public class GreedProgressBarElement extends AbstractSpatialElement<GreedProgressBarElement> implements IRenderedElement {
    private final Supplier<Float> progress;
    private final Supplier<Component> caption;
    private final int fillColor;
    private boolean molten;
    private boolean navy;
    private boolean visible = true;

    public GreedProgressBarElement(ISpatial spatial, Supplier<Float> progress, Supplier<Component> caption, int fillColor) {
        super(spatial);
        this.progress = progress;
        this.caption = caption;
        this.fillColor = fillColor;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    /** Fills the bar with animated molten gold (see {@link GreedFlourish}) instead of the flat fill colour. */
    public GreedProgressBarElement molten() {
        this.molten = true;
        return this;
    }

    /** Draws the track in the navy row styles' colours: a dark navy channel with a black outline. */
    public GreedProgressBarElement navy() {
        this.navy = true;
        return this;
    }

    public GreedProgressBarElement progressTooltip(long current, long target) {
        return this.tooltip(Tooltips.single(() -> GreedTheme.progress(current, target)));
    }

    public GreedProgressBarElement durationTooltip(long current, long target) {
        return this.tooltip(Tooltips.single(() -> GreedTheme.durationProgress(current, target)));
    }

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ISpatial world = this.getWorldSpatial();
        renderer.renderColoredQuad(poseStack, this.navy ? GreedPixelArt.NAVY_TRACK : GreedTheme.TRACK, world);
        int interiorWidth = world.width() - 2;
        int interiorHeight = world.height() - 2;
        if (interiorWidth > 0 && interiorHeight > 0) {
            if (!this.navy) {
                renderer.renderColoredQuad(poseStack, GreedTheme.TRACK_SHADOW,
                        Spatials.positionXYZ(world.x() + 1, world.y() + 1, world.z() + 1).size(interiorWidth, 1));
                renderer.renderColoredQuad(poseStack, GreedTheme.TRACK_SHADOW,
                        Spatials.positionXYZ(world.x() + 1, world.y() + 1, world.z() + 1).size(1, interiorHeight));
            }
            float fraction = Math.max(0.0F, Math.min(1.0F, this.progress.get()));
            int filled = fraction <= 0.0F ? 0 : Math.max(1, Math.round(interiorWidth * fraction));
            if (filled > 0 && this.molten) {
                GreedFlourish.drawMolten(poseStack, world.x() + 1, world.y() + 1, world.z() + 2, filled, interiorWidth, interiorHeight);
            } else if (filled > 0) {
                renderer.renderColoredQuad(poseStack, this.fillColor,
                        Spatials.positionXYZ(world.x() + 1, world.y() + 1, world.z() + 2).size(filled, interiorHeight));
            }
        }
        renderer.renderColoredHollowRect(poseStack, this.navy ? GreedPixelArt.NAVY_OUTLINE : GreedTheme.GOLD_DEEP, world);
        if (this.caption == null) {
            return;
        }
        Component component = this.caption.get();
        if (component == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int textWidth = minecraft.font.width(component);
        int textX = world.x() + (world.width() - textWidth) / 2;
        int textY = world.y() + 1 + (world.height() - 9) / 2;
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, world.z() + 3.0D);
        minecraft.font.drawShadow(poseStack, component, textX, textY, GreedTheme.TEXT_TITLE);
        poseStack.popPose();
    }
}
