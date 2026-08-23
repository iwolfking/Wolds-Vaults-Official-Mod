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
 * The greed tab of the player menu, carrying the same six sub-tabs Mr. Greedy shows but with claiming
 * disabled: the server only pays out reputation while the greed trader container is open.
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
