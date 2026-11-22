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
 * A greed screen button. {@link Look#FLAT} is the original flat greed-palette button with a gold highlight state marking
 * a claimable row; {@link Look#NAVY} is a navy Minecraft-style bevel that turns gold while highlighted and dark while
 * disabled; {@link Look#BARE} is just its icon, gold while highlighted and greyed otherwise. Besides its label it can
 * carry a pixel icon (above the label, or beside it), a smaller second line, and a label drawn at double size.
 */
public class GreedButtonElement extends AbstractSpatialElement<GreedButtonElement> implements IRenderedElement, IGuiEventElement {
    public enum Look { FLAT, NAVY, BARE }

    protected final Supplier<Component> label;
    private final Runnable onClick;
    private BooleanSupplier disabled = () -> false;
    private BooleanSupplier highlighted = () -> false;
    private boolean visible = true;
    private Look look = Look.FLAT;
    private Component subLabel;
    private int labelScale = 1;
    private GreedPixelArt.Sprite icon;
    private boolean iconBeside;
    private boolean dimIconWhenOff;

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

    public GreedButtonElement look(Look look) {
        this.look = look;
        return this;
    }

    public GreedButtonElement subLabel(Component subLabel) {
        this.subLabel = subLabel;
        return this;
    }

    public GreedButtonElement labelScale(int labelScale) {
        this.labelScale = labelScale;
        return this;
    }

    /** A pixel icon above the label, or beside it; {@code dimWhenOff} darkens it while the button is disabled. */
    public GreedButtonElement icon(GreedPixelArt.Sprite icon, boolean beside, boolean dimWhenOff) {
        this.icon = icon;
        this.iconBeside = beside;
        this.dimIconWhenOff = dimWhenOff;
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
        int textColor;
        int subColor;
        switch (this.look) {
            case NAVY -> {
                int x = world.x();
                int y = world.y();
                int z = world.z();
                if (off) {
                    GreedPixelArt.bevel(renderer, poseStack, x, y, z, world.width(), world.height(),
                            0xFF1A1F2A, 0xFF252C3B, 0xFF0C0F15, GreedPixelArt.NAVY_OUTLINE);
                    textColor = GreedPixelArt.NAVY_OFF_TEXT;
                    subColor = GreedPixelArt.NAVY_OFF_TEXT;
                } else if (gold) {
                    GreedPixelArt.bevel(renderer, poseStack, x, y, z, world.width(), world.height(),
                            hover ? 0xFF9E7A2A : 0xFF8A6A22, hover ? 0xFFFFD677 : GreedTheme.GOLD, 0xFF4A3608, 0xFF1A1205);
                    textColor = GreedPixelArt.CREAM;
                    subColor = GreedPixelArt.PALE;
                } else {
                    GreedPixelArt.bevel(renderer, poseStack, x, y, z, world.width(), world.height(),
                            hover ? 0xFF2C3549 : GreedPixelArt.NAVY_FACE, hover ? 0xFF4D5C7A : GreedPixelArt.NAVY_HIGHLIGHT,
                            GreedPixelArt.NAVY_DARK, GreedPixelArt.NAVY_OUTLINE);
                    textColor = hover ? 0xFFE6ECF7 : GreedPixelArt.NAVY_TEXT;
                    subColor = GreedPixelArt.NAVY_DIM;
                }
            }
            case BARE -> {
                textColor = gold ? GreedTheme.GOLD : GreedPixelArt.NAVY_DIM;
                subColor = textColor;
            }
            default -> {
                int fill;
                int border;
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
                subColor = textColor;
            }
        }
        this.renderContent(renderer, poseStack, world, off, gold, hover, textColor, subColor);
    }

    private void renderContent(IElementRenderer renderer, PoseStack poseStack, ISpatial world, boolean off, boolean gold,
                               boolean hover, int textColor, int subColor) {
        Component component = this.label.get();
        int scale = this.labelScale;
        int labelWidth = component == null ? 0 : GreedPixelArt.width(component, scale);
        int labelHeight = component == null ? 0 : 8 * scale;
        int iconWidth = this.icon == null ? 0 : this.icon.width();
        int iconHeight = this.icon == null ? 0 : this.icon.height();
        int z = world.z() + 1;
        int centreX = world.x() + world.width() / 2;
        int iconX;
        int iconY;
        int labelX;
        int labelY;
        int bottom;
        if (this.iconBeside) {
            int rowWidth = iconWidth + (component == null ? 0 : 2 + labelWidth);
            int rowHeight = Math.max(iconHeight, labelHeight);
            int total = rowHeight + (this.subLabel == null ? 0 : 3 + 8);
            int top = world.y() + (world.height() - total + 1) / 2;
            iconX = centreX - rowWidth / 2;
            iconY = top + (rowHeight - iconHeight) / 2;
            labelX = iconX + iconWidth + 2;
            labelY = top + (rowHeight - labelHeight) / 2;
            bottom = top + rowHeight;
        } else {
            int gap = this.icon != null && component != null ? 2 : 0;
            int total = iconHeight + gap + labelHeight + (this.subLabel == null ? 0 : 3 + 8);
            int top = world.y() + (world.height() - total + 1) / 2;
            iconX = centreX - iconWidth / 2;
            iconY = top;
            labelX = centreX - labelWidth / 2;
            labelY = top + iconHeight + gap;
            bottom = labelY + labelHeight;
        }
        if (this.icon != null) {
            GreedPixelArt.sprite(renderer, poseStack, this.icon, iconX, iconY, z, colour -> {
                int c = colour;
                if (this.look == Look.BARE && !gold) {
                    c = GreedPixelArt.greyed(c);
                } else if (off && this.dimIconWhenOff) {
                    c = GreedPixelArt.scale(c, 0.35F);
                }
                return hover ? GreedPixelArt.mix(c, 0xFFFFFFFF, 0.25F) : c;
            });
        }
        if (component != null) {
            GreedPixelArt.text(poseStack, component, labelX, labelY, z + 1, textColor, scale);
        }
        if (this.subLabel != null) {
            int subWidth = GreedPixelArt.width(this.subLabel, 1);
            GreedPixelArt.text(poseStack, this.subLabel, centreX - subWidth / 2.0F, bottom + 3, z + 1, subColor, 1);
        }
    }
}
