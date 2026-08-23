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
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundClaimMilestoneMessage;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundTakeTrialMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The greed UI below the window frame: the six-tab strip plus either the rank summary (Main) or the
 * tab category's achievement list. Resizable, swapping {@link GreedMetrics} table with the panel size.
 */
public class GreedPanelElement extends ContainerElement<GreedPanelElement> {
    public static final int WIDTH = 345;
    public static final int TRADER_HEIGHT = 189;

    private static final int MIN_WIDTH = 240;
    private static final int MIN_HEIGHT = 110;
    private static final int SCROLLBAR_ALLOWANCE = 11;

    private final boolean claimEnabled;
    private final boolean ownFrame;

    private int panelWidth;
    private int panelHeight;
    private GreedMetrics metrics;

    private GreedTab tab = GreedTab.MAIN;
    private GreedScrollList list;
    private float savedScroll;
    private int lastSignature;
    private boolean rebuildPending;

    public GreedPanelElement(int width, int height, boolean claimEnabled, boolean ownFrame) {
        super(Spatials.positionXY(0, 0).size(Math.max(MIN_WIDTH, width), Math.max(MIN_HEIGHT, height)));
        this.claimEnabled = claimEnabled;
        this.ownFrame = ownFrame;
        this.panelWidth = Math.max(MIN_WIDTH, width);
        this.panelHeight = Math.max(MIN_HEIGHT, height);
        this.metrics = GreedMetrics.forSize(this.panelWidth, this.panelHeight);
        this.lastSignature = signature();
        this.rebuild();
    }

    /** Resizes the panel to the rectangle its host screen wants it to fill, rebuilding if that moved. */
    public void applyBounds(int width, int height) {
        int newWidth = Math.max(MIN_WIDTH, width);
        int newHeight = Math.max(MIN_HEIGHT, height);
        if (newWidth == this.panelWidth && newHeight == this.panelHeight) {
            return;
        }
        this.panelWidth = newWidth;
        this.panelHeight = newHeight;
        this.metrics = GreedMetrics.forSize(newWidth, newHeight);
        this.setWidth(newWidth);
        this.setHeight(newHeight);
        this.rebuild();
    }

    public void setTab(GreedTab tab) {
        this.tab = tab;
        this.savedScroll = 0.0F;
        this.requestRebuild();
    }

    /** Queues a rebuild for the next screen tick; safe to call from inside click dispatch. */
    public void requestRebuild() {
        this.rebuildPending = true;
    }

