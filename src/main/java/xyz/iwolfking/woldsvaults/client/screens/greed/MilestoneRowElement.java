package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.iwolfking.woldsvaults.milestones.MilestoneDefinition;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundUnpinMilestoneMessage;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundClaimMilestoneMessage;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundPinMilestoneMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * One milestone row, laid out in one of the {@link GreedRowStyle}s. Classic puts the pin toggle, banked reputation
 * and claim button on the left chip, with the name, requirement, threshold and reputation lists and a within-tier
 * progress bar in the body. The rest are navy: the same facts arranged around reward chips, tier ladders, badges,
 * chips and claim buttons that say what they pay.
 */
public class MilestoneRowElement extends ContainerElement<MilestoneRowElement> {
    private static final int Z = 2;

    private final MilestoneDefinition definition;
    private final GreedMetrics metrics;
    private final GreedRowStyle style;
    private final int width;
    private final int height;
    private final boolean claimEnabled;
    private final boolean showClaim;
    private final Runnable onChanged;
    private final Font font = Minecraft.getInstance().font;

    private final long value;
    private final int completed;
    private final int tiers;
    private final boolean finished;
    private final int unclaimed;
    private final int claimedTiers;
    private final boolean pinned;
    private final int rarity;
    private final float fraction;
    private final long target;

    public MilestoneRowElement(int x, int y, int width, GreedMetrics metrics, GreedRowStyle style, int index,
                               MilestoneDefinition definition, boolean claimEnabled, boolean showClaim,
                               Runnable onChanged) {
        super(Spatials.positionXY(x, y).size(width, style.height(metrics.rowHeight)));
        this.definition = definition;
        this.metrics = metrics;
        this.style = style;
        this.width = width;
        this.height = style.height(metrics.rowHeight);
        this.claimEnabled = claimEnabled;
        this.showClaim = showClaim;
        this.onChanged = onChanged;

        this.value = ClientMilestoneData.getValue(definition.getId());
        this.tiers = definition.getTierCount();
        this.completed = definition.getCompletedTiers(this.value);
        this.finished = this.completed >= this.tiers;
        this.unclaimed = ClientMilestoneData.getUnclaimedRep(definition.getId());
        this.claimedTiers = ClientMilestoneData.getClaimedTiers(definition.getId());
        this.pinned = ClientMilestoneData.getPinned().contains(definition.getId());
        this.rarity = MilestoneFrame.rarity(this.completed, this.tiers);
        this.target = this.finished ? definition.getFinalThreshold() : definition.getThreshold(this.completed);
        if (this.finished) {
            this.fraction = 1.0F;
        } else {
            long floor = this.completed == 0 ? 0L : definition.getThreshold(this.completed - 1);
            long span = this.target - floor;
            this.fraction = span <= 0L ? 0.0F : Math.max(0.0F, Math.min(1.0F, (float) (this.value - floor) / (float) span));
        }

        this.buildBackground(index);
        switch (style) {
            case NAVY -> this.buildNavy();
            case REWARD_CHIP -> this.buildRewardChip();
            case ICON_CHIP -> this.buildIconChip();
            case TIER_LADDER -> this.buildTierLadder();
            case TIER_BADGE -> this.buildTierBadge();
            case COMPACT -> this.buildCompact();
            case BIG_CLAIM -> this.buildBigClaim();
            case TIER_CHIPS -> this.buildTierChips();
            case ADVANCEMENT -> this.buildAdvancement();
            case LEDGER -> this.buildLedger();
            case FOLDER_CARD -> this.buildFolderCard();
            default -> this.buildClassic();
        }
    }

