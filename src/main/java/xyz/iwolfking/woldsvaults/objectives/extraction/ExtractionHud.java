package xyz.iwolfking.woldsvaults.objectives.extraction;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import iskallia.vault.client.gui.helper.LightmapHelper;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.init.ModOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.Map;

/**
 * The extraction objective's HUD, drawn from the synced {@link ExtractionSite} fields only: the placed HUD (progress,
 * hull, shield, energy and the two thumper bars) near a drilling site, else one pip per site. An empty energy gauge is
 * drawn full in red.
 */
@OnlyIn(Dist.CLIENT)
public final class ExtractionHud {
    public static final ResourceLocation HUD = WoldsVaults.id("textures/gui/extraction/hud.png");
    private static final int PAD = 2;
    private static final float OUT_OF_POWER_RED = 1.0F;
    private static final float OUT_OF_POWER_GREEN_BLUE = 0.25F;
    private static final int PIP_UNTOUCHED = 0;
    private static final int PIP_IN_PROGRESS = 1;
    private static final int PIP_COMPLETE = 2;
    private static final Map<String, int[][]> PIP_UVS = Map.of(
            "velara", new int[][]{{169, 138}, {186, 138}, {203, 138}},
            "tenos", new int[][]{{220, 138}, {237, 138}, {0, 155}},
            "wendarr", new int[][]{{17, 155}, {34, 155}, {51, 155}},
            "idona", new int[][]{{68, 155}, {85, 155}, {102, 155}});

    private ExtractionHud() {
    }

    /** Draws the placed HUD near a drilling site, else the row of site pips; false when the vault has no sites. */
    public static boolean render(PoseStack matrixStack, ExtractionSite[] sites, int cellX, int cellZ, Player player, float partialTicks) {
        if (sites.length == 0) {
            return false;
        }
        ExtractionSite nearby = findNearbyDrillingSite(sites, Math.max(1, cellX), Math.max(1, cellZ), player);
        if (nearby != null) {
            renderPlacedHud(matrixStack, nearby, sites, ModOptions.ELIXIR_ORB_NUMBERS.getValue(), player, partialTicks);
        } else {
            renderIdleHud(matrixStack, sites);
        }
        return true;
    }

    private static ExtractionSite findNearbyDrillingSite(ExtractionSite[] sites, int cellX, int cellZ, Player player) {
        RegionPos playerRegion = RegionPos.ofBlockPos(player.blockPosition(), cellX, cellZ);
        for (ExtractionSite site : sites) {
            if (!site.isDrilling() || !site.isOccupied()) {
                continue;
            }
            int half = site.getSpan() / 2;
            int dx = Math.abs(playerRegion.getX() - (site.get(ExtractionSite.REGION_X) + half));
            int dz = Math.abs(playerRegion.getZ() - (site.get(ExtractionSite.REGION_Z) + half));
            if (dx <= 2 + half && dz <= 2 + half) {
                return site;
            }
        }
        return null;
    }

