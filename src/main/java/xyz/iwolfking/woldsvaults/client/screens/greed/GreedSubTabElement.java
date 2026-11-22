package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * One entry of the greed sub-tab strip, a dark navy folder tab in a Minecraft-style bevel. Unselected tabs sit a pixel
 * low as raised buttons, lighter while hovered; the selected one stands two pixels taller, takes gold text and opens
 * through the top edge of the {@link GreedFolderElement} under the strip, in its colour, so the two read as one folder.
 * The panel draws the selected tab above the folder for that.
 */
public class GreedSubTabElement extends ContainerElement<GreedSubTabElement> {
    private static final int FACE = 0xFF232A3A;
    private static final int FACE_HOVER = 0xFF2C3549;
    private static final int LIGHT_HOVER = 0xFF4D5C7A;
    private static final int TEXT = 0xFFB8C3D9;

    private final GreedTab tab;
    private final Consumer<GreedTab> onSelect;
    private final boolean selected;

    /** The folder's body has to start right under the strip, so the selected tab can open into it. */
    public GreedSubTabElement(ISpatial spatial, GreedTab tab, boolean selected, Consumer<GreedTab> onSelect) {
        super(spatial);
        this.tab = tab;
        this.selected = selected;
        this.onSelect = onSelect;
        this.addElement(new Face(Spatials.positionXYZ(0, 0, 0).size(spatial.width(), spatial.height()), selected));
        int labelY = (spatial.height() - 8) / 2 + (selected ? -1 : 1);
        this.addElement(new LabelElement<>(
                Spatials.positionXYZ(0, labelY, 1),
                (ISize) Spatials.size(spatial.width(), 9),
                tab.getTitle().copy().setStyle(Style.EMPTY.withColor(selected ? GreedTheme.GOLD : TEXT)),
                LabelTextStyle.defaultStyle().center()));
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int buttonIndex) {
        if (!this.selected) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.onSelect.accept(this.tab);
        }
        return true;
    }

    private static final class Face extends AbstractSpatialElement<Face> implements IRenderedElement {
        private final boolean selected;
        private boolean visible = true;

        Face(ISpatial spatial, boolean selected) {
            super(spatial);
            this.selected = selected;
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
            if (this.selected) {
                // two pixels taller, and its face runs on over the folder's top outline and highlight to open into it
                fill(renderer, poseStack, x, y - 2, z, w, h + 3, GreedFolderElement.OUTLINE);
                fill(renderer, poseStack, x + 1, y - 1, z, w - 2, h + 3, GreedFolderElement.FACE);
                fill(renderer, poseStack, x + 1, y - 1, z, w - 2, 1, GreedFolderElement.LIGHT);
                fill(renderer, poseStack, x + 1, y - 1, z, 1, h + 3, GreedFolderElement.SIDE);
                fill(renderer, poseStack, x + w - 2, y, z, 1, h, GreedFolderElement.DARK);
                return;
            }
            // a raised button a pixel lower than the strip's top: light top-left edge, dark bottom-right edge
            boolean hover = at.contains(mouseX, mouseY);
            int top = y + 1;
            int height = h - 1;
            fill(renderer, poseStack, x, top, z, w, height, GreedFolderElement.OUTLINE);
            fill(renderer, poseStack, x + 1, top + 1, z, w - 2, height - 2, hover ? FACE_HOVER : FACE);
            int light = hover ? LIGHT_HOVER : GreedFolderElement.LIGHT;
            fill(renderer, poseStack, x + 1, top + 1, z, w - 2, 1, light);
            fill(renderer, poseStack, x + 1, top + 1, z, 1, height - 2, light);
            fill(renderer, poseStack, x + 1, top + height - 2, z, w - 2, 1, GreedFolderElement.DARK);
            fill(renderer, poseStack, x + w - 2, top + 1, z, 1, height - 2, GreedFolderElement.DARK);
        }

        private static void fill(IElementRenderer renderer, PoseStack poseStack, int x, int y, int z, int w, int h, int colour) {
            GreedFolderElement.fill(renderer, poseStack, x, y, z, w, h, colour);
        }
    }
}
