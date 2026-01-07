package xyz.iwolfking.woldsvaults.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.TextComponent;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class RGBSlider extends AbstractWidget {

    private final IntSupplier getter;
    private final IntConsumer setter;
    private final int channel; // 0=R, 1=G, 2=B

    private boolean dragging = false;

    public RGBSlider(int x, int y, int width, int channel, IntSupplier getter, IntConsumer setter) {
        super(x, y, width, 20, new TextComponent(""));
        this.getter = getter;
        this.setter = setter;
        this.channel = channel;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        // Draw gradient background for this channel
        for (int i = 0; i < width; i++) {
            int value = (int) ((i / (float) width) * 255);
            int color = 0xFF000000;
            switch (channel) {
                case 0 -> color |= (value << 16); // Red
                case 1 -> color |= (value << 8);  // Green
                case 2 -> color |= value;         // Blue
            }
            fill(stack, x + i, y + 6, x + i + 1, y + 14, color);
        }

        // Draw slider track border
        fill(stack, x - 1, y + 5, x + width + 1, y + 15, 0xFFAAAAAA);

        // Draw handle
        int value = getter.getAsInt();
        int handleX = x + (int) ((value / 255f) * width);
        int handleY = y + 6;
        int handleSize = isMouseOver(mouseX, mouseY) ? 6 : 4; // hover effect
        fill(stack, handleX - handleSize / 2, handleY - 2, handleX + handleSize / 2, handleY + 10, 0xFFFFFFFF);
        fill(stack, handleX - handleSize / 2 + 1, handleY - 1, handleX + handleSize / 2 - 1, handleY + 9, 0xFF000000); // inner shadow
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            updateValue(mouseX);
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return true;
    }

    public void setValue(int value) {
        setter.accept(value);
    }


    private void updateValue(double mouseX) {
        double ratio = (mouseX - x) / (double) width;
        ratio = Math.max(0, Math.min(1, ratio));
        int value = (int) (ratio * 255);
        setter.accept(value);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
