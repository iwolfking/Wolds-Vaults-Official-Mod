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
 * A milestone's tiers as a row of chips, each showing its threshold: gold once reached, the one being worked on
 * filling up inside a gold outline, the rest dark. The rep each tier pays sits under its chip, gold while it waits to be
 * claimed and ticked once collected.
 */
public class GreedTierChipsElement extends AbstractSpatialElement<GreedTierChipsElement> implements IRenderedElement {
    private static final int CHIP_HEIGHT = 11;
    private static final int GAP = 4;

    private final MilestoneDefinition definition;
    private final int completed;
    private final int claimed;
    private final float fraction;
    private boolean visible = true;

    public GreedTierChipsElement(ISpatial spatial, MilestoneDefinition definition, int completed, int claimed, float fraction) {
        super(spatial);
        this.definition = definition;
        this.completed = completed;
        this.claimed = claimed;
        this.fraction = fraction;
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
        int tiers = this.definition.getTierCount();
        if (tiers <= 0) {
            return;
        }
        int z = at.z();
        int chip = Math.min(46, (at.width() - GAP * (tiers - 1)) / tiers);
        for (int tier = 0; tier < tiers; tier++) {
            int x = at.x() + tier * (chip + GAP);
            int y = at.y();
            int textColour;
            if (tier < this.completed) {
                GreedPixelArt.bevel(renderer, poseStack, x, y, z, chip, CHIP_HEIGHT, 0xFF8A6A22, GreedTheme.GOLD, 0xFF4A3608, 0xFF1A1205);
                textColour = GreedPixelArt.CREAM;
            } else if (tier == this.completed) {
                GreedPixelArt.fill(renderer, poseStack, x, y, z, chip, CHIP_HEIGHT, GreedPixelArt.NAVY_OUTLINE);
                GreedPixelArt.fill(renderer, poseStack, x + 1, y + 1, z, chip - 2, CHIP_HEIGHT - 2, GreedPixelArt.NAVY_TRACK);
                int filled = Math.round((chip - 2) * Math.max(0.0F, Math.min(1.0F, this.fraction)));
                GreedPixelArt.fill(renderer, poseStack, x + 1, y + 1, z + 1, filled, CHIP_HEIGHT - 2, 0xFF6E551B);
                GreedPixelArt.hollow(renderer, poseStack, x, y, z + 1, chip, CHIP_HEIGHT, GreedTheme.GOLD);
                textColour = 0xFFFFFFFF;
            } else {
                GreedPixelArt.bevel(renderer, poseStack, x, y, z, chip, CHIP_HEIGHT, 0xFF161B26, 0xFF202736,
                        GreedPixelArt.NAVY_DARK, GreedPixelArt.NAVY_OUTLINE);
                textColour = GreedPixelArt.NAVY_DIM;
            }
            Component threshold = GreedTheme.text(this.format(this.definition.getThreshold(tier)), 0xFFFFFFFF);
            this.centred(poseStack, threshold, x, chip, y + 2, z + 2, textColour);

            boolean collected = tier < this.claimed && tier < this.completed;
            boolean waiting = tier >= this.claimed && tier < this.completed;
            Component rep = GreedTheme.text("+" + this.definition.getReputation(tier), 0xFFFFFFFF);
            if (collected) {
                int width = GreedPixelArt.width(rep, 1) + 1 + GreedPixelArt.CHECK.width();
                int left = x + (chip - width) / 2;
                GreedPixelArt.text(poseStack, rep, left, y + CHIP_HEIGHT + 3, z + 2, GreedPixelArt.NAVY_DIM, 1);
                GreedPixelArt.sprite(renderer, poseStack, GreedPixelArt.CHECK, left + width - GreedPixelArt.CHECK.width(),
                        y + CHIP_HEIGHT + 4, z + 2, null);
            } else {
                this.centred(poseStack, rep, x, chip, y + CHIP_HEIGHT + 3, z + 2, waiting ? GreedTheme.GOLD : GreedPixelArt.NAVY_DIM);
            }
        }
    }

    private void centred(PoseStack poseStack, Component text, int x, int width, int y, int z, int colour) {
        GreedPixelArt.text(poseStack, text, x + (width - GreedPixelArt.width(text, 1)) / 2.0F, y, z, colour, 1);
    }

    private String format(long amount) {
        return this.definition.isDuration() ? GreedTheme.duration(amount) : GreedTheme.compact(amount);
    }
}
