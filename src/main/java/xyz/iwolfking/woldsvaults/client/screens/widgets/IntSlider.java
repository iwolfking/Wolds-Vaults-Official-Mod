package xyz.iwolfking.woldsvaults.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntSlider extends AbstractWidget {

    private final int minValue;
    private final int maxValue;
    private final IntSupplier getter;
    private final IntConsumer setter;

    private boolean dragging = false;

    public IntSlider(int x, int y, int width, int minValue, int maxValue, IntSupplier getter, IntConsumer setter) {
        super(x, y, width, 20, new TextComponent(""));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        fill(stack, x - 1, y + 5, x + width + 1, y + 15, 0xFFAAAAAA);
        fill(stack, x, y + 6, x + width, y + 14, 0xFF222222);

        float ratio = (getter.getAsInt() - minValue) / (float) (maxValue - minValue);
        ratio = Math.max(0, Math.min(1, ratio));
        int handleX = x + (int) (ratio * width);
        int handleSize = isMouseOver(mouseX, mouseY) || dragging ? 6 : 4;
        fill(stack, handleX - handleSize / 2, y + 4, handleX + handleSize / 2, y + 16, 0xFFFFFFFF);
        fill(stack, handleX - handleSize / 2 + 1, y + 5, handleX + handleSize / 2 - 1, y + 15, 0xFF000000);
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

    private void updateValue(double mouseX) {
        double ratio = (mouseX - x) / (double) width;
        ratio = Math.max(0, Math.min(1, ratio));
        setter.accept(minValue + (int) Math.round(ratio * (maxValue - minValue)));
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