    private void buildBackground(int index) {
        ISpatial whole = Spatials.positionXYZ(0, 0, 0).size(this.width, this.height);
        if (this.style == GreedRowStyle.LEDGER) {
            this.addElement(new GreedFillElement(whole, index % 2 == 1 ? GreedPixelArt.NAVY_ROW_ALT : GreedPixelArt.NAVY_ROW));
            if (this.rarity > MilestoneFrame.NONE) {
                this.addElement(new GreedFillElement(Spatials.positionXYZ(0, 0, 1).size(2, this.height),
                        0xFF000000 | MilestoneFrame.colour(this.rarity)));
            }
            return;
        }
        if (this.rarity > MilestoneFrame.NONE) {
            this.addElement(new MilestoneFrameElement(whole, this.rarity, this.definition.getId().hashCode(),
                    this.style.navy(), this.style.ornaments()));
        } else if (this.style.navy()) {
            this.addElement(GreedBevelElement.navyRow(whole));
        } else {
            this.addElement(new GreedFillElement(whole, GreedTheme.PLATE, GreedTheme.BORDER));
        }
        if (this.style == GreedRowStyle.FOLDER_CARD) {
            int inset = MilestoneFrame.borderWidth(this.rarity);
            this.addElement(new GreedBevelElement(Spatials.positionXYZ(inset, inset, 1).size(this.width - inset * 2, 14 - inset),
                    GreedPixelArt.NAVY_FACE, GreedPixelArt.NAVY_HIGHLIGHT, GreedPixelArt.NAVY_DARK, GreedPixelArt.NAVY_FACE));
        }
    }

    private void buildClassic() {
        GreedButtonElement pin = this.pinButton(Spatials.positionXYZ(2, this.metrics.rowPinY, 1)
                .size(this.metrics.rowPinWidth, this.metrics.rowPinHeight), GreedButtonElement.Look.FLAT);
        pin.setHighlighted(() -> this.pinned);
        this.label(2, this.metrics.rowRepY, this.metrics.rowChipWidth - 4,
                GreedTheme.langColored("row.rep", this.unclaimed > 0 ? GreedTheme.GOLD : GreedTheme.TEXT_DIM, this.unclaimed));
        this.claimButton(Spatials.positionXYZ(2, this.metrics.rowClaimY, 1)
                .size(this.metrics.rowChipWidth - 4, this.metrics.rowClaimHeight), () -> GreedTheme.lang("claim"), GreedButtonElement.Look.FLAT);

        int bodyX = this.metrics.rowChipWidth + 6;
        int bodyWidth = this.width - bodyX - 4;
        this.title(bodyX, this.metrics.rowTitleY, bodyWidth, this.finished ? GreedTheme.GOLD : GreedTheme.TEXT_TITLE, GreedTheme.TEXT_DIM);
        this.label(bodyX, this.metrics.rowTierY, bodyWidth, this.tierLine(GreedTheme.TEXT, GreedTheme.TEXT_DIM, " | "));

        int numeralWidth = this.metrics.rowNumeralWidth;
        int barX = bodyX + numeralWidth + 2;
        int barWidth = bodyWidth - (numeralWidth + 2) * 2;
        this.centred(bodyX, numeralWidth, this.metrics.rowBarY, GreedTheme.text(GreedTheme.roman(this.completed), GreedTheme.GOLD));
        float shown = this.fraction;
        GreedProgressBarElement bar = new GreedProgressBarElement(
                Spatials.positionXYZ(barX, this.metrics.rowBarY, 1).size(barWidth, this.metrics.rowBarHeight),
                () -> shown, null, this.finished ? GreedTheme.GOLD : GreedTheme.GOLD_DIM);
        this.barTooltip(bar);
        this.addElement(bar);
        this.centred(barX + barWidth + 2, numeralWidth, this.metrics.rowBarY,
                GreedTheme.text(this.finished ? GreedTheme.roman(this.completed) : GreedTheme.roman(this.completed + 1),
                        this.finished ? GreedTheme.GOLD : GreedTheme.TEXT_DIM));
    }

    private void buildNavy() {
        this.pinButton(Spatials.positionXYZ(4, 4, 1).size(32, 11), GreedButtonElement.Look.NAVY);
        if (this.unclaimed > 0) {
            this.label(5, 17, 50, GreedTheme.langColored("row.rep", GreedTheme.GOLD, this.unclaimed));
        }
        this.claimButton(Spatials.positionXYZ(4, 26, 1).size(50, 11), () -> GreedTheme.lang("claim"), GreedButtonElement.Look.NAVY);
        this.title(60, 5, this.width - 66, 0xFFFFFFFF, GreedPixelArt.NAVY_DIM);
        this.label(60, 15, this.width - 66, this.tierLine(GreedPixelArt.NAVY_TEXT, GreedPixelArt.NAVY_DIM, "  "));
        this.bar(60, 26, this.width - 66, 11, true);
    }

