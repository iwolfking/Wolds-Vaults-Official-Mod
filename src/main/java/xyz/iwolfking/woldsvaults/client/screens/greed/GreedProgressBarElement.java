package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Dark grey track with a gold fill and optional centred caption. Used for both the reputation bar
 * on the main screen and the per-tier bar on every achievement row; the base framework's progress
 * bar is texture sized, which cannot stretch to an arbitrary panel width.
 */
public class GreedProgressBarElement extends AbstractSpatialElement<GreedProgressBarElement> implements IRenderedElement {
    private final Supplier<Float> progress;
    private final Supplier<Component> caption;
    private final int fillColor;
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

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ISpatial world = this.getWorldSpatial();
        renderer.renderColoredQuad(poseStack, GreedTheme.PLATE_DARK, world);
        float fraction = Math.max(0.0F, Math.min(1.0F, this.progress.get()));
        int filled = Math.round((world.width() - 2) * fraction);
        if (filled > 0) {
            renderer.renderColoredQuad(poseStack, this.fillColor,
                    Spatials.positionXYZ(world.x() + 1, world.y() + 1, world.z() + 1).size(filled, world.height() - 2));
        }
        renderer.renderColoredHollowRect(poseStack, GreedTheme.GOLD_DEEP, world);
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
        int textY = world.y() + (world.height() - 8) / 2;
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, world.z() + 2.0D);
        minecraft.font.drawShadow(poseStack, component, textX, textY, GreedTheme.TEXT_TITLE);
        poseStack.popPose();
    }
}
