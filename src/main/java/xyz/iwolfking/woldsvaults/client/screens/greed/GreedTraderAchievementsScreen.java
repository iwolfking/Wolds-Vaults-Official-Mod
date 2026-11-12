package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.GreedTraderScreen;
import iskallia.vault.container.GreedTraderContainer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Mr. Greedy's achievements tab: the only screen where the claim buttons do anything. */
public class GreedTraderAchievementsScreen extends GreedTraderScreen {
    private GreedPanelElement panel;

    public GreedTraderAchievementsScreen(GreedTraderContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.setGuiSize(Spatials.size(GreedPanelElement.WIDTH + 10, GreedPanelElement.TRADER_HEIGHT + 10));
    }

    /** Null, so the base strip draws none of its own tabs as selected. */
    @Override
    protected Tab getTab() {
        return null;
    }

    @Override
    protected String getTabTitle() {
        return I18n.get(GreedTheme.LANG_ROOT + "title");
    }

    @Override
    protected void buildContent() {
        this.panel = this.addContentEl(new GreedPanelElement(GreedPanelElement.WIDTH, GreedPanelElement.TRADER_HEIGHT, true, true)
                .layout(this.translateWorldSpatial()));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.panel != null) {
            this.panel.refreshIfChanged();
        }
    }
}