    private void buildRewardChip() {
        ISpatial chip = Spatials.positionXYZ(4, 4, 1).size(46, this.height - 8);
        if (this.unclaimed > 0 || !this.finished) {
            boolean waiting = this.unclaimed > 0;
            GreedButtonElement button = this.claimButton(chip,
                    () -> GreedTheme.text("+" + (waiting ? this.unclaimed : this.definition.getReputation(this.completed)), 0xFFFFFFFF),
                    GreedButtonElement.Look.NAVY);
            if (button != null) {
                button.labelScale(2).subLabel(GreedTheme.lang(waiting ? "claim" : "row.next"));
            }
        } else {
            this.doneButton(chip, GreedTheme.lang("row.done"));
        }
        this.pinButton(Spatials.positionXYZ(this.width - 11, 4, 1).size(7, 9), GreedButtonElement.Look.BARE);
        this.title(56, 5, this.width - 56 - 14, 0xFFFFFFFF, GreedPixelArt.NAVY_DIM);
        this.label(56, 15, this.width - 62, this.tierLine(GreedPixelArt.NAVY_TEXT, GreedPixelArt.NAVY_DIM, "  "));
        this.bar(56, 26, this.width - 62, 11, true);
    }

    private void buildIconChip() {
        ISpatial chip = Spatials.positionXYZ(4, 4, 1).size(46, this.height - 8);
        if (this.claimEnabled && this.showClaim && this.unclaimed > 0) {
            GreedButtonElement button = this.claimButton(chip, () -> GreedTheme.text("+" + this.unclaimed, 0xFFFFFFFF),
                    GreedButtonElement.Look.NAVY);
            if (button != null) {
                button.labelScale(2).subLabel(GreedTheme.lang("claim"));
            }
        } else {
            boolean waiting = this.unclaimed > 0;
            GreedItemIconElement icon = new GreedItemIconElement(chip, GreedMilestoneIcons.icon(this.definition.getId()),
                    waiting ? GreedTheme.text("+" + this.unclaimed, GreedTheme.GOLD) : null);
            icon.tooltip(Tooltips.multi(() -> {
                List<Component> lines = new ArrayList<>(this.tooltipLines());
                if (waiting) {
                    lines.add(GreedTheme.langColored("row.rep", GreedTheme.GOLD, this.unclaimed));
                    lines.add(GreedTheme.langColored("claim.tooltip.player", GreedTheme.TEXT_DIM));
                }
                return lines;
            }));
            this.addElement(icon);
        }
        this.pinButton(Spatials.positionXYZ(this.width - 11, 4, 1).size(7, 9), GreedButtonElement.Look.BARE);
        this.title(56, 6, this.width - 56 - 14, 0xFFFFFFFF, GreedPixelArt.NAVY_DIM);
        this.label(56, 17, this.width - 62, this.tierLine(GreedPixelArt.NAVY_TEXT, GreedPixelArt.NAVY_DIM, "  "));
        this.bar(56, 29, this.width - 62, 11, true);
    }

    private void buildTierLadder() {
        this.pinButton(Spatials.positionXYZ(6, 6, 1).size(7, 9), GreedButtonElement.Look.BARE);
        if (this.completed > 0) {
            this.centred(4, 44, 16, GreedTheme.langColored("row.tier", GreedTheme.GOLD, GreedTheme.roman(this.completed)));
        }
        this.claimButton(Spatials.positionXYZ(4, this.height - 16, 1).size(44, 12),
                () -> this.unclaimed > 0 ? GreedTheme.text("+" + this.unclaimed, 0xFFFFFFFF) : GreedTheme.lang("claim"),
                GreedButtonElement.Look.NAVY);
        Component progress = this.progressText(false);
        int progressWidth = this.font.width(progress);
        this.title(54, 5, this.width - 54 - progressWidth - 12, 0xFFFFFFFF, GreedPixelArt.NAVY_DIM);
        this.right(this.width - 6, 5, progress.copy().setStyle(Style.EMPTY.withColor(this.finished ? GreedPixelArt.GREEN : GreedPixelArt.NAVY_TEXT)));
        this.addElement(new GreedTierBarElement(Spatials.positionXYZ(54, 16, 1).size(this.width - 60, 24), this.definition,
                this.value, this.completed, this.claimedTiers, this.fraction, 12, GreedTierBarElement.Labels.REP, true, null));
    }

