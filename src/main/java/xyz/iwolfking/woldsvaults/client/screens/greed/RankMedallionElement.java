package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntSupplier;

/**
 * The rank emblem on the main greed screen. It blits the 16x16 medallion item art for the rank,
 * scaled up to the badge size with no filtering, which is the chunky look the rest of the pack's
 * item art has. Ranks past Legend keep the Legend medallion.
 *
 * <p>If the art is missing from the resource manager the element falls back to the drawn badge -
 * a gold ring around a dark disc carrying the rank's short label - and logs the missing path once
 * per path. The lookup is cached, so a resource pack reload does not re-check it.</p>
 */
public class RankMedallionElement extends AbstractSpatialElement<RankMedallionElement> implements IRenderedElement {
    private static final String TEXTURE_PREFIX = "textures/item/greed_medallion_";
    private static final Map<ResourceLocation, Boolean> PRESENCE = new HashMap<>();

    private final IntSupplier rank;
    private boolean visible = true;

    public RankMedallionElement(ISpatial spatial, IntSupplier rank) {
        super(spatial);
        this.rank = rank;
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
        ISpatial world = this.getWorldSpatial();
        int currentRank = this.rank.getAsInt();
        ResourceLocation texture = resolveTexture(currentRank);
        if (texture != null) {
            drawMedallion(poseStack, texture, world);
            return;
        }
        drawFallback(renderer, poseStack, world, currentRank);
    }

    private static ResourceLocation resolveTexture(int rank) {
        Optional<GreedMedallionTier> tier = GreedMedallionTier.byRankIndex(Math.min(rank, MilestoneRankLadder.LEGEND_RANK));
        if (tier.isEmpty()) {
            return null;
        }
        ResourceLocation location = WoldsVaults.id(TEXTURE_PREFIX + tier.get().getPathName() + ".png");
        Boolean cached = PRESENCE.get(location);
        if (cached == null) {
            cached = Minecraft.getInstance().getResourceManager().hasResource(location);
            PRESENCE.put(location, cached);
            if (!cached) {
                WoldsVaults.LOGGER.warn("Greed rank medallion texture {} is missing; drawing the fallback badge instead", location);
            }
        }
        return cached ? location : null;
    }

    private static void drawMedallion(PoseStack poseStack, ResourceLocation texture, ISpatial world) {
        int size = Math.min(world.width(), world.height());
        int x = world.x() + (world.width() - size) / 2;
        int y = world.y() + (world.height() - size) / 2;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, world.z());
        GuiComponent.blit(poseStack, x, y, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
        poseStack.popPose();
    }

    private static void drawFallback(IElementRenderer renderer, PoseStack poseStack, ISpatial world, int rank) {
        int diameter = Math.min(world.width(), world.height());
        int radius = diameter / 2;
        int centerX = world.x() + world.width() / 2;
        int centerY = world.y() + world.height() / 2;
        drawDisc(renderer, poseStack, centerX, centerY, radius, world.z(), GreedTheme.GOLD);
        drawDisc(renderer, poseStack, centerX, centerY, radius - 2, world.z() + 1, GreedTheme.GOLD_DEEP);
        drawDisc(renderer, poseStack, centerX, centerY, radius - 3, world.z() + 2, GreedTheme.PLATE_DARK);
        Component glyph = new TextComponent(GreedTheme.rankMedallionGlyph(rank));
        Minecraft minecraft = Minecraft.getInstance();
        int textWidth = minecraft.font.width(glyph);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, world.z() + 3.0D);
        minecraft.font.drawShadow(poseStack, glyph, centerX - textWidth / 2.0F, centerY - 4.0F, GreedTheme.GOLD);
        poseStack.popPose();
    }

    private static void drawDisc(IElementRenderer renderer, PoseStack poseStack, int centerX, int centerY, int radius, int z, int color) {
        if (radius <= 0) {
            return;
        }
        for (int dy = -radius; dy <= radius; dy++) {
            int half = (int) Math.floor(Math.sqrt((double) radius * radius - (double) dy * dy));
            if (half <= 0) {
                continue;
            }
            renderer.renderColoredQuad(poseStack, color,
                    Spatials.positionXYZ(centerX - half, centerY + dy, z).size(half * 2, 1));
        }
    }
}
