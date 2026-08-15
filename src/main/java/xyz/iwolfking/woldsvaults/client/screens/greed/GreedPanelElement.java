package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.milestones.MilestoneDefinition;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The whole greed UI below the window frame: the six-tab strip plus either the rank summary
 * (Main) or a scrollable achievement list for the tab's category. Both host screens embed this
 * one element, so the player menu and Mr. Greedy render identical content and differ only in
 * whether claiming is enabled.
 */
public class GreedPanelElement extends ContainerElement<GreedPanelElement> {
    public static final int WIDTH = 345;
    public static final int TRADER_HEIGHT = 189;
    public static final int PLAYER_HEIGHT = 229;

    private static final int TAB_STRIP_Y = 18;
    private static final int TAB_HEIGHT = 14;
    private static final int CONTENT_Y = 36;
    private static final int MARGIN = 7;
    private static final int ROW_GAP = 3;

    private final int panelHeight;
    private final boolean claimEnabled;

    private GreedTab tab = GreedTab.MAIN;
    private GreedScrollList list;
    private float savedScroll;
    private int lastSignature;
    private boolean rebuildPending;

    public GreedPanelElement(int x, int y, int panelHeight, boolean claimEnabled) {
        super(Spatials.positionXY(x, y).size(WIDTH, panelHeight));
        this.panelHeight = panelHeight;
        this.claimEnabled = claimEnabled;
        this.lastSignature = signature();
        this.rebuild();
    }

    public void setTab(GreedTab tab) {
        this.tab = tab;
        this.savedScroll = 0.0F;
        this.requestRebuild();
    }

    /**
     * Queues a rebuild for the next screen tick. Tab, pin and claim buttons all call this from
     * inside the element store's own click dispatch, and tearing that store down mid-iteration
     * would be a concurrent modification.
     */
    public void requestRebuild() {
        this.rebuildPending = true;
    }

    /**
     * Applies a queued rebuild, and otherwise rebuilds only when the synced milestone state
     * actually moved. Counters tick every second while the owner is inside a vault, so an
     * unconditional per-tick rebuild would reset the scroll position and re-allocate every row
     * for nothing.
     */
    public void refreshIfChanged() {
        int current = signature();
        if (this.rebuildPending || current != this.lastSignature) {
            this.lastSignature = current;
            this.rebuildPending = false;
            this.rebuild();
        }
    }

    private static int signature() {
        int hash = ClientMilestoneData.getAll().hashCode();
        hash = 31 * hash + ClientMilestoneData.getAllClaimedTiers().hashCode();
        hash = 31 * hash + Objects.hashCode(ClientMilestoneData.getPinned());
        hash = 31 * hash + ClientMilestoneData.getRank();
        hash = 31 * hash + ClientMilestoneData.getReputation();
        hash = 31 * hash + ClientMilestoneData.getNextRankThreshold();
        hash = 31 * hash + ClientMilestoneData.getShopRerollCost();
        return hash;
    }

    /**
     * Tears the panel down and rebuilds it from the current client milestone mirror. Called on
     * every tab switch, pin toggle and claim, which is also how the screens pick up the sync that
     * follows a server round trip.
     */
    public void rebuild() {
        if (this.list != null) {
            this.savedScroll = this.list.getScrollValue();
            this.list = null;
        }
        this.removeAllElements();
        this.addElement(new NineSliceElement<>(Spatials.positionXYZ(0, 0, 0).size(WIDTH, this.panelHeight),
                ScreenTextures.DEFAULT_WINDOW_BACKGROUND));
        this.addElement(new LabelElement<>(Spatials.positionXYZ(MARGIN, 6, 1),
                this.tab.getTitle().copy().setStyle(Style.EMPTY.withColor(GreedTheme.TEXT_TITLE)),
                LabelTextStyle.defaultStyle()));
        this.buildTabStrip();
        if (this.tab == GreedTab.MAIN) {
            this.buildMain();
        } else {
            this.buildList();
        }
        ScreenLayout.requestLayout();
    }

