package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

/**
 * Circular rank emblem: a gold ring around a dark grey disc carrying the rank's short badge. No
 * rank artwork exists yet, so the medallion is drawn as horizontal spans of a rasterised circle;
 * swapping in a texture later only means replacing {@link #render}.
 */
public class RankMedallionElement extends AbstractSpatialElement<RankMedallionElement> implements IRenderedElement {
    private final IntSupplier rank;
    private boolean visible = true;

    public RankMedallionElement(ISpatial spatial, IntSupplier rank) {
        super(spatial);
        this.rank = rank;
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
        int diameter = Math.min(world.width(), world.height());
        int radius = diameter / 2;
        int centerX = world.x() + world.width() / 2;
        int centerY = world.y() + world.height() / 2;
        drawDisc(renderer, poseStack, centerX, centerY, radius, world.z(), GreedTheme.GOLD);
        drawDisc(renderer, poseStack, centerX, centerY, radius - 2, world.z() + 1, GreedTheme.GOLD_DEEP);
        drawDisc(renderer, poseStack, centerX, centerY, radius - 3, world.z() + 2, GreedTheme.PLATE_DARK);
        Component glyph = new TextComponent(GreedTheme.rankMedallionGlyph(this.rank.getAsInt()));
        Minecraft minecraft = Minecraft.getInstance();
        int textWidth = minecraft.font.width(glyph);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, world.z() + 3.0D);
        minecraft.font.drawShadow(poseStack, glyph, centerX - textWidth / 2.0F, centerY - 4.0F, GreedTheme.GOLD);
        poseStack.popPose();
    }

    private static void drawDisc(IElementRenderer renderer, PoseStack poseStack, int centerX, int centerY, int radius, int z, int color) {
        if (radius <= 0) {
            return;
        }
        for (int dy = -radius; dy <= radius; dy++) {
            int half = (int) Math.floor(Math.sqrt((double) radius * radius - (double) dy * dy));
            if (half <= 0) {
                continue;
            }
            renderer.renderColoredQuad(poseStack, color,
                    Spatials.positionXYZ(centerX - half, centerY + dy, z).size(half * 2, 1));
        }
    }
}
