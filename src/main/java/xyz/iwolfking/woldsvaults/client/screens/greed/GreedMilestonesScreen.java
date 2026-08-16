package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.screen.player.AbstractSkillTabElementContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import xyz.iwolfking.woldsvaults.milestones.container.GreedMilestonesContainer;

/**
 * The greed tab of the player menu. It sits in the slot the retired greed tree used to hold and
 * carries the same six sub-tabs Mr. Greedy shows, but with claiming disabled: the server only
 * pays out reputation while the greed trader container is open.
 *
 * <p>It extends {@link AbstractSkillTabElementContainerScreen} rather than framing itself, which
 * is what makes it size and behave like the statistics, talent and prestige tabs: the base class
 * places the shared tab strip and the tab title against {@code getTabContentSpatial()}, that
 * rectangle is the near-full-window inset every other tab occupies, and the_vault's JEI plugin
 * registers a {@code RemoveJEIContainerHandler} against this very class, so JEI stops drawing
 * beside the window. The panel is then stretched to fill that same rectangle.</p>
 */
public class GreedMilestonesScreen extends AbstractSkillTabElementContainerScreen<GreedMilestonesContainer> {
    public static final int TAB_INDEX = 6;

    private final GreedPanelElement panel;

    public GreedMilestonesScreen(GreedMilestonesContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, ScreenRenderers.getImmediate());
        this.addElement(new NineSliceElement<>(Spatials.positionXYZ(0, 0, -10), ScreenTextures.DEFAULT_WINDOW_BACKGROUND)
                .layout((screen, gui, parent, world) -> {
                    ISpatial content = this.getTabContentSpatial();
                    world.positionXY(content.x(), content.y()).size(content.width(), content.height());
                }));
        this.panel = this.addElement(new GreedPanelElement(GreedPanelElement.WIDTH, GreedPanelElement.TRADER_HEIGHT, false, false)
                .layout((screen, gui, parent, world) -> {
                    ISpatial content = this.getTabContentSpatial();
                    world.positionXY(content.x(), content.y());
                }));
    }

    @Override
    public int getTabIndex() {
        return TAB_INDEX;
    }

    @Override
    public MutableComponent getTabTitle() {
        return new TranslatableComponent(GreedTheme.LANG_ROOT + "title");
    }

    /**
     * Re-sizes the panel to the tab content rectangle. Minecraft re-runs {@code init} on every
     * window resize and gui scale change, which is the only point at which the screen's own width
     * and height are known - they are still zero while the constructor runs.
     */
    @Override
    protected void init() {
        super.init();
        ISpatial content = this.getTabContentSpatial();
        this.panel.applyBounds(content.width(), content.height());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.panel.refreshIfChanged();
    }
}
