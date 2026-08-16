package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.GreedTraderScreen;
import iskallia.vault.container.GreedTraderContainer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Mr. Greedy's achievements tab. It is a fourth tab of the greed trader's own tab strip and reuses
 * the trader's container, which is exactly what the server-side claim gate checks - so this is the
 * only screen where the claim buttons do anything.
 *
 * <p>{@code getTab()} returns null on purpose: the base strip then draws none of Shop, Quests or
 * Challenges as selected, and its switch handler still routes clicks on them normally.</p>
 */
public class GreedTraderAchievementsScreen extends GreedTraderScreen {
    private GreedPanelElement panel;

    public GreedTraderAchievementsScreen(GreedTraderContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.setGuiSize(Spatials.size(GreedPanelElement.WIDTH + 10, GreedPanelElement.TRADER_HEIGHT + 10));
    }

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
