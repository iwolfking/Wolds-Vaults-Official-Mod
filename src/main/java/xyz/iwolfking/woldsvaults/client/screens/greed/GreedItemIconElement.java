package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A milestone's icon: its item drawn at double size in a recessed navy slot, with an optional gold tag in the bottom
 * right corner (like a stack count) for rep that is waiting to be claimed elsewhere.
 */
public class GreedItemIconElement extends AbstractSpatialElement<GreedItemIconElement> implements IRenderedElement {
    private static final int SCALE = 2;

    private final ItemStack stack;
    private final Component tag;
    private boolean visible = true;

    public GreedItemIconElement(ISpatial spatial, ItemStack stack, Component tag) {
        super(spatial);
        this.stack = stack;
        this.tag = tag;
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
        GreedPixelArt.bevel(renderer, poseStack, x, y, z, w, h, 0xFF0E121B, 0xFF080A10, GreedPixelArt.NAVY_LIGHT,
                GreedPixelArt.NAVY_OUTLINE);

        int size = 16 * SCALE;
        int iconX = x + (w - size) / 2;
        int iconY = y + (h - size) / 2;
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(poseStack.last().pose());
        modelView.translate(iconX, iconY, z);
        modelView.scale(SCALE, SCALE, 1.0F);
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(this.stack, 0, 0);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();

        if (this.tag != null) {
            int tagWidth = GreedPixelArt.width(this.tag, 1);
            GreedPixelArt.text(poseStack, this.tag, x + w - 3 - tagWidth, y + h - 11, z + 300, GreedTheme.GOLD, 1);
        }
    }
}
