package xyz.iwolfking.woldsvaults.api.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import iskallia.vault.VaultMod;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.helper.LightmapHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.objectives.CorruptedObjective;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public class CorruptedVaultClientHelper {
    private static final ResourceLocation VIGNETTE = VaultMod.id("textures/gui/vignette.png");
    private static final ResourceLocation CORRUPTED_HUD = VaultMod.id("textures/gui/corrupted/hud.png");




    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void corruptTooltips(ItemTooltipEvent event) {
        if(!CorruptedVaultHelper.isVaultCorrupted) return;

        List<Component> toolTip = event.getToolTip();

        for (int i = 0; i < toolTip.size(); i++) {
            if(toolTip.get(i) instanceof MutableComponent cmp) {
                toolTip.set(i, ComponentUtils.corruptComponent(cmp));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderVignette(TextColor color, float alpha, int width, int height, ResourceLocation vignette) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        int colorValue = color.getValue();
        float b = (float)(colorValue & 255) / 255.0F;
        float g = (float)(colorValue >> 8 & 255) / 255.0F;
        float r = (float)(colorValue >> 16 & 255) / 255.0F;

        RenderSystem.setShaderColor(r, g, b, alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, vignette);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0F, height, -90.0F).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width, height, -90.0F).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width, 0.0F, -90.0F).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0F, 0.0F, -90.0F).uv(0.0F, 0.0F).endVertex();

        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.0F);
        RenderSystem.defaultBlendFunc();
    }

    public static void renderObjectiveProgress(CorruptedObjective obj, PoseStack poseStack, Font font, int centerX) {
        int current = obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.COUNT);
        int total = obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TARGET);

        Component progressText = new TextComponent(String.valueOf(current)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                .append(new TextComponent(" / ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(new TextComponent(String.valueOf(total)).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));

        float progress = (float) current / (float) total;

        poseStack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int previousTex = RenderSystem.getShaderTexture(0);
        RenderSystem.setShaderTexture(0, CORRUPTED_HUD);

        poseStack.translate(centerX - 80, 8.0F, 0.0F);
        GuiComponent.blit(poseStack, 0, 0, 0.0F, 0.0F, 200, 26, 200, 100);
        GuiComponent.blit(poseStack, 0, 8, 0.0F, 30.0F, 13 + (int)(130.0F * progress), 10, 200, 100);

        RenderSystem.setShaderTexture(0, previousTex);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.scale(0.6F, 0.6F, 0.6F);

        float x = centerX / 0.6F - font.width(progressText) / 2.0F;
        float y = font.lineHeight + 22;

        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(progressText.getVisualOrderText(), x, y, -1, true, poseStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
        buffer.endBatch();

        poseStack.popPose();
    }

    public static void renderCorruptionOverlay(CorruptedObjective obj, PoseStack poseStack, Font font, Window window, int centerX) {
        float corruption = obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.CORRUPTION);
        float clamped = Math.min(corruption, 10.0f);

        float shakeAmount = 0.20F + (1.75F * (clamped / 10.0f));

        // Color transition from white to dark red
        int r = (int)(255 + (180 - 255) * (clamped / 10.0f));
        int g = (int)(255 + (-255) * (clamped / 10.0f));
        int b = (int)(255 + (-255) * (clamped / 10.0f));

        TextColor color = TextColor.fromRgb((r << 16) | (g << 8) | b);

        boolean roofDisplay = obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.COUNT) >= obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TARGET) &&
                obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TIME_TICKED_FAKE) == 401;

        String format = roofDisplay ? "???" : String.format("%.1f", corruption * 100) + "%";
        MutableComponent text = new TextComponent(format).withStyle(Style.EMPTY.withColor(color));

        renderShakingText(poseStack, text, centerX - font.width(text) / 2, 44, shakeAmount);
        renderVignette(TextColor.parseColor("#fa0000"), Mth.clamp(corruption / 35F, 0, 0.5F),
                window.getGuiScaledWidth(), window.getGuiScaledHeight(), VIGNETTE);
    }

    public static void renderShakingText(PoseStack matrixStack, MutableComponent text, int x, int y, float intensity) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        if (mc.level == null) return;

        matrixStack.pushPose();

        float effectiveIntensity = Math.min(intensity, 1.3f);
        float shakeX = (mc.level.random.nextFloat() - 0.5f) * 2.0f * effectiveIntensity;
        float shakeY = (mc.level.random.nextFloat() - 0.5f) * 2.0f * effectiveIntensity;

        matrixStack.translate(shakeX, shakeY, 0);

        // If high intensity, draw multiple blurred layers for readability
        int layers = (int)(1 + intensity * 2);

        for (int i = 0; i < layers; i++) {
            float offsetX = (mc.level.random.nextFloat() - 0.5f) * effectiveIntensity * 0.5f;
            float offsetY = (mc.level.random.nextFloat() - 0.5f) * effectiveIntensity * 0.5f;
            font.drawShadow(matrixStack, text, x + offsetX, y + offsetY, 0xFFFFFF);
        }

        matrixStack.popPose();
    }

    public static void renderTimeAddendOverlay(CorruptedObjective obj, PoseStack poseStack, Window window, Player player) {
        if (obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.DISPLAY_OVERLAY_TICK) > 0 && player != null) {
            float alpha = Math.min(1.0f, obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.DISPLAY_OVERLAY_TICK) / 40.0f);
            int textColor = (int)(255 * alpha) << 24 | 0xFFFFFF;

            float time = obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.TIME_ADDEND_TICKS) / 20.0f;
            MutableComponent text = new TextComponent("+" + time + "s");

            FontHelper.drawStringWithBorder(poseStack, text, 76, window.getGuiScaledHeight() - 44, textColor, 0xFF000000);
        }
    }

    public static void renderEscapePrompt(CorruptedObjective obj, PoseStack poseStack, Font font, int centerX) {
        MutableComponent msg1 = new TextComponent("Find an escape.").withStyle(ChatFormatting.RED);
        MutableComponent msg2 = new TextComponent("?? / " + obj.get(CorruptedObjective.DATA).get(CorruptedObjective.CData.SECONDARY_TARGET)).withStyle(ChatFormatting.RED);

        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(msg1.getVisualOrderText(), centerX - font.width(msg1) / 2.0f, 9.0f, -1, true, poseStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());
        font.drawInBatch(msg2.getVisualOrderText(), centerX - font.width(msg2) / 2.0f, 21.0f, -1, true, poseStack.last().pose(), buffer, false, 0, LightmapHelper.getPackedFullbrightCoords());

        buffer.endBatch();

    }
}
