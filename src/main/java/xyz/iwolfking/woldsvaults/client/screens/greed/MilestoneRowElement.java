package xyz.iwolfking.woldsvaults.client.screens.greed;

import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
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
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundClaimMilestoneMessage;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundPinMilestoneMessage;

import java.util.List;

/**
 * One achievement row: pin toggle, banked reputation and claim button on the left chip; name,
 * requirement, threshold and reputation lists and a within-tier progress bar in the body.
 */
public class MilestoneRowElement extends ContainerElement<MilestoneRowElement> {
    private final MilestoneDefinition definition;
    private final GreedMetrics metrics;

    public MilestoneRowElement(int x, int y, int width, GreedMetrics metrics, MilestoneDefinition definition,
                               boolean claimEnabled, boolean showClaim, Runnable onChanged) {
        super(Spatials.positionXY(x, y).size(width, metrics.rowHeight));
        this.definition = definition;
        this.metrics = metrics;

        long value = ClientMilestoneData.getValue(definition.getId());
        int completed = definition.getCompletedTiers(value);
        boolean finished = completed >= definition.getTierCount();
        int unclaimed = ClientMilestoneData.getUnclaimedRep(definition.getId());
        boolean pinned = definition.getId().equals(ClientMilestoneData.getPinned());

        this.addElement(new GreedFillElement(Spatials.positionXYZ(0, 0, 0).size(width, metrics.rowHeight),
                finished ? GreedTheme.PLATE_LIGHT : GreedTheme.PLATE,
                finished ? GreedTheme.GOLD : GreedTheme.BORDER));

        this.buildChip(claimEnabled, showClaim, unclaimed, pinned, onChanged);

        int bodyX = metrics.rowChipWidth + 6;
        int bodyWidth = width - bodyX - 4;
        var title = new LabelElement<>(Spatials.positionXYZ(bodyX, metrics.rowTitleY, 1),
                (ISize) Spatials.size(bodyWidth, 9),
                this.buildTitleLine(finished, bodyWidth), LabelTextStyle.defaultStyle());
        title.tooltip(Tooltips.multi(() -> List.of(
                new TranslatableComponent(definition.getNameKey()).setStyle(Style.EMPTY.withColor(GreedTheme.GOLD)),
                new TranslatableComponent(definition.getDescriptionKey()).setStyle(Style.EMPTY.withColor(GreedTheme.TEXT_DIM)))));
        this.addElement(title);
        this.addElement(new LabelElement<>(Spatials.positionXYZ(bodyX, metrics.rowTierY, 1),
                (ISize) Spatials.size(bodyWidth, 9),
                this.buildTierLine(completed, finished), LabelTextStyle.defaultStyle()));

        this.buildProgress(bodyX, bodyWidth, value, completed, finished);
    }

    private void buildChip(boolean claimEnabled, boolean showClaim, int unclaimed, boolean pinned, Runnable onChanged) {
        String id = this.definition.getId();
        GreedButtonElement pinButton = new GreedButtonElement(
                Spatials.positionXYZ(2, this.metrics.rowPinY, 1).size(this.metrics.rowPinWidth, this.metrics.rowPinHeight),
                () -> GreedTheme.lang(pinned ? "pin.unpin" : "pin.pin"),
                () -> {
                    NetworkHandler.INSTANCE.sendToServer(new ServerboundPinMilestoneMessage(pinned ? ServerboundPinMilestoneMessage.UNPIN : id));
                    ClientMilestoneData.setPinned(pinned ? null : id);
                    onChanged.run();
                });
        pinButton.setHighlighted(() -> pinned);
        pinButton.tooltip(Tooltips.single(() -> GreedTheme.lang(pinned ? "pin.unpin.tooltip" : "pin.pin.tooltip")));
        this.addElement(pinButton);

        this.addElement(new LabelElement<>(Spatials.positionXYZ(2, this.metrics.rowRepY, 1),
                (ISize) Spatials.size(this.metrics.rowChipWidth - 4, 9),
                GreedTheme.langColored("row.rep", unclaimed > 0 ? GreedTheme.GOLD : GreedTheme.TEXT_DIM, unclaimed),
                LabelTextStyle.defaultStyle()));

        if (!showClaim) {
            return;
        }
        GreedButtonElement claimButton = new GreedButtonElement(
                Spatials.positionXYZ(2, this.metrics.rowClaimY, 1)
                        .size(this.metrics.rowChipWidth - 4, this.metrics.rowClaimHeight),
                () -> GreedTheme.lang("claim"),
                () -> {
                    NetworkHandler.INSTANCE.sendToServer(new ServerboundClaimMilestoneMessage(id));
                    onChanged.run();
                });
        claimButton.setDisabled(() -> !claimEnabled || ClientMilestoneData.getUnclaimedRep(id) <= 0);
        claimButton.setHighlighted(() -> claimEnabled && ClientMilestoneData.getUnclaimedRep(id) > 0);
        claimButton.tooltip(Tooltips.multi(() -> claimEnabled
                ? List.of(GreedTheme.lang("claim.tooltip"))
                : List.of(GreedTheme.lang("claim.tooltip.player"))));
        this.addElement(claimButton);
    }