    private void buildTierBadge() {
        this.addElement(new GreedTierBadgeElement(Spatials.positionXYZ(5, 5, 1).size(26, 28),
                GreedTierBadgeElement.Shape.SHIELD, this.rarity, this.completed));
        this.title(36, 5, this.width - 36 - 16, 0xFFFFFFFF, GreedPixelArt.NAVY_DIM);
        this.label(36, 15, this.width - 36 - 70, this.nextLine());
        int barWidth = Math.max(40, this.width - 36 - 130);
        this.bar(36, 28, barWidth, 5, false);
        this.label(36 + barWidth + 4, 26, 60, this.progressText(true).copy().setStyle(Style.EMPTY.withColor(GreedPixelArt.NAVY_TEXT)));
        this.pinButton(Spatials.positionXYZ(this.width - 11, 4, 1).size(7, 9), GreedButtonElement.Look.BARE);
        this.claimButton(Spatials.positionXYZ(this.width - 68, 24, 1).size(63, 12),
                () -> this.unclaimed > 0 ? GreedTheme.lang("row.claim_amount", this.unclaimed) : GreedTheme.lang("claim"),
                GreedButtonElement.Look.NAVY);
    }

    private void buildCompact() {
        this.pinButton(Spatials.positionXYZ(4, 5, 1).size(7, 9), GreedButtonElement.Look.BARE);
        this.addElement(new GreedTierBadgeElement(Spatials.positionXYZ(14, 3, 1).size(16, 12),
                GreedTierBadgeElement.Shape.TAG, this.rarity, this.completed));
        int claimX = this.width - 68;
        int barWidth = Math.max(60, Math.min(128, (this.width - 110) / 2));
        int barX = claimX - 4 - barWidth;
        Component name = this.fit(I18n.get(this.definition.getNameKey()), barX - 38, this.unclaimed > 0 ? GreedTheme.GOLD : 0xFFFFFFFF);
        LabelElement<?> title = this.label(35, 5, barX - 38, name);
        title.tooltip(Tooltips.multi(this::tooltipLines));
        this.bar(barX, 3, barWidth, 12, true);
        ISpatial claim = Spatials.positionXYZ(claimX, 3, 1).size(64, 12);
        if (this.finished && this.unclaimed <= 0) {
            this.doneButton(claim, null);
        } else {
            this.claimButton(claim, () -> GreedTheme.lang("row.rep", this.unclaimed), GreedButtonElement.Look.NAVY);
        }
    }

    private void buildBigClaim() {
        this.pinButton(Spatials.positionXYZ(6, 5, 1).size(7, 9), GreedButtonElement.Look.BARE);
        this.title(16, 5, this.width - 16 - 80, 0xFFFFFFFF, GreedPixelArt.NAVY_DIM);
        MutableComponent line = GreedTheme.langColored("row.tier_of", this.completed > 0 ? GreedTheme.GOLD : GreedPixelArt.NAVY_TEXT,
                this.completed, this.tiers);
        line.append(GreedTheme.text("  -  ", GreedPixelArt.NAVY_DIM));
        line.append(this.progressText(false).copy().setStyle(Style.EMPTY.withColor(GreedPixelArt.NAVY_DIM)));
        this.label(16, 16, this.width - 16 - 80, line);
        this.bar(16, 28, this.width - 16 - 82, 7, false);
        ISpatial button = Spatials.positionXYZ(this.width - 74, 4, 1).size(70, this.height - 8);
        if (this.unclaimed > 0) {
            GreedButtonElement claim = this.claimButton(button, () -> GreedTheme.lang("claim"), GreedButtonElement.Look.NAVY);
            if (claim != null) {
                claim.subLabel(GreedTheme.lang("row.rep", this.unclaimed));
            }
        } else if (this.finished) {
            this.doneButton(button, GreedTheme.lang("row.complete"));
        } else {
            this.addElement(new GreedButtonElement(button, () -> GreedTheme.lang("row.next"), () -> {
            }).look(GreedButtonElement.Look.NAVY).setDisabled(() -> true)
                    .subLabel(GreedTheme.lang("row.rep", this.definition.getReputation(this.completed))));
        }
    }

