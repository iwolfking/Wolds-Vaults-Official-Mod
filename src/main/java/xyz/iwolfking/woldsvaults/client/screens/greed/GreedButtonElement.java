package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IGuiEventElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Flat greed-palette button. Base's nine-slice buttons only come in the stock colour sets, and the
 * greed screens need the gold-on-dark-grey highlight state that marks a claimable row.
 */
public class GreedButtonElement extends AbstractSpatialElement<GreedButtonElement> implements IRenderedElement, IGuiEventElement {
    private final Supplier<Component> label;
    private final Runnable onClick;
    private BooleanSupplier disabled = () -> false;
    private BooleanSupplier highlighted = () -> false;
    private boolean visible = true;

    public GreedButtonElement(ISpatial spatial, Supplier<Component> label, Runnable onClick) {
        super(spatial);
        this.label = label;
        this.onClick = onClick;
    }

    public GreedButtonElement setDisabled(BooleanSupplier disabled) {
        this.disabled = disabled;
        return this;
    }

    public GreedButtonElement setHighlighted(BooleanSupplier highlighted) {
        this.highlighted = highlighted;
        return this;
    }

    public boolean isDisabled() {
        return this.disabled.getAsBoolean();
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
    public boolean onMouseClicked(double mouseX, double mouseY, int buttonIndex) {
        if (this.isDisabled()) {
            return true;
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.onClick.run();
        return true;
    }

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ISpatial world = this.getWorldSpatial();
        boolean off = this.isDisabled();
        boolean gold = !off && this.highlighted.getAsBoolean();
        boolean hover = !off && world.contains(mouseX, mouseY);
        int fill;
        int border;
        int textColor;
        if (off) {
            fill = GreedTheme.PLATE_DARK;
            border = GreedTheme.BORDER;
            textColor = GreedTheme.TEXT_DIM;
        } else if (gold) {
            fill = hover ? GreedTheme.GOLD_DIM : GreedTheme.GOLD_DEEP;
            border = GreedTheme.GOLD;
            textColor = GreedTheme.GOLD;
        } else {
            fill = hover ? GreedTheme.PLATE_LIGHT : GreedTheme.PLATE;
            border = GreedTheme.GOLD_DEEP;
            textColor = GreedTheme.TEXT;
        }
        renderer.renderColoredQuad(poseStack, fill, world);
        renderer.renderColoredHollowRect(poseStack, border, world);
        Component component = this.label.get();
        if (component == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int textWidth = minecraft.font.width(component);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, world.z() + 1.0D);
        minecraft.font.drawShadow(poseStack, component,
                world.x() + (world.width() - textWidth) / 2.0F,
                world.y() + (world.height() - 8) / 2.0F, textColor);
        poseStack.popPose();
    }
}