    /** Name and requirement on one line, with the requirement ellipsised to the room the name leaves. */
    private Component buildTitleLine(boolean finished, int bodyWidth) {
        Font font = Minecraft.getInstance().font;
        String name = I18n.get(this.definition.getNameKey());
        String requirement = I18n.get(this.definition.getDescriptionKey());
        String separator = " - ";
        int available = bodyWidth - font.width(name) - font.width(separator);
        if (available < font.width(requirement)) {
            String ellipsis = "...";
            int room = available - font.width(ellipsis);
            requirement = room <= 0 ? "" : font.plainSubstrByWidth(requirement, room) + ellipsis;
        }
        MutableComponent line = GreedTheme.text(name, finished ? GreedTheme.GOLD : GreedTheme.TEXT_TITLE);
        if (!requirement.isEmpty()) {
            line.append(GreedTheme.text(separator + requirement, GreedTheme.TEXT_DIM));
        }
        return line;
    }

    private Component buildTierLine(int completed, boolean finished) {
        MutableComponent line = new TextComponent("");
        for (int tier = 0; tier < this.definition.getTierCount(); tier++) {
            if (tier > 0) {
                line.append(GreedTheme.text("/", GreedTheme.TEXT_DIM));
            }
            boolean current = !finished && tier == completed;
            long threshold = this.definition.getThreshold(tier);
            line.append(GreedTheme.text(this.definition.isDuration()
                            ? GreedTheme.duration(threshold) : GreedTheme.compact(threshold),
                    current ? GreedTheme.GOLD : GreedTheme.TEXT));
        }
        line.append(GreedTheme.text(" | ", GreedTheme.TEXT_DIM));
        line.append(GreedTheme.langColored("row.rep_list", GreedTheme.TEXT_DIM));
        for (int tier = 0; tier < this.definition.getTierCount(); tier++) {
            if (tier > 0) {
                line.append(GreedTheme.text("/", GreedTheme.TEXT_DIM));
            }
            boolean current = !finished && tier == completed;
            line.append(GreedTheme.text(String.valueOf(this.definition.getReputation(tier)),
                    current ? GreedTheme.GOLD : GreedTheme.TEXT));
        }
        return line;
    }

    private void buildProgress(int bodyX, int bodyWidth, long value, int completed, boolean finished) {
        int numeralWidth = this.metrics.rowNumeralWidth;
        int barX = bodyX + numeralWidth + 2;
        int barWidth = bodyWidth - (numeralWidth + 2) * 2;
        float fraction;
        if (finished) {
            fraction = 1.0F;
        } else {
            long floor = completed == 0 ? 0L : this.definition.getThreshold(completed - 1);
            long ceiling = this.definition.getThreshold(completed);
            long span = ceiling - floor;
            fraction = span <= 0L ? 0.0F : (float) (value - floor) / (float) span;
        }
        float shown = fraction;

        this.addElement(new LabelElement<>(Spatials.positionXYZ(bodyX, this.metrics.rowBarY, 1),
                (ISize) Spatials.size(numeralWidth, 9),
                GreedTheme.text(GreedTheme.roman(completed), GreedTheme.GOLD), LabelTextStyle.defaultStyle().center()));
        long target = finished ? this.definition.getFinalThreshold() : this.definition.getThreshold(completed);
        GreedProgressBarElement bar = new GreedProgressBarElement(
                Spatials.positionXYZ(barX, this.metrics.rowBarY, 1).size(barWidth, this.metrics.rowBarHeight),
                () -> shown, null, finished ? GreedTheme.GOLD : GreedTheme.GOLD_DIM);
        if (this.definition.isDuration()) {
            bar.durationTooltip(value, target);
        } else {
            bar.progressTooltip(value, target);
        }
        this.addElement(bar);
        this.addElement(new LabelElement<>(Spatials.positionXYZ(barX + barWidth + 2, this.metrics.rowBarY, 1),
                (ISize) Spatials.size(numeralWidth, 9),
                GreedTheme.text(finished ? GreedTheme.roman(completed) : GreedTheme.roman(completed + 1),
                        finished ? GreedTheme.GOLD : GreedTheme.TEXT_DIM),
                LabelTextStyle.defaultStyle().center()));
    }
}
