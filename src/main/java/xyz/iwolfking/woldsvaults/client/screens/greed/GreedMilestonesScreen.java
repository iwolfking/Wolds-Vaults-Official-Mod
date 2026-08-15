package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.render.ScreenTooltipRenderer;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.screen.player.element.SkillTabContainerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xyz.iwolfking.woldsvaults.milestones.container.GreedMilestonesContainer;

/**
 * The greed tab of the player menu. It sits in the slot the retired greed tree used to hold and
 * carries the same six sub-tabs Mr. Greedy shows, but with claiming disabled: the server only
 * pays out reputation while the greed trader container is open.
 */
public class GreedMilestonesScreen extends AbstractElementContainerScreen<GreedMilestonesContainer> {
    public static final int TAB_INDEX = 6;

    private static final int PAD_LEFT = 21;
    private static final int PAD_TOP = 42;
    private static final int PAD_BOTTOM = 30;

    private final GreedPanelElement panel;

    public GreedMilestonesScreen(GreedMilestonesContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, ScreenRenderers.getImmediate(), ScreenTooltipRenderer::create);
        this.setGuiSize(Spatials.size(GreedPanelElement.WIDTH, GreedPanelElement.PLAYER_HEIGHT));
        this.panel = this.addElement(new GreedPanelElement(0, 0, GreedPanelElement.PLAYER_HEIGHT, false)
                .layout((screen, gui, parent, world) -> {
                    ISpatial content = this.getTabContentSpatial();
                    world.positionXY(content.x() + Math.max(0, (content.width() - GreedPanelElement.WIDTH) / 2),
                            content.y() + Math.max(0, (content.height() - GreedPanelElement.PLAYER_HEIGHT) / 2));
                }));
        this.addElement(new SkillTabContainerElement<>(Spatials.positionXYZ(15, -28, 1), TAB_INDEX)
                .layout((screen, gui, parent, world) -> world.translateXY(this.getTabContentSpatial())));
    }

    /**
     * Same content rectangle the base skill tabs use, so the shared tab strip lands in exactly the
     * position it holds on the statistics, talent and expertise tabs.
     */
    public ISpatial getTabContentSpatial() {
        int width = this.width - PAD_LEFT * 2;
        int height = this.height - PAD_BOTTOM - PAD_TOP;
        return Spatials.positionXY(PAD_LEFT, PAD_TOP).size(width, height);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.panel.refreshIfChanged();
    }
}
