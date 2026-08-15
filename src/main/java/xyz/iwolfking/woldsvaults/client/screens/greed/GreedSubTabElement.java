package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;

import java.util.function.Consumer;

/**
 * One entry of the greed sub-tab strip: a dark grey plate that turns gold-bordered with gold text
 * while its tab is the selected one.
 */
public class GreedSubTabElement extends ContainerElement<GreedSubTabElement> {
    private final GreedTab tab;
    private final Consumer<GreedTab> onSelect;
    private final boolean selected;

    public GreedSubTabElement(ISpatial spatial, GreedTab tab, boolean selected, Consumer<GreedTab> onSelect) {
        super(spatial);
        this.tab = tab;
        this.selected = selected;
        this.onSelect = onSelect;
        int fill = selected ? GreedTheme.GOLD_DEEP : GreedTheme.PLATE;
        int border = selected ? GreedTheme.GOLD : GreedTheme.BORDER;
        this.addElement(new GreedFillElement(Spatials.positionXYZ(0, 0, 0).size(spatial.width(), spatial.height()), fill, border));
        this.addElement(new LabelElement<>(
                Spatials.positionXYZ(0, (spatial.height() - 8) / 2, 1),
                (ISize) Spatials.size(spatial.width(), 9),
                tab.getTitle().copy().setStyle(Style.EMPTY.withColor(selected ? GreedTheme.GOLD : GreedTheme.TEXT)),
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
}
