package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * The Take Trial button: a gold-rimmed plate that warms to a red heat towards the bottom with embers drifting up out of
 * it (both from {@link GreedFlourish}), and a crossed-swords badge sitting on its top edge. Brighter while hovered.
 */
public class GreedTrialButtonElement extends GreedButtonElement {
    private static final int INK = 0xFF2A1C04;
    private static final int PALE = 0xFFFFE9A8;
    private static final int CREAM = 0xFFFFF4D0;
    private static final String[] SWORDS = {
            "G.....G",
            ".g...g.",
            "..g.g..",
            "...g...",
            "..g.g..",
            ".d...d.",
            "d.....d"};

    public GreedTrialButtonElement(ISpatial spatial, Supplier<Component> label, Runnable onClick) {
        super(spatial, label, onClick);
    }

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ISpatial at = this.getWorldSpatial();
        int x = at.x();
        int y = at.y();
        int z = at.z();
        int w = at.width();
        int h = at.height();
        boolean hover = at.contains(mouseX, mouseY);
        Minecraft minecraft = Minecraft.getInstance();
        Component component = this.label.get();
        int textWidth = component == null ? 0 : minecraft.font.width(component);
        int textX = (w - textWidth) / 2;
        int textY = (h - 8) / 2;
        GreedFlourish.drawTrial(poseStack, x, y, z, w, h, hover, textX - 2, textY - 2, textX + textWidth + 2, textY + 9);
        drawBadge(renderer, poseStack, x + (w - 11) / 2, y - 4, z + 1, hover);
        if (component == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, z + 2.0D);
        minecraft.font.drawShadow(poseStack, component, x + textX, y + textY, hover ? CREAM : GreedTheme.GOLD);
        poseStack.popPose();
    }

    // an 11 x 9 dark badge with a gold rim and crossed swords, straddling the button's top edge
    private static void drawBadge(IElementRenderer renderer, PoseStack poseStack, int x, int y, int z, boolean hover) {
        fill(renderer, poseStack, x + 1, y, z, 9, 9, INK);
        fill(renderer, poseStack, x, y + 1, z, 11, 7, INK);
        fill(renderer, poseStack, x + 1, y, z, 9, 1, GreedTheme.GOLD);
        fill(renderer, poseStack, x + 1, y + 8, z, 9, 1, GreedTheme.GOLD);
        fill(renderer, poseStack, x + 1, y, z, 1, 9, GreedTheme.GOLD);
        fill(renderer, poseStack, x + 9, y, z, 1, 9, GreedTheme.GOLD);
        for (int row = 0; row < SWORDS.length; row++) {
            for (int column = 0; column < SWORDS[row].length(); column++) {
                int colour = switch (SWORDS[row].charAt(column)) {
                    case 'G' -> PALE;
                    case 'g' -> GreedTheme.GOLD;
                    case 'd' -> GreedTheme.GOLD_DIM;
                    default -> 0;
                };
                if (colour != 0) {
                    fill(renderer, poseStack, x + 2 + column, y + 1 + row, z, 1, 1, hover ? brighten(colour) : colour);
                }
            }
        }
    }

    // a third of the way to white
    private static int brighten(int argb) {
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = argb & 255;
        return 0xFF000000 | (r + (255 - r) / 3) << 16 | (g + (255 - g) / 3) << 8 | (b + (255 - b) / 3);
    }

    private static void fill(IElementRenderer renderer, PoseStack poseStack, int x, int y, int z, int w, int h, int colour) {
        renderer.renderColoredQuad(poseStack, colour, Spatials.positionXYZ(x, y, z).size(w, h));
    }
}
