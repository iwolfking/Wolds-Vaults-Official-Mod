package xyz.iwolfking.woldsvaults.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchGridScroll;

public final class PouchScrollBar extends AbstractWidget {
    private final PouchGridScroll scroll;
    private double grabOffset;

    public PouchScrollBar(int x, int y, int height, String label, PouchGridScroll scroll) {
        super(x, y, 6, height, new TextComponent(label));
        this.scroll = scroll;
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        fill(pose, x + 1, y, x + 5, y + height, 0xFF828282);
        int top = y + scroll.thumbOffset(height);
        int bottom = top + scroll.thumbHeight(height);
        fill(pose, x, top, x + width, bottom, isHoveredOrFocused() ? 0xFFF4F4F4 : 0xFFD8D8D8);
        fill(pose, x + 1, top + 1, x + width - 1, bottom - 1, 0xFFB8B8B8);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        double withinThumb = mouseY - y - scroll.thumbOffset(height);
        grabOffset = withinThumb >= 0 && withinThumb < scroll.thumbHeight(height)
                ? withinThumb : scroll.thumbHeight(height) / 2.0;
        dragTo(mouseY);
    }

    public void dragTo(double mouseY) { scroll.dragThumb(mouseY - y - grabOffset, height); }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (!active || !visible) return false;
        if (key == GLFW.GLFW_KEY_UP) scroll.scroll(-1);
        else if (key == GLFW.GLFW_KEY_DOWN) scroll.scroll(1);
        else if (key == GLFW.GLFW_KEY_HOME) scroll.setFirstRow(0);
        else if (key == GLFW.GLFW_KEY_END) scroll.setFirstRow(scroll.maxFirstRow());
        else return super.keyPressed(key, scanCode, modifiers);
        return true;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
}