    private void buildTierChips() {
        this.pinButton(Spatials.positionXYZ(4, 4, 1).size(40, 11), GreedButtonElement.Look.NAVY);
        this.claimButton(Spatials.positionXYZ(4, this.height - 15, 1).size(40, 11),
                () -> this.unclaimed > 0 ? GreedTheme.text("+" + this.unclaimed, 0xFFFFFFFF) : GreedTheme.lang("claim"),
                GreedButtonElement.Look.NAVY);
        this.title(50, 5, this.width - 56, 0xFFFFFFFF, GreedPixelArt.NAVY_DIM);
        GreedTierChipsElement chips = new GreedTierChipsElement(Spatials.positionXYZ(50, 16, 1).size(this.width - 56, 24),
                this.definition, this.completed, this.claimedTiers, this.fraction);
        this.barTooltip(chips);
        this.addElement(chips);
    }

    private void buildAdvancement() {
        this.addElement(new GreedTierBadgeElement(Spatials.positionXYZ(4, 4, 1).size(28, 28),
                GreedTierBadgeElement.Shape.SQUARE, this.rarity, this.completed));
        this.pinButton(Spatials.positionXYZ(38, 4, 1).size(7, 9), GreedButtonElement.Look.BARE);
        Component progress = this.progressText(true);
        int progressWidth = this.font.width(progress);
        LabelElement<?> name = this.label(48, 5, this.width - 48 - 62 - progressWidth,
                this.fit(I18n.get(this.definition.getNameKey()), this.width - 48 - 62 - progressWidth, GreedTheme.GOLD));
        name.tooltip(Tooltips.multi(this::tooltipLines));
        this.right(this.width - 60, 5, progress.copy().setStyle(Style.EMPTY.withColor(this.finished ? GreedPixelArt.GREEN : GreedPixelArt.NAVY_TEXT)));
        this.label(38, 16, this.width - 38 - 60, this.fit(I18n.get(this.definition.getDescriptionKey()), this.width - 38 - 60, GreedPixelArt.NAVY_TEXT));
        this.bar(38, 28, this.width - 38 - 60, 4, false);
        GreedButtonElement coin = this.claimButton(Spatials.positionXYZ(this.width - 53, 5, 1).size(48, 26),
                () -> GreedTheme.text("+" + this.unclaimed, 0xFFFFFFFF), GreedButtonElement.Look.NAVY);
        if (coin != null) {
            coin.icon(GreedPixelArt.COIN, true, true);
        }
    }

    private void buildLedger() {
        int[] columns = ledgerColumns(this.width);
        this.pinButton(Spatials.positionXYZ(5, 4, 1).size(7, 9), GreedButtonElement.Look.BARE);
        int nameWidth = columns[0] - 15 - 4;
        LabelElement<?> name = this.label(15, 3, nameWidth, this.fit(I18n.get(this.definition.getNameKey()), nameWidth, 0xFFFFFFFF));
        name.tooltip(Tooltips.multi(this::tooltipLines));
        this.label(15, 13, nameWidth, this.fit(I18n.get(this.definition.getDescriptionKey()), nameWidth, GreedPixelArt.NAVY_DIM));
        this.centred(columns[0], 36, 8, GreedTheme.text(this.completed + "/" + this.tiers,
                this.completed > 0 ? GreedTheme.GOLD : GreedPixelArt.NAVY_TEXT));
        this.bar(columns[1], 4, 84, 7, false);
        this.centred(columns[1], 84, 13, this.finished ? GreedTheme.langColored("row.done", GreedPixelArt.NAVY_DIM)
                : this.progressText(true).copy().setStyle(Style.EMPTY.withColor(GreedPixelArt.NAVY_DIM)));
        this.centred(columns[2], 40, 8, GreedTheme.text(this.finished ? "-" : "+" + this.definition.getReputation(this.completed),
                GreedPixelArt.NAVY_TEXT));
        this.claimButton(Spatials.positionXYZ(columns[3], 5, 1).size(46, 14),
                () -> GreedTheme.text(this.unclaimed > 0 ? "+" + this.unclaimed : "-", 0xFFFFFFFF), GreedButtonElement.Look.NAVY);
    }

