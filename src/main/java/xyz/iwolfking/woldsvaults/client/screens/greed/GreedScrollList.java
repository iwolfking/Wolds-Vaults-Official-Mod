package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;

/**
 * Slim-scrollbar vertical list, mirroring the private helper the base greed trader screen uses.
 * The base one is package-private to that screen, so the greed rework carries its own with the
 * child-adding methods raised to public.
 */
public class GreedScrollList extends VerticalScrollClipContainer<GreedScrollList> {
    public GreedScrollList(ISpatial spatial) {
        super(spatial, Padding.of(0, -1, 1, 1), null, ScreenTextures.SCROLLBAR_HANDLE_SLIM,
                ScreenTextures.SCROLLBAR_HANDLE_SLIM_DISABLED, ScreenTextures.INSET_GREY_BACKGROUND);
    }

    @Override
    public <T extends IElement> T addElement(T element) {
        return super.addElement(element);
    }

    public float getScrollValue() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScrollValue(float value) {
        this.verticalScrollBarElement.setValue(value);
    }

    @Override
    public void removeAllElements() {
        this.innerContainerElement.getElementStore().removeAllElements();
    }
}
