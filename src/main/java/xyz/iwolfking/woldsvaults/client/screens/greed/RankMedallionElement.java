package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.mojang.blaze3d.platform.NativeImage;
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
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntSupplier;

/**
 * The rank emblem on the main greed screen. It blits the rank's medallion item art at whatever
 * square resolution the texture ships in, scaled to the badge size with no filtering, which is the
 * chunky look the rest of the pack's item art has. Ranks past Legend keep the Legend medallion.
 *
 * <p>If the art is missing from the resource manager the element falls back to the drawn badge -
 * a gold ring around a dark disc carrying the rank's short label - and logs the missing path once
 * per path. The probe (which also reads the texture's native size) is cached until the next
 * resource reload, so a pack that adds or fixes the art is picked up without a restart and a
 * probe that ran mid-reload cannot pin the fallback for the session.</p>
 */
public class RankMedallionElement extends AbstractSpatialElement<RankMedallionElement> implements IRenderedElement {
    private static final String TEXTURE_PREFIX = "textures/item/greed_medallion_";
    private static final Map<ResourceLocation, Integer> TEXTURE_SIZES = new HashMap<>();

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
        int textureSize = texture == null ? 0 : TEXTURE_SIZES.getOrDefault(texture, 0);
        if (texture != null && textureSize > 0) {
            drawMedallion(poseStack, texture, world, textureSize);
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
        Integer cached = TEXTURE_SIZES.get(location);
        if (cached == null) {
            cached = probeTextureSize(location);
            TEXTURE_SIZES.put(location, cached);
            if (cached == 0) {
                WoldsVaults.LOGGER.warn("Greed rank medallion texture {} is missing or unreadable; drawing the fallback badge instead", location);
            }
        }
        return cached > 0 ? location : null;
    }

    /** Forgets every probe when resources reload; the next render re-probes against the new packs. */
    @Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ReloadHook {
        private ReloadHook() {
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> TEXTURE_SIZES.clear());
        }
    }

    private static int probeTextureSize(ResourceLocation location) {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
             NativeImage image = NativeImage.read(stream)) {
            return image.getWidth();
        } catch (IOException e) {
            return 0;
        }
    }

    private static void drawMedallion(PoseStack poseStack, ResourceLocation texture, ISpatial world, int textureSize) {
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
        GuiComponent.blit(poseStack, x, y, size, size, 0.0F, 0.0F, textureSize, textureSize, textureSize, textureSize);
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
