package xyz.iwolfking.woldsvaults.blocks.containers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.blocks.tiles.ConfigurableFloatingTextTileEntity;
import xyz.iwolfking.woldsvaults.client.screens.widgets.HexColorField;
import xyz.iwolfking.woldsvaults.client.screens.widgets.RGBSlider;

public class FloatingTextColorPickerScreen extends Screen {

    private final Screen parent;
    private final ConfigurableFloatingTextTileEntity.TextLine line;

    private RGBSlider rSlider, gSlider, bSlider;
    private HexColorField hexField;
    private boolean updating = false;

    public FloatingTextColorPickerScreen(Screen parent, ConfigurableFloatingTextTileEntity.TextLine line) {
        super(new TextComponent("Text Color"));
        this.parent = parent;
        this.line = line;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;

        int sliderWidth = 150;
        int sliderHeight = 20;
        int spacing = 25;

        Font font = this.font;


        rSlider = addRenderableWidget(new RGBSlider(
                cx - sliderWidth / 2, cy - spacing * 2, sliderWidth,
                0, // channel 0 = Red
                () -> (line.color >> 16) & 0xFF,
                r -> updateColorFromSliders(r, -1, -1)
        ));


        gSlider = addRenderableWidget(new RGBSlider(
                cx - sliderWidth / 2, cy - spacing, sliderWidth,
                1, // channel 1 = Green
                () -> (line.color >> 8) & 0xFF,
                g -> updateColorFromSliders(-1, g, -1)
        ));


        bSlider = addRenderableWidget(new RGBSlider(
                cx - sliderWidth / 2, cy, sliderWidth,
                2, // channel 2 = Blue
                () -> line.color & 0xFF,
                b -> updateColorFromSliders(-1, -1, b)
        ));



        hexField = addRenderableWidget(new HexColorField(
                font,
                cx - 50,
                cy + spacing + 5,
                100,
                rgb -> {
                    if (updating) return;
                    updating = true;

                    line.color = rgb;
                    rSlider.setValue((rgb >> 16) & 0xFF);
                    gSlider.setValue((rgb >> 8) & 0xFF);
                    bSlider.setValue(rgb & 0xFF);

                    updating = false;
                }
        ));
        hexField.setValue(String.format("#%06X", line.color));
    }

    private void updateColorFromSliders(int r, int g, int b) {
        if (updating) return;
        updating = true;

        int red = r >= 0 ? r : (line.color >> 16) & 0xFF;
        int green = g >= 0 ? g : (line.color >> 8) & 0xFF;
        int blue = b >= 0 ? b : line.color & 0xFF;

        line.color = (red << 16) | (green << 8) | blue;

        // Update hex field
        hexField.setValue(String.format("#%06X", line.color));

        updating = false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        int cx = width / 2;
        int cy = height / 2;

        // Draw slider labels
        font.draw(poseStack, "R", cx - 120, cy - 50, 0xFFFF0000);
        font.draw(poseStack, "G", cx - 120, cy - 25, 0xFF00FF00);
        font.draw(poseStack, "B", cx - 120, cy, 0xFF0000FF);

        // Color preview box
        int boxSize = 40;
        int boxX = cx + 80;
        int boxY = cy - 30;
        fill(poseStack, boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFF000000 | line.color);

        font.draw(poseStack, "Preview", boxX, boxY - 10, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