    private void buildFolderCard() {
        int inset = MilestoneFrame.borderWidth(this.rarity);
        this.title(inset + 4, 4, this.width - inset * 2 - 18, 0xFFFFFFFF, GreedPixelArt.NAVY_DIM);
        this.pinButton(Spatials.positionXYZ(this.width - inset - 10, 3, 2).size(7, 9), GreedButtonElement.Look.BARE);
        GreedTierBarElement bar = new GreedTierBarElement(Spatials.positionXYZ(inset + 3, 17, 1).size(this.width - inset * 2 - 6, 11),
                this.definition, this.value, this.completed, this.claimedTiers, this.fraction, 11,
                GreedTierBarElement.Labels.PROGRESS, false, this.progressText(false));
        this.barTooltip(bar);
        this.addElement(bar);
        this.label(inset + 4, 34, this.width - 150, this.nextLineShort());
        if (this.completed > 0) {
            this.right(this.width - 74, 34, GreedTheme.langColored("row.tier", GreedTheme.GOLD, GreedTheme.roman(this.completed)));
        }
        this.claimButton(Spatials.positionXYZ(this.width - inset - 66, 31, 1).size(64, 13),
                () -> this.unclaimed > 0 ? GreedTheme.lang("row.claim_amount", this.unclaimed) : GreedTheme.lang("claim"),
                GreedButtonElement.Look.NAVY);
    }

    private static int[] ledgerColumns(int width) {
        int claim = width - 50;
        int next = claim - 44;
        int progress = next - 88;
        int tier = progress - 40;
        return new int[]{tier, progress, next, claim};
    }

    public static final int LEDGER_HEADER_HEIGHT = 12;

    /** The ledger style's column headings, once above its rows. */
    public static ContainerElement<?> ledgerHeader(int x, int y, int width) {
        return new LedgerHeader(x, y, width);
    }

    private static final class LedgerHeader extends ContainerElement<LedgerHeader> {
        LedgerHeader(int x, int y, int width) {
            super(Spatials.positionXY(x, y).size(width, LEDGER_HEADER_HEIGHT));
            int[] columns = ledgerColumns(width);
            this.addElement(new GreedFillElement(Spatials.positionXYZ(0, 0, 1).size(width, LEDGER_HEADER_HEIGHT - 1), 0xFF0D1119));
            this.addElement(new GreedFillElement(Spatials.positionXYZ(0, LEDGER_HEADER_HEIGHT - 1, 1).size(width, 1), GreedPixelArt.NAVY_HIGHLIGHT));
            this.addElement(new LabelElement<>(Spatials.positionXYZ(15, 2, 2),
                    GreedTheme.langColored("ledger.milestone", GreedPixelArt.NAVY_DIM), LabelTextStyle.defaultStyle()));
            String[] keys = {"ledger.tier", "ledger.progress", "ledger.next", "ledger.claim"};
            int[] widths = {36, 84, 40, 46};
            for (int i = 0; i < keys.length; i++) {
                this.addElement(new LabelElement<>(Spatials.positionXYZ(columns[i], 2, 2), (ISize) Spatials.size(widths[i], 9),
                        GreedTheme.langColored(keys[i], GreedPixelArt.NAVY_DIM), LabelTextStyle.defaultStyle().center()));
            }
        }
    }

    private GreedButtonElement pinButton(ISpatial spatial, GreedButtonElement.Look look) {
        String id = this.definition.getId();
        boolean isPinned = this.pinned;
        Supplier<Component> text = look == GreedButtonElement.Look.BARE ? () -> null
                : () -> GreedTheme.lang(isPinned ? "pin.unpin" : "pin.pin");
        GreedButtonElement button = new GreedButtonElement(spatial, text, () -> {
            if (isPinned) {
                NetworkHandler.INSTANCE.sendToServer(new ServerboundUnpinMilestoneMessage(id));
                ClientMilestoneData.unpin(id);
            } else {
                NetworkHandler.INSTANCE.sendToServer(new ServerboundPinMilestoneMessage(id));
                ClientMilestoneData.pin(id);
            }
            this.onChanged.run();
        }).look(look);
        button.setHighlighted(() -> isPinned);
        if (look == GreedButtonElement.Look.BARE) {
            button.icon(GreedPixelArt.PIN, false, false);
        }
        button.tooltip(Tooltips.single(() -> GreedTheme.lang(isPinned ? "pin.unpin.tooltip" : "pin.pin.tooltip")));
        return this.addElement(button);
    }

