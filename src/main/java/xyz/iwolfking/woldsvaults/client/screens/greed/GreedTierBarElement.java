package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.milestones.MilestoneDefinition;

/**
 * A milestone's progress bar split into one segment per tier, each filled as far as the milestone has got through it:
 * reached tiers full (dimmed once claimed, outlined in gold while their rep waits), the one being worked on in animated
 * molten gold, the rest empty. Segments can be labelled with the rep each tier pays or, on the one in progress, the
 * live numbers; tier thresholds can sit underneath, right-aligned to each segment's end.
 */
public class GreedTierBarElement extends AbstractSpatialElement<GreedTierBarElement> implements IRenderedElement {
    public enum Labels { REP, PROGRESS }

    private final MilestoneDefinition definition;
    private final long value;
    private final int completed;
    private final int claimed;
    private final float fraction;
    private final int barHeight;
    private final Labels labels;
    private final boolean thresholds;
    private final Component progress;
    private boolean visible = true;

    public GreedTierBarElement(ISpatial spatial, MilestoneDefinition definition, long value, int completed, int claimed,
                               float fraction, int barHeight, Labels labels, boolean thresholds, Component progress) {
        super(spatial);
        this.definition = definition;
        this.value = value;
        this.completed = completed;
        this.claimed = claimed;
        this.fraction = fraction;
        this.barHeight = barHeight;
        this.labels = labels;
        this.thresholds = thresholds;
        this.progress = progress;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ISpatial at = this.getWorldSpatial();
        int x = at.x();
        int y = at.y();
        int z = at.z();
        int tiers = this.definition.getTierCount();
        if (tiers <= 0) {
            return;
        }
        int segment = (at.width() - (tiers - 1)) / tiers;
        int h = this.barHeight;
        for (int tier = 0; tier < tiers; tier++) {
            int sx = x + tier * (segment + 1);
            int sw = tier == tiers - 1 ? x + at.width() - sx : segment;
            GreedPixelArt.fill(renderer, poseStack, sx, y, z, sw, h, GreedPixelArt.NAVY_OUTLINE);
            GreedPixelArt.fill(renderer, poseStack, sx + 1, y + 1, z, sw - 2, h - 2, GreedPixelArt.NAVY_TRACK);
            boolean reached = tier < this.completed;
            boolean waiting = reached && tier >= this.claimed;
            if (reached) {
                for (int row = 0; row < h - 2; row++) {
                    int colour = GreedPixelArt.moltenRow(row, h - 2);
                    GreedPixelArt.fill(renderer, poseStack, sx + 1, y + 1 + row, z + 1, sw - 2, 1,
                            waiting ? colour : GreedPixelArt.scale(colour, 0.75F));
                }
            } else if (tier == this.completed) {
                int filled = Math.round((sw - 2) * Math.max(0.0F, Math.min(1.0F, this.fraction)));
                GreedFlourish.drawMolten(poseStack, sx + 1, y + 1, z + 1, filled, sw - 2, h - 2);
            }
            if (waiting) {
                GreedPixelArt.hollow(renderer, poseStack, sx, y, z + 2, sw, h, GreedTheme.GOLD);
            }
            Component label = this.label(tier, reached);
            if (label != null) {
                int colour = reached ? GreedPixelArt.CREAM : tier == this.completed ? 0xFFFFFFFF : GreedPixelArt.NAVY_DIM;
                int width = GreedPixelArt.width(label, 1);
                GreedPixelArt.text(poseStack, label, sx + (sw - width) / 2.0F, y + 1 + (h - 9) / 2, z + 3, colour, 1);
            }
            if (this.thresholds) {
                Component threshold = GreedTheme.text(this.format(this.definition.getThreshold(tier)), 0xFFFFFFFF);
                int colour = reached ? GreedPixelArt.NAVY_TEXT : tier == this.completed ? GreedTheme.GOLD : GreedPixelArt.NAVY_DIM;
                GreedPixelArt.text(poseStack, threshold, sx + sw - GreedPixelArt.width(threshold, 1), y + h + 3, z + 3, colour, 1);
            }
        }
    }

    private Component label(int tier, boolean reached) {
        if (this.labels == Labels.REP) {
            return GreedTheme.text("+" + this.definition.getReputation(tier), 0xFFFFFFFF);
        }
        return tier == this.completed && !reached ? this.progress : null;
    }

    private String format(long amount) {
        return this.definition.isDuration() ? GreedTheme.duration(amount) : GreedTheme.compact(amount);
    }
}