    /** Applies a queued rebuild, and otherwise rebuilds only when the synced milestone state moved. */
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
        hash = 31 * hash + ClientMilestoneData.getBestGodLevel();
        return hash;
    }

    /** Tears the panel down and rebuilds it from the current client milestone mirror. */
    public void rebuild() {
        if (this.list != null) {
            this.savedScroll = this.list.getScrollValue();
            this.list = null;
        }
        this.removeAllElements();
        if (this.ownFrame) {
            this.addElement(new NineSliceElement<>(Spatials.positionXYZ(0, 0, 0).size(this.panelWidth, this.panelHeight),
                    ScreenTextures.DEFAULT_WINDOW_BACKGROUND));
            this.addElement(new LabelElement<>(Spatials.positionXYZ(this.metrics.margin, 6, 1),
                    this.tab.getTitle().copy().setStyle(Style.EMPTY.withColor(GreedTheme.TEXT_TITLE)),
                    LabelTextStyle.defaultStyle()));
        }
        this.buildTabStrip();
        GreedScrollList content = this.buildContentList();
        if (this.tab == GreedTab.MAIN) {
            this.buildMain(content);
        } else {
            this.buildList(content);
        }
        ScreenLayout.requestLayout();
    }

    private void buildTabStrip() {
        GreedTab[] tabs = GreedTab.values();
        int available = this.panelWidth - this.metrics.margin * 2;
        int tabWidth = (available - (tabs.length - 1)) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            GreedTab entry = tabs[i];
            int x = this.metrics.margin + i * (tabWidth + 1);
            this.addElement(new GreedSubTabElement(
                    Spatials.positionXYZ(x, this.metrics.tabStripY, 1).size(tabWidth, this.metrics.tabHeight),
                    entry, entry == this.tab, this::setTab));
        }
    }

    private GreedScrollList buildContentList() {
        int width = this.panelWidth - this.metrics.margin * 2;
        int height = this.panelHeight - this.metrics.contentY - this.metrics.margin;
        this.addElement(new GreedFillElement(Spatials.positionXYZ(this.metrics.margin, this.metrics.contentY, 1)
                .size(width, height), GreedTheme.PLATE_DARK, GreedTheme.BORDER));
        GreedScrollList created = this.addElement(new GreedScrollList(
                Spatials.positionXYZ(this.metrics.margin + 1, this.metrics.contentY + 1, 2).size(width - 2, height - 2)));
        this.list = created;
        created.setScrollValue(this.savedScroll);
        return created;
    }

    private int innerWidth() {
        return this.panelWidth - this.metrics.margin * 2 - 2 - SCROLLBAR_ALLOWANCE;
    }

    private void buildMain(GreedScrollList content) {
        GreedMetrics m = this.metrics;
        int available = this.innerWidth();
        int columnWidth = Math.min(available, m.mainColumnMaxWidth);
        int columnX = 1 + Math.max(0, (available - columnWidth) / 2);

        int rank = ClientMilestoneData.getRank();
        int reputation = ClientMilestoneData.getReputation();
        int nextThreshold = ClientMilestoneData.getNextRankThreshold();

        int medallionY = 4;
        RankMedallionElement medallion = new RankMedallionElement(
                Spatials.positionXYZ(columnX + (columnWidth - m.medallionSize) / 2, medallionY, 1)
                        .size(m.medallionSize, m.medallionSize), ClientMilestoneData::getRank);
        medallion.tooltip(Tooltips.multi(() -> List.of(GreedTheme.rankName(ClientMilestoneData.getRank()))));
        content.addElement(medallion);

        int barY = medallionY + m.medallionSize + 6;
        int barX = columnX + m.repFlankWidth + 4;
        int barWidth = columnWidth - (m.repFlankWidth + 4) * 2;

        var currentFlank = new LabelElement<>(Spatials.positionXYZ(columnX, barY + (m.repBarHeight - 8) / 2, 1),
                (ISize) Spatials.size(m.repFlankWidth, 9),
                GreedTheme.text(GreedTheme.rankShortLabel(rank), GreedTheme.GOLD),
                LabelTextStyle.defaultStyle().center());
        currentFlank.tooltip(Tooltips.multi(() -> List.of(GreedTheme.rankName(rank))));
        content.addElement(currentFlank);

        float fill = nextThreshold <= 0 ? 1.0F
                : Math.max(0.0F, Math.min(1.0F, (float) reputation / (float) nextThreshold));
        GreedProgressBarElement repBar = new GreedProgressBarElement(
                Spatials.positionXYZ(barX, barY, 1).size(barWidth, m.repBarHeight),
                () -> fill,
                () -> GreedTheme.text(reputation + "/" + nextThreshold, GreedTheme.TEXT_TITLE),
                GreedTheme.GOLD_DIM);
        repBar.progressTooltip(reputation, nextThreshold);
        content.addElement(repBar);

        var nextFlank = new LabelElement<>(Spatials.positionXYZ(barX + barWidth + 4, barY + (m.repBarHeight - 8) / 2, 1),
                (ISize) Spatials.size(m.repFlankWidth, 9),
                GreedTheme.text(GreedTheme.rankShortLabel(rank + 1), GreedTheme.TEXT_DIM),
                LabelTextStyle.defaultStyle().center());
        nextFlank.tooltip(Tooltips.multi(() -> List.of(GreedTheme.rankName(rank + 1))));
        content.addElement(nextFlank);

        int gainY = barY + m.repBarHeight + 7;
        buildGainBox(content, columnX, gainY, columnWidth, m, rank);

        int claimAllY = gainY + m.unlockBoxHeight + 3;
        buildClaimAll(content, columnX, claimAllY, columnWidth, m);

        int pinnedY = claimAllY + m.rowClaimHeight + 5;
        content.addElement(new LabelElement<>(Spatials.positionXYZ(columnX, pinnedY, 1),
                GreedTheme.langColored("pinned_task", GreedTheme.GOLD), LabelTextStyle.defaultStyle()));
        MilestoneDefinition pinned = MilestoneRegistry.get(ClientMilestoneData.getPinned());
        int afterPinned;
        if (pinned == null) {
            content.addElement(new LabelElement<>(Spatials.positionXYZ(columnX + 4, pinnedY + 13, 1),
                    (ISize) Spatials.size(columnWidth - 8, 9),
                    GreedTheme.langColored("pinned_task.empty", GreedTheme.TEXT_DIM), LabelTextStyle.defaultStyle()));
            afterPinned = pinnedY + 24;
        } else {
            content.addElement(new MilestoneRowElement(columnX, pinnedY + 11, columnWidth, m,
                    pinned, false, false, this::requestRebuild));
            afterPinned = pinnedY + 11 + m.rowHeight;
        }

        int bottomY = afterPinned + 6;
        content.addElement(new LabelElement<>(Spatials.positionXYZ(columnX, bottomY, 1),
                (ISize) Spatials.size(columnWidth, 9), activeGodLine(), LabelTextStyle.defaultStyle()));
        var rerollLabel = new LabelElement<>(Spatials.positionXYZ(columnX, bottomY + 11, 1),
                (ISize) Spatials.size(columnWidth, 9),
                GreedTheme.langColored("shop_reroll_cost", GreedTheme.TEXT, ClientMilestoneData.getShopRerollCost()),
                LabelTextStyle.defaultStyle());
        rerollLabel.tooltip(Tooltips.multi(() -> List.of(GreedTheme.lang("shop_reroll_cost.tooltip"))));
        content.addElement(rerollLabel);
    }

    /** The {@code claimAll} button; disabled with a hint on the player tab, where the server refuses claims. */
    private void buildClaimAll(GreedScrollList content, int columnX, int y, int columnWidth, GreedMetrics m) {
        boolean claimEnabled = this.claimEnabled;
        GreedButtonElement claimAll = new GreedButtonElement(
                Spatials.positionXYZ(columnX, y, 1).size(columnWidth, m.rowClaimHeight),
                () -> GreedTheme.lang("claim_all", ClientMilestoneData.getUnclaimedReputation()),
                () -> {
                    NetworkHandler.INSTANCE.sendToServer(new ServerboundClaimMilestoneMessage());
                    this.requestRebuild();
                });
        claimAll.setDisabled(() -> !claimEnabled || ClientMilestoneData.getUnclaimedReputation() <= 0);
        claimAll.setHighlighted(() -> claimEnabled && ClientMilestoneData.getUnclaimedReputation() > 0);
        claimAll.tooltip(Tooltips.multi(() -> claimEnabled
                ? List.of(GreedTheme.lang("claim_all.tooltip"))
                : List.of(GreedTheme.lang("claim.tooltip.player"))));
        content.addElement(claimAll);
    }

    private void buildList(GreedScrollList content) {
        int rowWidth = this.innerWidth();
        List<MilestoneDefinition> entries = new ArrayList<>(MilestoneRegistry.getByCategory(this.tab.getCategory()));
        entries.sort(Comparator.comparing(definition -> I18n.get(definition.getNameKey())));
        if (entries.isEmpty()) {
            content.addElement(new LabelElement<>(Spatials.positionXYZ(4, 4, 1), (ISize) Spatials.size(rowWidth, 9),
                    GreedTheme.langColored("list.empty", GreedTheme.TEXT_DIM), LabelTextStyle.defaultStyle()));
            return;
        }
        int y = 1;
        for (MilestoneDefinition definition : entries) {
            content.addElement(new MilestoneRowElement(1, y, rowWidth, this.metrics, definition,
                    this.claimEnabled, true, this::requestRebuild));
            y += this.metrics.rowHeight + this.metrics.rowGap;
        }
    }

    /** The "gain on rank up" plate; becomes a "Take Trial" button when ready, but only on a rebuild. */
    private static void buildGainBox(GreedScrollList content, int x, int y, int width, GreedMetrics m, int rank) {
        content.addElement(new GreedFillElement(Spatials.positionXYZ(x, y, 1)
                .size(width, m.unlockBoxHeight), GreedTheme.PLATE, GreedTheme.GOLD_DEEP));
        int targetRank = rank + 1;
        if (ClientMilestoneData.isTrialReady()) {
            GreedButtonElement take = new GreedButtonElement(
                    Spatials.positionXYZ(x + 3, y + 3, 2).size(width - 6, m.unlockBoxHeight - 6),
                    () -> GreedTheme.lang("take_trial"),
                    () -> NetworkHandler.INSTANCE.sendToServer(new ServerboundTakeTrialMessage()));
            take.setHighlighted(() -> true);
            take.tooltip(Tooltips.multi(() -> List.of(
                    GreedTheme.langColored(ClientMilestoneData.isTrialHyper()
                            ? "take_trial.hyper" : "take_trial.vessel", GreedTheme.TEXT),
                    GreedTheme.langColored("take_trial.reward", GreedTheme.TEXT_DIM,
                            MilestoneRankLadder.getTrialCoinReward(targetRank)),
                    GreedTheme.rankName(targetRank))));
            content.addElement(take);
            return;
        }
        content.addElement(new LabelElement<>(Spatials.positionXYZ(x + 4, y + 4, 2),
                GreedTheme.langColored("gain_on_rank_up", GreedTheme.GOLD), LabelTextStyle.defaultStyle()));
        content.addElement(new LabelElement<>(Spatials.positionXYZ(x + 4, y + 15, 2),
                (ISize) Spatials.size(width - 8, m.unlockBoxHeight - 30),
                nextRankUnlocks(rank), LabelTextStyle.defaultStyle().wrap()));
        content.addElement(new LabelElement<>(Spatials.positionXYZ(x + 4, y + m.unlockBoxHeight - 12, 2),
                (ISize) Spatials.size(width - 8, 9), trialRequirement(), LabelTextStyle.defaultStyle()));
    }

    /** The requirement still standing between the player and the next rank's trial. */
    private static Component trialRequirement() {
        if (ClientMilestoneData.getRank() <= 0) {
            return GreedTheme.langColored("take_trial.herald", GreedTheme.TEXT_DIM);
        }
        if (!ClientMilestoneData.hasTrial()) {
            return GreedTheme.langColored("take_trial.none", GreedTheme.TEXT_DIM);
        }
        if (ClientMilestoneData.getReputation() < ClientMilestoneData.getNextRankThreshold()) {
            return GreedTheme.langColored("take_trial.locked.reputation", GreedTheme.TEXT_DIM,
                    ClientMilestoneData.getNextRankThreshold() - ClientMilestoneData.getReputation());
        }
        return GreedTheme.langColored("take_trial.locked.god", GreedTheme.TEXT_DIM,
                ClientMilestoneData.getTrialGodGate(), ClientMilestoneData.getBestGodLevel());
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