    private GreedButtonElement claimButton(ISpatial spatial, Supplier<Component> text, GreedButtonElement.Look look) {
        if (!this.showClaim) {
            return null;
        }
        String id = this.definition.getId();
        boolean enabled = this.claimEnabled;
        GreedButtonElement button = new GreedButtonElement(spatial, text, () -> {
            NetworkHandler.INSTANCE.sendToServer(new ServerboundClaimMilestoneMessage(id));
            this.onChanged.run();
        }).look(look);
        button.setDisabled(() -> !enabled || ClientMilestoneData.getUnclaimedRep(id) <= 0);
        button.setHighlighted(() -> enabled && ClientMilestoneData.getUnclaimedRep(id) > 0);
        button.tooltip(Tooltips.multi(() -> enabled
                ? List.of(GreedTheme.lang("claim.tooltip"))
                : List.of(GreedTheme.lang("claim.tooltip.player"))));
        return this.addElement(button);
    }

    private void doneButton(ISpatial spatial, Component text) {
        this.addElement(new GreedButtonElement(spatial, () -> null, () -> {
        }).look(GreedButtonElement.Look.NAVY).setDisabled(() -> true).icon(GreedPixelArt.CHECK, false, false)
                .subLabel(text == null ? null : text.copy().setStyle(Style.EMPTY.withColor(GreedPixelArt.GREEN))));
    }

    private void bar(int x, int y, int w, int h, boolean caption) {
        float shown = this.fraction;
        Component text = this.progressText(false);
        GreedProgressBarElement bar = new GreedProgressBarElement(Spatials.positionXYZ(x, y, 1).size(w, h),
                () -> shown, caption ? () -> text : null, GreedTheme.GOLD_DIM).molten().navy();
        if (caption && this.style.ornaments()) {
            int crest = MilestoneFrame.crestWidth(this.rarity);
            int span = crest > 0 ? crest : this.width;
            bar.captionOver((this.width - span) / 2 - x, span);
        }
        this.barTooltip(bar);
        this.addElement(bar);
    }

    private void barTooltip(GreedProgressBarElement bar) {
        if (this.definition.isDuration()) {
            bar.durationTooltip(this.value, this.target);
        } else {
            bar.progressTooltip(this.value, this.target);
        }
    }

    private void barTooltip(GreedTierBarElement bar) {
        bar.tooltip(Tooltips.single(() -> this.definition.isDuration()
                ? GreedTheme.durationProgress(this.value, this.target) : GreedTheme.progress(this.value, this.target)));
    }

    private void barTooltip(GreedTierChipsElement chips) {
        chips.tooltip(Tooltips.single(() -> this.definition.isDuration()
                ? GreedTheme.durationProgress(this.value, this.target) : GreedTheme.progress(this.value, this.target)));
    }

    private LabelElement<?> label(int x, int y, int w, Component text) {
        return this.addElement(new LabelElement<>(Spatials.positionXYZ(x, y, Z), (ISize) Spatials.size(Math.max(1, w), 9),
                text, LabelTextStyle.defaultStyle()));
    }

    private void centred(int x, int w, int y, Component text) {
        this.addElement(new LabelElement<>(Spatials.positionXYZ(x, y, Z), (ISize) Spatials.size(w, 9), text,
                LabelTextStyle.defaultStyle().center()));
    }

    private void right(int right, int y, Component text) {
        int w = this.font.width(text);
        this.label(right - w, y, w + 1, text);
    }

    private String format(long amount) {
        return this.definition.isDuration() ? GreedTheme.duration(amount) : GreedTheme.compact(amount);
    }

