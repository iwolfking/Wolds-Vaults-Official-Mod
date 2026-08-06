package xyz.iwolfking.woldsvaults.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.screens.widgets.IntSlider;
import xyz.iwolfking.woldsvaults.effect.trinkets.SpeedLimitTrinketEffect;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundSetTrinketSpeedCapPacket;

public class SpeedCapConfigScreen extends Screen {
    private static final int SLIDER_MIN = 50;
    private static final int SLIDER_MAX = 500;

    private final Screen parent;
    private final boolean creativeInventory;
    private final int slotIndex;
    private final int initialCapPercent;
    private int capPercent;

    private EditBox valueBox;
    private boolean updating = false;
    private boolean sent = false;

    public SpeedCapConfigScreen(Screen parent, boolean creativeInventory, int slotIndex, int capPercent) {
        super(new TextComponent("Configure Speed"));
        this.parent = parent;
        this.creativeInventory = creativeInventory;
        this.slotIndex = slotIndex;
        this.initialCapPercent = capPercent;
        this.capPercent = capPercent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;

        valueBox = addRenderableWidget(new EditBox(font, cx - 40, cy - 35, 80, 18, new TextComponent("")));
        valueBox.setMaxLength(6);
        valueBox.setFilter(text -> text.isEmpty() || text.matches("\\d+"));
        valueBox.setResponder(this::onTextChanged);
        valueBox.setValue(String.valueOf(capPercent));

        addRenderableWidget(new IntSlider(cx - 100, cy - 5, 200, SLIDER_MIN, SLIDER_MAX, () -> capPercent, this::onSliderChanged));
    }

    private void onTextChanged(String text) {
        if (updating) {
            return;
        }
        updating = true;
        capPercent = text.isEmpty() ? 0 : SpeedLimitTrinketEffect.sanitizeCapPercent(parseCap(text));
        updating = false;
    }

    private int parseCap(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            WoldsVaults.LOGGER.warn("Weighted Boots: could not parse speed cap input '{}', treating as uncapped.", text);
            return 0;
        }
    }

    private void onSliderChanged(int value) {
        if (updating) {
            return;
        }
        updating = true;
        capPercent = value;
        valueBox.setValue(String.valueOf(value));
        updating = false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, font, title, width / 2, height / 2 - 60, 0xFFFFFF);
        font.draw(poseStack, "%", width / 2 + 44, height / 2 - 30, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (!valueBox.isFocused() && minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        sendIfNeeded();
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void removed() {
        sendIfNeeded();
    }

    private void sendIfNeeded() {
        if (sent) {
            return;
        }
        sent = true;
        int sanitized = SpeedLimitTrinketEffect.sanitizeCapPercent(capPercent);
        if (sanitized != initialCapPercent) {
            ModNetwork.sendToServer(new ServerboundSetTrinketSpeedCapPacket(sanitized, creativeInventory, slotIndex));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