    private static void renderPlacedHud(PoseStack matrixStack, ExtractionSite site, ExtractionSite[] sites, boolean showNumbers, Player player, float partialTicks) {
        float progress = site.progressFraction();
        float hpFrac = frac(site.get(ExtractionSite.HP), site.get(ExtractionSite.MAX_HP));
        float shieldFrac = frac(site.get(ExtractionSite.SHIELD), site.get(ExtractionSite.MAX_SHIELD));
        float energyFrac = frac(site.get(ExtractionSite.ENERGY), site.get(ExtractionSite.MAX_ENERGY));
        long gameTime = player.level.getGameTime();
        float t = (gameTime + partialTicks) * 0.06F;

        matrixStack.pushPose();
        matrixStack.translate(-115.0D, 0.0D, 0.0D);
        bindHud();
        GuiComponent.blit(matrixStack, 11, 0, 0, 185, 176, 6, 256, 256);
        GuiComponent.blit(matrixStack, 195, 0, 0, 0, 35, 48, 256, 256);
        blitBar(matrixStack, 9, 4, 0, 49, 0, 68, 181, 68, 180, 18, 176, progress);
        blitBubbles(matrixStack, 11, 6, 14, 176, Math.round(176.0F * progress), 6, 5, t);
        blitBar(matrixStack, 22, 19, 0, 87, 0, 104, 131, 104, 130, 16, 126, hpFrac);
        blitBubbles(matrixStack, 24, 21, 12, 126, Math.round(126.0F * hpFrac), 5, 13, t + 2.1F);
        blitBar(matrixStack, 22, 31, 0, 121, 0, 138, 131, 138, 130, 16, 126, shieldFrac);
        blitBubbles(matrixStack, 24, 33, 12, 126, Math.round(126.0F * shieldFrac), 5, 13, t + 4.3F);
        GuiComponent.blit(matrixStack, 0, 2, 186, 0, 22, 22, 256, 256);
        GuiComponent.blit(matrixStack, 176, 2, 209, 0, 22, 22, 256, 256);
        GuiComponent.blit(matrixStack, 9, 19, 135, 138, 16, 16, 256, 256);
        GuiComponent.blit(matrixStack, 9, 31, 152, 138, 16, 16, 256, 256);
        for (int i = 0; i < sites.length - 1; i++) {
            GuiComponent.blit(matrixStack, 159 + 15 * i, 26, 119, 155, 15, 14, 256, 256);
        }
        for (int i = 0; i < sites.length; i++) {
            blitPip(matrixStack, 151 + 15 * i, 25, sites[i]);
        }
        GuiComponent.blit(matrixStack, 198, 2, 36, 0, 29, 44, 256, 256);
        int lit = Math.round(40.0F * energyFrac);
        if (lit >= 1) {
            int frame = (int) ((gameTime / 3L) % 4L);
            GuiComponent.blit(matrixStack, 198, 4 + 40 - lit, 66 + 30 * frame, PAD + 40 - lit, 25 + 2 * PAD, lit + PAD, 256, 256);
        } else if (site.get(ExtractionSite.ENERGY) <= 0.0F) {
            RenderSystem.setShaderColor(OUT_OF_POWER_RED, OUT_OF_POWER_GREEN_BLUE, OUT_OF_POWER_GREEN_BLUE, 1.0F);
            GuiComponent.blit(matrixStack, 198, 4, 66, PAD, 25 + 2 * PAD, 40 + PAD, 256, 256);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        for (int slot = 0; slot < ExtractionSite.THUMPER_SLOTS; slot++) {
            renderThumperBar(matrixStack, site, slot, THUMPER_BAR_X[slot], THUMPER_BAR_Y);
        }
        matrixStack.popPose();

        if (showNumbers) {
            Font font = Minecraft.getInstance().font;
            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            matrixStack.pushPose();
            matrixStack.translate(-115.0D, 0.0D, 100.0D);
            matrixStack.scale(0.7F, 0.7F, 0.7F);
            drawCentered(font, buffer, matrixStack, String.format("%.2f%%", progress * 100.0F), 99.0F / 0.7F, (13.0F - 3.5F) / 0.7F, 0xFFF6E4);
            drawCentered(font, buffer, matrixStack, String.format("%,.0f/%,.0f", site.get(ExtractionSite.HP), site.get(ExtractionSite.MAX_HP)), 80.5F / 0.7F, (27.0F - 3.5F) / 0.7F, 0xE2FFEA);
            drawCentered(font, buffer, matrixStack, String.format("%,.0f/%,.0f", site.get(ExtractionSite.SHIELD), site.get(ExtractionSite.MAX_SHIELD)), 80.5F / 0.7F, (39.0F - 3.5F) / 0.7F, 0xE2F4FF);
            buffer.endBatch();
            matrixStack.popPose();
            {
                matrixStack.pushPose();
                matrixStack.translate(-115.0D, 0.0D, 100.0D);
                matrixStack.scale(0.5F, 0.5F, 0.5F);
                for (int slot = 0; slot < ExtractionSite.THUMPER_SLOTS; slot++) {
                    int state = site.getThumperState(slot);
                    String label = state == ExtractionSite.THUMPER_STANDING
                            ? String.format("%,.0f/%,.0f", site.get(ExtractionSite.THUMPER_HP[slot]), site.get(ExtractionSite.THUMPER_MAX_HP[slot]))
                            : state == ExtractionSite.THUMPER_WRECKED ? "WRECKED" : "EMPTY";
                    float cx = THUMPER_BAR_X[slot] + THUMPER_BAR_W / 2.0F;
                    drawCentered(font, buffer, matrixStack, label, cx / 0.5F, (THUMPER_BAR_Y + 3.0F) / 0.5F,
                            state == ExtractionSite.THUMPER_WRECKED ? 0xFF8A80 : 0xE2FFEA);
                }
                buffer.endBatch();
                matrixStack.popPose();
            }
        }
    }

    private static void renderIdleHud(PoseStack matrixStack, ExtractionSite[] sites) {
        int width = 16 + 15 * (sites.length - 1);
        matrixStack.pushPose();
        matrixStack.translate(-width / 2.0D, 0.0D, 0.0D);
        bindHud();
        for (int i = 0; i < sites.length - 1; i++) {
            GuiComponent.blit(matrixStack, 8 + 15 * i, 1, 119, 155, 15, 14, 256, 256);
        }
        for (int i = 0; i < sites.length; i++) {
            blitPip(matrixStack, 15 * i, 0, sites[i]);
        }
        matrixStack.popPose();
    }

    private static final int[] THUMPER_BAR_X = {22, 112};
    private static final int THUMPER_BAR_Y = 48;
    private static final int THUMPER_BAR_W = 86;
    private static final float THUMPER_BAR_SCALE = 86.0F / 131.0F;

    /**
     * One compact thumper bar: the main HP bar sprite at two thirds size, with the main shield bar's fill, flipped top to
     * bottom to match the HP bar's slant, laid over it from the left by the shield fraction, so the hull shows only where
     * the shield is gone. A slot with no thumper is
     * drawn faded; a wrecked one gets a dark red wash.
     */
    private static void renderThumperBar(PoseStack matrixStack, ExtractionSite site, int slot, int x, int y) {
        int state = site.getThumperState(slot);
        boolean standing = state == ExtractionSite.THUMPER_STANDING;
        float hpFrac = standing ? frac(site.get(ExtractionSite.THUMPER_HP[slot]), site.get(ExtractionSite.THUMPER_MAX_HP[slot])) : 0.0F;
        float shieldFrac = standing ? frac(site.get(ExtractionSite.THUMPER_SHIELD[slot]), site.get(ExtractionSite.THUMPER_MAX_SHIELD[slot])) : 0.0F;
        bindHud();
        if (state == ExtractionSite.THUMPER_NONE) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.45F);
        }
        matrixStack.pushPose();
        matrixStack.translate(x, y, 0.0D);
        matrixStack.scale(THUMPER_BAR_SCALE, THUMPER_BAR_SCALE, 1.0F);
        blitBar(matrixStack, 0, 0, 0, 87, 0, 104, 131, 104, 130, 16, 126, hpFrac);
        if (standing && site.get(ExtractionSite.THUMPER_MAX_SHIELD[slot]) > 0.0F) {
            matrixStack.pushPose();
            matrixStack.translate(0.0D, 16.0D, 0.0D);
            matrixStack.scale(1.0F, -1.0F, 1.0F);
            RenderSystem.disableCull();
            blitFill(matrixStack, 0, 0, 0, 138, 131, 138, 16, 126, shieldFrac);
            RenderSystem.enableCull();
            matrixStack.popPose();
        }
        matrixStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int innerLeft = x + 2;
        int innerRight = x + THUMPER_BAR_W - 9;
        if (state == ExtractionSite.THUMPER_WRECKED) {
            GuiComponent.fill(matrixStack, innerLeft, y + 2, innerRight, y + 9, 0xA0500000);
        }
    }

    /** Track, then the partial fill cut with a clean vertical edge, then the bright head column. */
    private static void blitBar(PoseStack matrixStack, int blitX, int blitY, int trackU, int trackV, int fillU, int fillV, int edgeU, int edgeV, int spriteW, int spriteH, int barW, float fraction) {
        GuiComponent.blit(matrixStack, blitX, blitY, trackU, trackV, spriteW, spriteH, 256, 256);
        blitFill(matrixStack, blitX, blitY, fillU, fillV, edgeU, edgeV, spriteH, barW, fraction);
    }

    /** The partial fill of a bar sprite cut with a clean vertical edge, then its bright head column; no track. */
    private static void blitFill(PoseStack matrixStack, int blitX, int blitY, int fillU, int fillV, int edgeU, int edgeV, int spriteH, int barW, float fraction) {
        int w = Math.round(barW * clamp01(fraction));
        if (w >= 1) {
            GuiComponent.blit(matrixStack, blitX, blitY, fillU, fillV, PAD + w, spriteH, 256, 256);
        }
        if (w > 0 && w < barW) {
            GuiComponent.blit(matrixStack, blitX + PAD + w - 1, blitY, edgeU, edgeV, 3, spriteH, 256, 256);
        }
    }

    /** The bubble field is periodic with period 6, so a 6-tall window scrolled by v wraps forever. */
    private static void blitBubbles(PoseStack matrixStack, int barX, int barY, int barH, int barW, int fillW, int marginLeft, int cutRight, float phase) {
        int bx = barX + marginLeft;
        int bw = Math.min(fillW - marginLeft, barW - cutRight - 1);
        if (bw > 1) {
            float sv = ((phase % 6.0F) + 6.0F) % 6.0F;
            GuiComponent.blit(matrixStack, bx, barY + (barH - 6) / 2, bw, 6, 0.0F, 172.0F + sv, bw, 6, 256, 256);
        }
    }

    private static void blitPip(PoseStack matrixStack, int x, int y, ExtractionSite site) {
        String god = site.get(ExtractionSite.GOD);
        int[][] rows = PIP_UVS.get(god);
        if (rows == null) {
            WoldLogger.errorOnce("extraction_hud_pip_" + god, "Extraction HUD has no pip sprites for god '{}'; drawing the idona set instead.", god);
            rows = PIP_UVS.get("idona");
        }
        int state = site.isComplete() ? PIP_COMPLETE : site.isUntouched() ? PIP_UNTOUCHED : PIP_IN_PROGRESS;
        GuiComponent.blit(matrixStack, x, y, rows[state][0], rows[state][1], 16, 16, 256, 256);
    }

    private static void bindHud() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, HUD);
    }

    private static void drawCentered(Font font, MultiBufferSource.BufferSource buffer, PoseStack matrixStack, String text, float centerX, float y, int color) {
        TextComponent component = new TextComponent(text);
        font.drawInBatch(component.getVisualOrderText(), centerX - font.width(component) / 2.0F, y, color, true,
                matrixStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
    }

    private static float clamp01(float value) {
        return Math.min(1.0F, Math.max(0.0F, value));
    }

    private static float frac(float current, float max) {
        return max <= 0.0F ? 0.0F : clamp01(current / max);
    }
}