    private Component progressText(boolean tight) {
        if (this.finished) {
            return GreedTheme.lang("row.complete");
        }
        String separator = tight ? "/" : " / ";
        return new TextComponent(this.format(Math.min(this.value, this.target)) + separator + this.format(this.target));
    }

    private Component fit(String text, int room, int colour) {
        if (this.font.width(text) > room) {
            String ellipsis = "...";
            text = room <= this.font.width(ellipsis) ? "" : this.font.plainSubstrByWidth(text, room - this.font.width(ellipsis)) + ellipsis;
        }
        return GreedTheme.text(text, colour);
    }

    /** Name and requirement on one line, the requirement ellipsised to the room the name leaves, with a tooltip. */
    private void title(int x, int y, int w, int nameColour, int dimColour) {
        String name = I18n.get(this.definition.getNameKey());
        String requirement = I18n.get(this.definition.getDescriptionKey());
        String separator = " - ";
        int available = w - this.font.width(name) - this.font.width(separator);
        if (available < this.font.width(requirement)) {
            String ellipsis = "...";
            int room = available - this.font.width(ellipsis);
            requirement = room <= 0 ? "" : this.font.plainSubstrByWidth(requirement, room) + ellipsis;
        }
        MutableComponent line = GreedTheme.text(name, nameColour);
        if (!requirement.isEmpty()) {
            line.append(GreedTheme.text(separator + requirement, dimColour));
        }
        LabelElement<?> title = this.label(x, y, w, line);
        title.tooltip(Tooltips.multi(this::tooltipLines));
    }

    private List<Component> tooltipLines() {
        return List.of(
                new TranslatableComponent(this.definition.getNameKey()).setStyle(Style.EMPTY.withColor(GreedTheme.GOLD)),
                new TranslatableComponent(this.definition.getDescriptionKey()).setStyle(Style.EMPTY.withColor(GreedTheme.TEXT_DIM)),
                this.tierLine(GreedTheme.TEXT, GreedTheme.TEXT_DIM, " | "));
    }

    private Component tierLine(int colour, int dimColour, String divider) {
        MutableComponent line = new TextComponent("");
        for (int tier = 0; tier < this.tiers; tier++) {
            if (tier > 0) {
                line.append(GreedTheme.text("/", dimColour));
            }
            boolean current = !this.finished && tier == this.completed;
            line.append(GreedTheme.text(this.format(this.definition.getThreshold(tier)), current ? GreedTheme.GOLD : colour));
        }
        line.append(GreedTheme.text(divider, dimColour));
        line.append(GreedTheme.langColored("row.rep_list", dimColour));
        for (int tier = 0; tier < this.tiers; tier++) {
            if (tier > 0) {
                line.append(GreedTheme.text("/", dimColour));
            }
            boolean current = !this.finished && tier == this.completed;
            line.append(GreedTheme.text(String.valueOf(this.definition.getReputation(tier)), current ? GreedTheme.GOLD : colour));
        }
        return line;
    }

    private Component nextLine() {
        if (this.finished) {
            return GreedTheme.langColored("row.all_complete", GreedPixelArt.GREEN);
        }
        MutableComponent line = GreedTheme.langColored("row.next_label", GreedPixelArt.NAVY_DIM);
        line.append(GreedTheme.text(this.format(this.target), 0xFFFFFFFF));
        line.append(GreedTheme.langColored("row.for", GreedPixelArt.NAVY_DIM));
        line.append(GreedTheme.langColored("row.rep", GreedTheme.GOLD, this.definition.getReputation(this.completed)));
        return line;
    }

    private Component nextLineShort() {
        if (this.finished) {
            return GreedTheme.langColored("row.all_complete", GreedPixelArt.GREEN);
        }
        MutableComponent line = GreedTheme.langColored("row.next", GreedPixelArt.NAVY_DIM);
        line.append(GreedTheme.text(" ", GreedPixelArt.NAVY_DIM));
        line.append(GreedTheme.langColored("row.rep", GreedTheme.GOLD, this.definition.getReputation(this.completed)));
        line.append(GreedTheme.langColored("row.at", GreedPixelArt.NAVY_DIM, this.format(this.target)));
        return line;
    }
}