    private void buildTabStrip() {
        GreedTab[] tabs = GreedTab.values();
        int available = WIDTH - MARGIN * 2;
        int tabWidth = (available - (tabs.length - 1)) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            GreedTab entry = tabs[i];
            int x = MARGIN + i * (tabWidth + 1);
            this.addElement(new GreedSubTabElement(
                    Spatials.positionXYZ(x, TAB_STRIP_Y, 1).size(tabWidth, TAB_HEIGHT),
                    entry, entry == this.tab, this::setTab));
        }
    }

    private void buildMain() {
        int contentWidth = WIDTH - MARGIN * 2;
        int rank = ClientMilestoneData.getRank();
        int reputation = ClientMilestoneData.getReputation();
        int nextThreshold = ClientMilestoneData.getNextRankThreshold();
        int currentThreshold = MilestoneRankLadder.getThreshold(rank);

        this.addElement(new GreedFillElement(Spatials.positionXYZ(MARGIN, CONTENT_Y, 1)
                .size(contentWidth, this.panelHeight - CONTENT_Y - MARGIN),
                GreedTheme.PLATE_DARK, GreedTheme.BORDER));

        RankMedallionElement medallion = new RankMedallionElement(
                Spatials.positionXYZ(WIDTH / 2 - 17, CONTENT_Y + 4, 2).size(34, 34), ClientMilestoneData::getRank);
        medallion.tooltip(Tooltips.multi(() -> List.of(GreedTheme.rankName(ClientMilestoneData.getRank()))));
        this.addElement(medallion);

        int barY = CONTENT_Y + 42;
        int flankWidth = 24;
        int barX = MARGIN + 4 + flankWidth;
        int barWidth = contentWidth - 8 - flankWidth * 2;

        var currentFlank = new LabelElement<>(Spatials.positionXYZ(MARGIN + 4, barY + 3, 2),
                (ISize) Spatials.size(flankWidth, 9),
                GreedTheme.text(GreedTheme.rankShortLabel(rank), GreedTheme.GOLD),
                LabelTextStyle.defaultStyle().center());
        currentFlank.tooltip(Tooltips.multi(() -> List.of(GreedTheme.rankName(rank))));
        this.addElement(currentFlank);

        float span = Math.max(1, nextThreshold - currentThreshold);
        float fill = Math.max(0.0F, (reputation - currentThreshold) / span);
        this.addElement(new GreedProgressBarElement(Spatials.positionXYZ(barX, barY, 2).size(barWidth, 13),
                () -> fill,
                () -> GreedTheme.text(reputation + "/" + nextThreshold, GreedTheme.TEXT_TITLE),
                GreedTheme.GOLD_DIM));

        var nextFlank = new LabelElement<>(Spatials.positionXYZ(barX + barWidth + 4, barY + 3, 2),
                (ISize) Spatials.size(flankWidth, 9),
                GreedTheme.text(GreedTheme.rankShortLabel(rank + 1), GreedTheme.TEXT_DIM),
                LabelTextStyle.defaultStyle().center());
        nextFlank.tooltip(Tooltips.multi(() -> List.of(GreedTheme.rankName(rank + 1))));
        this.addElement(nextFlank);

        int gainY = barY + 20;
        int gainHeight = 44;
        this.addElement(new GreedFillElement(Spatials.positionXYZ(MARGIN + 4, gainY, 2)
                .size(contentWidth - 8, gainHeight), GreedTheme.PLATE, GreedTheme.GOLD_DEEP));
        this.addElement(new LabelElement<>(Spatials.positionXYZ(MARGIN + 8, gainY + 4, 3),
                GreedTheme.langColored("gain_on_rank_up", GreedTheme.GOLD), LabelTextStyle.defaultStyle()));
        this.addElement(new LabelElement<>(Spatials.positionXYZ(MARGIN + 8, gainY + 15, 3),
                (ISize) Spatials.size(contentWidth - 16, 26),
                nextRankUnlocks(rank), LabelTextStyle.defaultStyle().wrap()));

        int pinnedY = gainY + gainHeight + 5;
        this.addElement(new LabelElement<>(Spatials.positionXYZ(MARGIN + 4, pinnedY, 2),
                GreedTheme.langColored("pinned_task", GreedTheme.GOLD), LabelTextStyle.defaultStyle()));
        MilestoneDefinition pinned = MilestoneRegistry.get(ClientMilestoneData.getPinned());
        if (pinned == null) {
            this.addElement(new LabelElement<>(Spatials.positionXYZ(MARGIN + 8, pinnedY + 14, 2),
                    (ISize) Spatials.size(contentWidth - 16, 9),
                    GreedTheme.langColored("pinned_task.empty", GreedTheme.TEXT_DIM), LabelTextStyle.defaultStyle()));
        } else {
            this.addElement(new MilestoneRowElement(MARGIN + 4, pinnedY + 11, contentWidth - 8,
                    pinned, false, false, this::requestRebuild));
        }

        int bottomY = this.panelHeight - MARGIN - 22;
        this.addElement(new LabelElement<>(Spatials.positionXYZ(MARGIN + 4, bottomY, 2),
                (ISize) Spatials.size(contentWidth - 8, 9), activeGodLine(), LabelTextStyle.defaultStyle()));
        var rerollLabel = new LabelElement<>(Spatials.positionXYZ(MARGIN + 4, bottomY + 10, 2),
                (ISize) Spatials.size(contentWidth - 8, 9),
                GreedTheme.langColored("shop_reroll_cost", GreedTheme.TEXT, ClientMilestoneData.getShopRerollCost()),
                LabelTextStyle.defaultStyle());
        rerollLabel.tooltip(Tooltips.multi(() -> List.of(GreedTheme.lang("shop_reroll_cost.tooltip"))));
        this.addElement(rerollLabel);
    }

    private void buildList() {
        int listWidth = WIDTH - MARGIN * 2;
        int listHeight = this.panelHeight - CONTENT_Y - MARGIN;
        this.addElement(new GreedFillElement(Spatials.positionXYZ(MARGIN, CONTENT_Y, 1).size(listWidth, listHeight),
                GreedTheme.PLATE_DARK, GreedTheme.BORDER));
        GreedScrollList list = this.addElement(new GreedScrollList(
                Spatials.positionXYZ(MARGIN + 1, CONTENT_Y + 1, 2).size(listWidth - 2, listHeight - 2)));
        this.list = list;
        list.setScrollValue(this.savedScroll);

        List<MilestoneDefinition> entries = new ArrayList<>(MilestoneRegistry.getByCategory(this.tab.getCategory()));
        entries.sort(Comparator.comparing(definition -> I18n.get(definition.getNameKey())));
        int rowWidth = listWidth - 11;
        if (entries.isEmpty()) {
            list.addElement(new LabelElement<>(Spatials.positionXYZ(4, 4, 1), (ISize) Spatials.size(rowWidth, 9),
                    GreedTheme.langColored("list.empty", GreedTheme.TEXT_DIM), LabelTextStyle.defaultStyle()));
            return;
        }
        int y = 1;
        for (MilestoneDefinition definition : entries) {
            list.addElement(new MilestoneRowElement(1, y, rowWidth, definition, this.claimEnabled, true, this::requestRebuild));
            y += MilestoneRowElement.HEIGHT + ROW_GAP;
        }
    }

    private static Component nextRankUnlocks(int rank) {
        return new TranslatableComponent(GreedTheme.rankUnlockKey(rank + 1))
                .setStyle(Style.EMPTY.withColor(GreedTheme.TEXT));
    }

    private static Component activeGodLine() {
        Player player = Minecraft.getInstance().player;
        Optional<VaultGod> god = player == null ? Optional.empty() : ActiveGodResolver.getActiveGod(player);
        if (god.isEmpty()) {
            return GreedTheme.langColored("active_god.none", GreedTheme.TEXT);
        }
        VaultGod active = god.get();
        return GreedTheme.langColored("active_god", GreedTheme.TEXT,
                new TextComponent(active.getName()).setStyle(Style.EMPTY.withColor(active.getColor())),
                ClientGodAlignmentData.getLevel(active));
    }
}
