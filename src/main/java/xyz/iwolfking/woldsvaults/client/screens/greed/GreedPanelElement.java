package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.NineSliceTextInputElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
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

import java.util.*;
import org.lwjgl.glfw.GLFW;

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
    private static final int SEARCH_HEIGHT = 12;
    private static final int BODY_PAD = 3;

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
    private SearchField search;
    private String searchQuery = "";
    private static SortKey sortKey = SortKey.NAME;
    private static boolean sortDescending;

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
        if (this.search != null && !this.tab.isList()) {
            this.search.setFocused(false);
        }
        this.removeAllElements();
        boolean locked = ClientMilestoneData.getRank() <= 0;
        if (this.ownFrame) {
            this.addElement(new NineSliceElement<>(Spatials.positionXYZ(0, 0, 0).size(this.panelWidth, this.panelHeight),
                    ScreenTextures.DEFAULT_WINDOW_BACKGROUND));
            Component title = locked ? GreedTheme.lang("title") : this.tab.getTitle();
            this.addElement(new LabelElement<>(Spatials.positionXYZ(this.metrics.margin, 6, 1),
                    title.copy().setStyle(Style.EMPTY.withColor(GreedTheme.TEXT_TITLE)),
                    LabelTextStyle.defaultStyle()));
        }
        if (locked) {
            this.buildHeraldLock();
            ScreenLayout.requestLayout();
            return;
        }
        this.buildTabStrip();
        int bodyY = this.metrics.tabStripY + this.metrics.tabHeight;
        this.addElement(new GreedFolderElement(Spatials.positionXYZ(this.metrics.margin, bodyY, 1)
                .size(this.panelWidth - this.metrics.margin * 2, this.panelHeight - this.metrics.margin - bodyY)));
        int top = bodyY + BODY_PAD;
        if (this.tab.isList()) {
            this.buildSearch(top);
            top += SEARCH_HEIGHT + 2;
        }
        GreedScrollList content = this.buildContentList(top);
        if (this.tab == GreedTab.MAIN) {
            this.buildMain(content);
        } else {
            this.buildList(content);
        }
        ScreenLayout.requestLayout();
    }

    /** The whole panel before the Herald is beaten: one line of text, no tabs and no achievement list. */
    private void buildHeraldLock() {
        int width = this.panelWidth - this.metrics.margin * 2;
        int height = this.panelHeight - this.metrics.contentY - this.metrics.margin;
        this.addElement(new GreedFillElement(Spatials.positionXYZ(this.metrics.margin, this.metrics.contentY, 1)
                .size(width, height), GreedTheme.PLATE_DARK, GreedTheme.BORDER));
        this.addElement(new LabelElement<>(
                Spatials.positionXYZ(this.metrics.margin + 8, this.metrics.contentY + height / 2 - 9, 2),
                (ISize) Spatials.size(width - 16, 18),
                GreedTheme.langColored("locked.herald", GreedTheme.TEXT_DIM),
                LabelTextStyle.defaultStyle().center().wrap()));
    }

    private void buildTabStrip() {
        GreedTab[] tabs = GreedTab.values();
        int available = this.panelWidth - this.metrics.margin * 2;
        int tabWidth = (available - (tabs.length - 1)) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            GreedTab entry = tabs[i];
            boolean selected = entry == this.tab;
            int x = this.metrics.margin + i * (tabWidth + 1);
            int width = i == tabs.length - 1 ? this.metrics.margin + available - x : tabWidth;
            this.addElement(new GreedSubTabElement(
                    Spatials.positionXYZ(x, this.metrics.tabStripY, selected ? 5 : 1).size(width, this.metrics.tabHeight),
                    entry, selected, this::setTab));
        }
    }

    private void buildSearch(int y) {
        Font font = Minecraft.getInstance().font;
        int x = this.metrics.margin + BODY_PAD;
        int width = this.bodyInnerWidth();
        int directionWidth = SEARCH_HEIGHT + 2;
        int sortWidth = 0;
        for (SortKey key : SortKey.values()) {
            sortWidth = Math.max(sortWidth, font.width(sortLabel(key)) + 10);
        }
        int searchWidth = width - sortWidth - directionWidth - 4;
        this.buildSortButtons(x + searchWidth + 2, y, sortWidth, directionWidth);

        SearchField field = new SearchField(Spatials.positionXYZ(x, y, 3).size(searchWidth, SEARCH_HEIGHT), font);
        field.setPlaceholderText(I18n.get(GreedTheme.LANG_ROOT + "search.placeholder"));
        field.setMaxLength(48);
        field.continueFrom(this.search, this.searchQuery);
        field.onTextChanged(text -> {
            if (!text.equals(this.searchQuery)) {
                this.searchQuery = text;
                this.savedScroll = 0.0F;
                this.requestRebuild();
            }
        });
        this.search = this.addElement(field);
    }

    private void buildSortButtons(int x, int y, int sortWidth, int directionWidth) {
        GreedButtonElement sort = new GreedButtonElement(Spatials.positionXYZ(x, y, 3).size(sortWidth, SEARCH_HEIGHT),
                () -> sortLabel(sortKey), () -> {
        }) {
            @Override
            public boolean onMouseClicked(double mouseX, double mouseY, int buttonIndex) {
                SortKey[] keys = SortKey.values();
                int step = buttonIndex == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? keys.length - 1 : 1;
                sortKey = keys[(sortKey.ordinal() + step) % keys.length];
                GreedPanelElement.this.resortList();
                return super.onMouseClicked(mouseX, mouseY, buttonIndex);
            }
        };
        sort.tooltip(Tooltips.multi(() -> List.of(
                GreedTheme.langColored("sort.tooltip", GreedTheme.TEXT, sortKey.label()),
                GreedTheme.langColored("sort.tooltip.cycle", GreedTheme.TEXT_DIM))));
        this.addElement(sort);

        GreedButtonElement direction = new GreedButtonElement(
                Spatials.positionXYZ(x + sortWidth + 2, y, 3).size(directionWidth, SEARCH_HEIGHT),
                () -> new TextComponent(sortDescending ? "▼" : "▲"), () -> {
                    sortDescending = !sortDescending;
                    this.resortList();
                });
        direction.tooltip(Tooltips.multi(() -> List.of(
                GreedTheme.langColored(sortDescending ? "sort.descending" : "sort.ascending", GreedTheme.TEXT),
                GreedTheme.langColored("sort.direction.tooltip", GreedTheme.TEXT_DIM))));
        this.addElement(direction);
    }

    private void resortList() {
        this.savedScroll = 0.0F;
        this.requestRebuild();
    }

    private static Component sortLabel(SortKey key) {
        return GreedTheme.lang("sort", key.label());
    }

    /** What a category's list can be ordered by; ties always fall back to the name, A to Z. */
    private enum SortKey {
        NAME("name", Comparator.comparing((MilestoneDefinition definition) -> sortName(definition))),
        TIER("tier", Comparator.comparingInt((MilestoneDefinition definition) ->
                definition.getCompletedTiers(ClientMilestoneData.getValue(definition.getId())))),
        REPUTATION("reputation", Comparator.comparingInt(GreedPanelElement::totalReputation)),
        PROGRESS("progress", Comparator.comparingDouble(GreedPanelElement::progress)),
        CLOSEST("closest", Comparator.comparingDouble(GreedPanelElement::toNextTier));

        private final String key;
        private final Comparator<MilestoneDefinition> order;

        SortKey(String key, Comparator<MilestoneDefinition> order) {
            this.key = key;
            this.order = order;
        }

        Component label() {
            return GreedTheme.lang("sort." + this.key);
        }

        Comparator<MilestoneDefinition> comparator(boolean descending) {
            Comparator<MilestoneDefinition> primary = descending ? this.order.reversed() : this.order;
            return primary.thenComparing(GreedPanelElement::sortName).thenComparing(MilestoneDefinition::getId);
        }
    }

    private static String sortName(MilestoneDefinition definition) {
        return I18n.get(definition.getNameKey()).toLowerCase(Locale.ROOT);
    }

    private static int totalReputation(MilestoneDefinition definition) {
        int total = 0;
        for (int tier = 0; tier < definition.getTierCount(); tier++) {
            total += definition.getReputation(tier);
        }
        return total;
    }

    private static double progress(MilestoneDefinition definition) {
        long value = ClientMilestoneData.getValue(definition.getId());
        int tiers = definition.getTierCount();
        int completed = definition.getCompletedTiers(value);
        if (tiers <= 0 || completed >= tiers) {
            return 1.0;
        }
        return (completed + tierProgress(definition, value, completed)) / tiers;
    }

    private static double toNextTier(MilestoneDefinition definition) {
        long value = ClientMilestoneData.getValue(definition.getId());
        int completed = definition.getCompletedTiers(value);
        if (completed >= definition.getTierCount()) {
            return Double.POSITIVE_INFINITY;
        }
        return 1.0 - tierProgress(definition, value, completed);
    }

    private static double tierProgress(MilestoneDefinition definition, long value, int completed) {
        long floor = completed == 0 ? 0L : definition.getThreshold(completed - 1);
        long span = definition.getThreshold(completed) - floor;
        return span <= 0L ? 0.0 : Math.max(0.0, Math.min(1.0, (double) (value - floor) / span));
    }

    /** While the search box has focus it takes every key (so the inventory key types instead of closing); Escape lets go. */
    public boolean searchKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.search == null || !this.search.isFocused()) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.search.setFocused(false);
            return true;
        }
        this.search.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    public boolean searchCharTyped(char character, int modifiers) {
        if (this.search == null || !this.search.isFocused()) {
            return false;
        }
        this.search.charTyped(character, modifiers);
        return true;
    }

    /** A click anywhere outside the search box lets go of it. */
    public void searchClickedAway(double mouseX, double mouseY) {
        if (this.search != null && this.search.isFocused() && !this.search.getWorldSpatial().contains(mouseX, mouseY)) {
            this.search.setFocused(false);
        }
    }

    /**
     * Vault Hunters' text box with the editing a desktop text field has. Focusing it (by a click or a panel rebuild)
     * leaves the caret where it is instead of selecting everything; only Ctrl+A, Shift with the arrows or Home/End,
     * Shift-clicking or dragging across the text select. Ctrl+Left/Right jump a word (with Shift, selecting it), and
     * Ctrl+Backspace/Delete remove one. The placeholder hides while the box has focus.
     */
    private static final class SearchField extends NineSliceTextInputElement<SearchField> {
        private boolean dragging;

        SearchField(ISpatial spatial, Font font) {
            super(spatial, ScreenTextures.INSET_BLACK_BACKGROUND, font);
        }

        void continueFrom(@Nullable SearchField previous, String query) {
            this.text = previous == null ? query : previous.text;
            this.cursorPos = previous == null ? this.text.length() : previous.cursorPos;
            this.selectionStart = previous == null ? this.cursorPos : previous.selectionStart;
            this.selectionEnd = previous == null ? this.cursorPos : previous.selectionEnd;
            this.displayPos = previous == null ? 0 : previous.displayPos;
            this.tickCount = previous == null ? 0 : previous.tickCount;
            this.focused = previous != null && previous.focused;
        }

        @Override
        public SearchField setFocused(boolean focused) {
            int cursor = this.cursorPos;
            super.setFocused(focused);
            this.cursorPos = cursor;
            this.selectionStart = cursor;
            this.selectionEnd = cursor;
            return this;
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button) {
            boolean handled = super.onMouseClicked(mouseX, mouseY, button);
            this.dragging = handled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
            return handled;
        }

        @Override
        public boolean onMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.dragging || !this.focused) {
                return false;
            }
            this.cursorPos = this.getCharIndexAtPosition((int) mouseX - this.getWorldSpatial().x() - this.paddingLeft + this.displayPos);
            this.selectionEnd = this.cursorPos;
            this.updateDisplayPos();
            return true;
        }

        @Override
        public boolean onMouseReleased(double mouseX, double mouseY, int button) {
            this.dragging = false;
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.focused && Screen.hasControlDown() && !Screen.hasAltDown()) {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_LEFT -> {
                        this.moveCursor(this.wordEdge(-1) - this.cursorPos, Screen.hasShiftDown());
                        return true;
                    }
                    case GLFW.GLFW_KEY_RIGHT -> {
                        this.moveCursor(this.wordEdge(1) - this.cursorPos, Screen.hasShiftDown());
                        return true;
                    }
                    case GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_DELETE -> {
                        if (!this.hasSelection()) {
                            this.selectionStart = this.cursorPos;
                            this.selectionEnd = this.wordEdge(keyCode == GLFW.GLFW_KEY_BACKSPACE ? -1 : 1);
                        }
                        String before = this.text;
                        this.deleteSelection();
                        if (!this.text.equals(before) && this.onTextChanged != null) {
                            this.onTextChanged.accept(this.text);
                        }
                        return true;
                    }
                    default -> {
                    }
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private int wordEdge(int direction) {
            int at = this.cursorPos;
            if (direction < 0) {
                while (at > 0 && this.text.charAt(at - 1) == ' ') {
                    at--;
                }
                while (at > 0 && this.text.charAt(at - 1) != ' ') {
                    at--;
                }
            } else {
                while (at < this.text.length() && this.text.charAt(at) != ' ') {
                    at++;
                }
                while (at < this.text.length() && this.text.charAt(at) == ' ') {
                    at++;
                }
            }
            return at;
        }

        @Override
        public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            this.setUsePlaceholder(!this.focused);
            super.render(renderer, poseStack, mouseX, mouseY, partialTick);
        }
    }

    private GreedScrollList buildContentList(int top) {
        int x = this.metrics.margin + BODY_PAD;
        int width = this.bodyInnerWidth();
        int height = this.panelHeight - top - this.metrics.margin - BODY_PAD;
        this.addElement(new GreedFillElement(Spatials.positionXYZ(x, top, 1).size(width, height),
                GreedTheme.PLATE_DARK, GreedTheme.BORDER));
        GreedScrollList created = this.addElement(new GreedScrollList(
                Spatials.positionXYZ(x + 1, top + 1, 2).size(width - 2, height - 2)));
        this.list = created;
        created.setScrollValue(this.savedScroll);
        return created;
    }

    private int bodyInnerWidth() {
        return this.panelWidth - this.metrics.margin * 2 - BODY_PAD * 2;
    }

    private int innerWidth() {
        return this.bodyInnerWidth() - 2 - SCROLLBAR_ALLOWANCE;
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
        repBar.molten().progressTooltip(reputation, nextThreshold);
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
        List<MilestoneDefinition> pinned = ClientMilestoneData.getPinned().stream().map(MilestoneRegistry::get)
                .filter(Objects::nonNull).sorted(MilestoneDefinition.compareByProgress().reversed()).toList();
        int afterPinned;
        if (pinned.isEmpty()) {
            content.addElement(new LabelElement<>(Spatials.positionXYZ(columnX + 4, pinnedY + 13, 1),
                    (ISize) Spatials.size(columnWidth - 8, 9),
                    GreedTheme.langColored("pinned_task.empty", GreedTheme.TEXT_DIM), LabelTextStyle.defaultStyle()));
            afterPinned = pinnedY + 24;
        } else {
            afterPinned = this.stackRows(content, pinned, columnX, columnWidth, pinnedY + 11, false, false,
                    GreedRowStyle.current(), true);
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
        String query = this.searchQuery.trim().toLowerCase(Locale.ROOT);
        List<MilestoneDefinition> entries = new ArrayList<>(query.isEmpty()
                ? MilestoneRegistry.getByCategory(this.tab.getCategory())
                : MilestoneRegistry.getAll().stream().filter(definition -> matches(definition, query)).toList());
        entries.sort(sortKey.comparator(sortDescending));
        if (entries.isEmpty()) {
            content.addElement(new LabelElement<>(Spatials.positionXYZ(4, 4, 1), (ISize) Spatials.size(rowWidth, 9),
                    query.isEmpty() ? GreedTheme.langColored("list.empty", GreedTheme.TEXT_DIM)
                            : GreedTheme.langColored("search.empty", GreedTheme.TEXT_DIM, this.searchQuery.trim()),
                    LabelTextStyle.defaultStyle()));
            return;
        }
        this.stackRows(content, entries, 1, rowWidth, 1, this.claimEnabled, true, GreedRowStyle.current(), true);
    }

    private static boolean matches(MilestoneDefinition definition, String query) {
        return I18n.get(definition.getNameKey()).toLowerCase(Locale.ROOT).contains(query)
                || I18n.get(definition.getDescriptionKey()).toLowerCase(Locale.ROOT).contains(query);
    }

    private int stackRows(GreedScrollList content, List<MilestoneDefinition> rows, int x, int width, int y,
                          boolean claimEnabled, boolean showClaim, GreedRowStyle style, boolean interactive) {
        int inset = style.framed() ? MilestoneFrame.SIDE_OVERHANG : 1;
        int rowHeight = style.height(this.metrics.rowHeight);
        if (!style.framed()) {
            content.addElement(MilestoneRowElement.ledgerHeader(x + inset, y, width - inset * 2));
            y += MilestoneRowElement.LEDGER_HEADER_HEIGHT;
        }
        int previousBottom = -1;
        int index = 0;
        for (MilestoneDefinition definition : rows) {
            int rarity = style.framed() ? rarityOf(definition) : MilestoneFrame.NONE;
            int top = style.ornaments() ? MilestoneFrame.topOverhang(rarity) : (rarity > MilestoneFrame.NONE ? 2 : 0);
            int gap = !style.framed() ? 0 : previousBottom < 0 ? top
                    : Math.max(this.metrics.rowGap, previousBottom + top + 2);
            y += gap;
            content.addElement(new MilestoneRowElement(x + inset, y, width - inset * 2, this.metrics, style, index++,
                    definition, claimEnabled, showClaim, interactive, this::requestRebuild));
            y += rowHeight;
            previousBottom = style.ornaments() ? MilestoneFrame.bottomOverhang(rarity) : (rarity > MilestoneFrame.NONE ? 2 : 0);
        }
        int end = y + Math.max(previousBottom, 0) + 1;
        content.addElement(new GreedFillElement(Spatials.positionXYZ(x, y, 0).size(1, end - y), 0));
        return end;
    }

    private static int rarityOf(MilestoneDefinition definition) {
        long value = ClientMilestoneData.getValue(definition.getId());
        return MilestoneFrame.rarity(definition.getCompletedTiers(value), definition.getTierCount());
    }

    /** The "gain on rank up" plate; becomes a "Take Trial" button when ready, but only on a rebuild. */
    private static void buildGainBox(GreedScrollList content, int x, int y, int width, GreedMetrics m, int rank) {
        content.addElement(new GreedFillElement(Spatials.positionXYZ(x, y, 1)
                .size(width, m.unlockBoxHeight), GreedTheme.PLATE, GreedTheme.GOLD_DEEP));
        int targetRank = rank + 1;
        if (ClientMilestoneData.isTrialReady()) {
            GreedButtonElement take = new GreedTrialButtonElement(
                    Spatials.positionXYZ(x + 3, y + 3, 2).size(width - 6, m.unlockBoxHeight - 6),
                    () -> GreedTheme.lang("take_trial"),
                    () -> NetworkHandler.INSTANCE.sendToServer(new ServerboundTakeTrialMessage()));
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
